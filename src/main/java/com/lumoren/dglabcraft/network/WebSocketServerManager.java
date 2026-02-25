package com.lumoren.dglabcraft.network;

import com.google.gson.Gson;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import com.lumoren.dglabcraft.util.WaveformGenerator;
import com.lumoren.dglabcraft.util.WaveformManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WebSocket 服务器管理器
 * 参考 CaiJi-ikun/DG_LAB 实现
 */
public class WebSocketServerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("DGLabCraft-WebSocketServer");
    private static WebSocketServerManager instance;

    private WebSocketServer server;
    private int port;
    private String sessionId;  // 对应 targetId
    private String localIp;
    private boolean isRunning = false;

    // 当前连接的客户端
    private WebSocket connectedClient;
    private String connectedClientId = null;  // App 的 clientId
    private String targetId = null;  // 我们的 targetId
    private boolean isBound = false; // 是否已收到 DGLab App 的绑定消息

    // 设备端发来的强度设置
    private int appAStrength = 0;
    private int appBStrength = 0;
    private int appAMaxStrength = 100;
    private int appBMaxStrength = 100;

    // 通道状态 (用于 HUD)
    private double channelAIntensity = 0;
    private double channelBIntensity = 0;
    private String channelAStatus = "Idle";
    private String channelBStatus = "Idle";

    private final Gson gson = new Gson();

    // 固定的客户端 ID（参考 DG_LAB）
    private static final String FIXED_CLIENT_ID = "1234-123456789-12345-12345-01";

    private WebSocketServerManager() {
        // 使用固定的 sessionId（参考 DG_LAB）
        sessionId = FIXED_CLIENT_ID;
        port = ModConfig.WS_PORT.get();
    }

    public static WebSocketServerManager getInstance() {
        if (instance == null) {
            instance = new WebSocketServerManager();
        }
        return instance;
    }

    /**
     * 启动 WebSocket 服务器
     */
    public void start() {
        if (isRunning) {
            LOGGER.info("WebSocket 服务器已在运行");
            return;
        }

        // 获取本机局域网 IP
        localIp = getLocalIpAddress();
        if (localIp == null) {
            localIp = "127.0.0.1";
        }

        try {
            server = new WebSocketServer(new InetSocketAddress(port)) {
                @Override
                public void onStart() {
                    LOGGER.info("WebSocket 服务器已启动");
                }

                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    if (!isBound) {
                        LOGGER.info("新连接: " + conn.getRemoteSocketAddress());

                        // 重置状态
                        connectedClient = conn;
                        appAStrength = 0;
                        appBStrength = 0;
                        appAMaxStrength = 100;
                        appBMaxStrength = 100;

                        // 主动发送 bind 消息（格式必须正确）
                        // {"type":"bind","clientId":"<UUID>","targetId":"","message":"targetId"}
                        String appId = UUID.randomUUID().toString();
                        conn.send("{\"type\":\"bind\",\"clientId\":\"" + appId + "\",\"targetId\":\"\",\"message\":\"targetId\"}");

                        LOGGER.info("已发送 bind 消息给客户端");

                        // 启动心跳定时器
                        startHeartbeat(conn);
                    } else {
                        // 已有连接，拒绝新连接
                        conn.send("{\"type\":\"error\",\"message\":\"400\"}");
                        conn.close();
                    }
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    LOGGER.info("Server 收到来自 {} 的消息: {}", conn.getRemoteSocketAddress(), message);
                    handleMessage(message);
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    LOGGER.info("连接关闭: " + code + " - " + reason);
                    if (connectedClient == conn) {
                        connectedClient = null;
                        connectedClientId = null;
                        isBound = false;
                        targetId = null;
                    }
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    LOGGER.error("WebSocket 错误: " + ex.getMessage());
                }
            };

            server.start();
            isRunning = true;
            LOGGER.info("WebSocket 服务器已启动: {}:{}", localIp, port);

        } catch (Exception e) {
            LOGGER.error("启动 WebSocket 服务器失败: " + e.getMessage());
        }
    }

    /**
     * 启动心跳定时器
     */
    private void startHeartbeat(WebSocket conn) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connectedClient != null && connectedClient.isOpen()) {
                    Map<String, String> heartbeat = new HashMap<>();
                    heartbeat.put("type", "heartbeat");
                    heartbeat.put("message", "200");
                    heartbeat.put("clientId", sessionId);
                    heartbeat.put("targetId", targetId != null ? targetId : "");
                    connectedClient.send(gson.toJson(heartbeat));
                }
            }
        }, 0, 60000); // 每 60 秒发送一次心跳
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (server != null) {
            try {
                server.stop();
                isRunning = false;
                LOGGER.info("WebSocket 服务器已停止");
            } catch (Exception e) {
                LOGGER.error("停止服务器失败: " + e.getMessage());
            }
        }
    }

    /**
     * 处理收到的消息
     */
    private void handleMessage(String message) {
        // 1. 打印原始消息
        LOGGER.info("收到 App 消息: {}", message);

        try {
            // 使用 JsonObject 解析
            com.google.gson.JsonObject json = gson.fromJson(message, com.google.gson.JsonObject.class);
            if (json == null) return;

            String type = json.has("type") ? json.get("type").getAsString() : null;

            if ("bind".equals(type)) {
                // 提取 message 字段，检查是否为 "DGLAB"
                String msgContent = json.has("message") ? json.get("message").getAsString() : null;
                String appClientId = json.has("clientId") ? json.get("clientId").getAsString() : null;
                String receivedTargetId = json.has("targetId") ? json.get("targetId").getAsString() : null;

                LOGGER.info("收到 bind: appClientId={}, targetId={}, message={}", appClientId, receivedTargetId, msgContent);

                // 检查 message 是否为 "DGLAB"
                if ("DGLAB".equals(msgContent)) {
                    // 返回 200 确认包
                    // {"type":"bind","clientId":"<PC_ID>","targetId":"<appId>","message":"200","statusCode":200}
                    connectedClient.send("{\"type\":\"bind\",\"clientId\":\"" + FIXED_CLIENT_ID + "\",\"targetId\":\"" + receivedTargetId + "\",\"message\":\"200\",\"statusCode\":200}");

                    // 标记绑定成功
                    connectedClientId = appClientId;
                    targetId = receivedTargetId;
                    isBound = true;

                    LOGGER.info("设备绑定成功: " + appClientId);
                }
            } else if ("heartbeat".equals(type)) {
                // 心跳响应 - 必须包含正确的 targetId
                String receivedTargetId = json.has("targetId") ? json.get("targetId").getAsString() : null;

                // {"type":"heartbeat","clientId":"<PC_ID>","targetId":"<appId>","message":"200"}
                connectedClient.send("{\"type\":\"heartbeat\",\"clientId\":\"" + FIXED_CLIENT_ID + "\",\"targetId\":\"" + receivedTargetId + "\",\"message\":\"200\"}");
                LOGGER.info("心跳响应已发送");
            } else if ("msg".equals(type)) {
                String msgContent = json.has("message") ? json.get("message").getAsString() : null;
                if (msgContent != null) {
                    // 解析强度消息: 格式如 "strength-1+2+50" 或 "pulse-A:[...]"
                    parseStrengthMessage(msgContent);
                }
            }
        } catch (Exception e) {
            LOGGER.error("解析消息失败: " + e.getMessage());
        }
    }

    /**
     * 解析强度消息
     * 格式: strength-<通道>+<模式>+<值>
     * 通道: 1=A, 2=B
     * 模式: 0=减少, 1=增加, 2=设置
     */
    private void parseStrengthMessage(String msg) {
        if (msg.startsWith("strength-")) {
            // 解析格式: strength-1+2+50
            String[] parts = msg.substring(9).split("\\+");
            if (parts.length >= 3) {
                try {
                    int channel = Integer.parseInt(parts[0]);
                    int mode = Integer.parseInt(parts[1]);
                    int value = Integer.parseInt(parts[2]);

                    if (channel == 1) {
                        appAMaxStrength = value;
                    } else if (channel == 2) {
                        appBMaxStrength = value;
                    }
                    LOGGER.info("收到强度设置: 通道{}, 模式{}, 值{}", channel, mode, value);
                } catch (NumberFormatException e) {
                    LOGGER.error("解析强度值失败: " + msg);
                }
            }
        }
    }

    /**
     * 发送刺激到连接的客户端
     * 格式: strength-<通道>+<模式>+<值>
     */
    public void sendStimulus(String channel, String waveType, double intensity, int duration) {
        if (connectedClient == null || !connectedClient.isOpen()) {
            return;
        }

        try {
            int channelNum = "A".equalsIgnoreCase(channel) ? 1 : 2;
            String channelStr = "A".equalsIgnoreCase(channel) ? "A" : "B";

            // 根据波形类型决定模式
            int mode;
            if ("increase".equals(waveType)) {
                mode = 1;
            } else if ("decrease".equals(waveType)) {
                mode = 0;
            } else {
                mode = 2; // 设置值
            }

            int value = (int) intensity;

            // 更新通道状态
            if (channelNum == 1) {
                channelAIntensity = intensity;
                channelAStatus = waveType;
            } else {
                channelBIntensity = intensity;
                channelBStatus = waveType;
            }

            // 发送波形配置 (让 App 显示对应的波形) - 使用 WaveformManager 获取实际波形数据
            if (waveType != null && !waveType.equals("increase") && !waveType.equals("decrease")) {
                // 从 WaveformManager 获取实际波形数据
                List<String> waveformData = WaveformManager.getInstance().getWaveform(waveType);

                // 构造脉冲格式: pulse-A:[hex1,hex2,...]
                StringBuilder waveformMessage = new StringBuilder();
                waveformMessage.append("pulse-").append(channelStr).append(":[");
                for (int i = 0; i < waveformData.size(); i++) {
                    if (i > 0) waveformMessage.append(",");
                    waveformMessage.append(waveformData.get(i));
                }
                waveformMessage.append("]");

                Map<String, String> waveformMsg = new HashMap<>();
                waveformMsg.put("type", "msg");
                waveformMsg.put("message", waveformMessage.toString());
                waveformMsg.put("clientId", sessionId);
                waveformMsg.put("targetId", targetId != null ? targetId : "");
                connectedClient.send(gson.toJson(waveformMsg));
                LOGGER.info("发送波形配置: " + waveformMessage);
            }

            // 发送强度值
            Map<String, String> msg = new HashMap<>();
            msg.put("type", "msg");
            msg.put("message", "strength-" + channelNum + "+" + mode + "+" + value);
            msg.put("clientId", sessionId);
            msg.put("targetId", targetId != null ? targetId : "");

            connectedClient.send(gson.toJson(msg));
            LOGGER.info("发送强度: strength-" + channelNum + "+" + mode + "+" + value);

            // 如果启用了通道同步，同时发送到另一个通道
            if (ModConfig.SYNC_CHANNELS.get()) {
                String otherChannel = "A".equalsIgnoreCase(channel) ? "B" : "A";
                int otherChannelNum = "A".equalsIgnoreCase(otherChannel) ? 1 : 2;
                String otherChannelStr = "A".equalsIgnoreCase(otherChannel) ? "A" : "B";

                // 更新另一个通道的状态
                if (otherChannelNum == 1) {
                    channelAIntensity = intensity;
                    channelAStatus = waveType;
                } else {
                    channelBIntensity = intensity;
                    channelBStatus = waveType;
                }

                // 发送波形配置到另一个通道 - 使用实际波形数据
                if (waveType != null && !waveType.equals("increase") && !waveType.equals("decrease")) {
                    // 从 WaveformManager 获取实际波形数据
                    List<String> waveformData = WaveformManager.getInstance().getWaveform(waveType);

                    // 构造脉冲格式: pulse-B:[hex1,hex2,...]
                    StringBuilder waveformMessage = new StringBuilder();
                    waveformMessage.append("pulse-").append(otherChannelStr).append(":[");
                    for (int i = 0; i < waveformData.size(); i++) {
                        if (i > 0) waveformMessage.append(",");
                        waveformMessage.append(waveformData.get(i));
                    }
                    waveformMessage.append("]");

                    Map<String, String> otherWaveformMsg = new HashMap<>();
                    otherWaveformMsg.put("type", "msg");
                    otherWaveformMsg.put("message", waveformMessage.toString());
                    otherWaveformMsg.put("clientId", sessionId);
                    otherWaveformMsg.put("targetId", targetId != null ? targetId : "");
                    connectedClient.send(gson.toJson(otherWaveformMsg));
                    LOGGER.info("同步发送波形配置: " + waveformMessage);
                }

                // 发送强度值到另一个通道
                Map<String, String> otherMsg = new HashMap<>();
                otherMsg.put("type", "msg");
                otherMsg.put("message", "strength-" + otherChannelNum + "+" + mode + "+" + value);
                otherMsg.put("clientId", sessionId);
                otherMsg.put("targetId", targetId != null ? targetId : "");
                connectedClient.send(gson.toJson(otherMsg));
                LOGGER.info("同步发送强度: strength-" + otherChannelNum + "+" + mode + "+" + value);
            }
        } catch (Exception e) {
            LOGGER.error("发送刺激失败: " + e.getMessage());
        }
    }

    /**
     * 发送波形数据 (带分块机制) - 严格遵守协议
     * 根据 syncChannels 配置决定使用单通道或双通道同步模式
     *
     * @param channel 通道 "A" 或 "B"
     * @param waveId 波形 ID (对应文件名)
     * @param intensity 强度值
     */
    public void sendWaveformData(String channel, String waveId, double intensity) {
        if (connectedClient == null || !connectedClient.isOpen()) {
            return;
        }

        // 同步模式：使用通道 ID 3 实现零延迟双通道同步
        if (ModConfig.SYNC_CHANNELS.get()) {
            sendWaveformDataDualChannel(waveId, intensity);
            return;
        }

        // 普通模式：单通道发送 - 原有逻辑完全保留
        sendWaveformDataSingleChannel(channel, waveId, intensity);
    }

    /**
     * 单通道发送 (原有逻辑)
     */
    private void sendWaveformDataSingleChannel(String channel, String waveId, double intensity) {
        try {
            // 通道转换: A=1, B=2
            int channelNum = "A".equalsIgnoreCase(channel) ? 1 : 2;
            String channelStr = "A".equalsIgnoreCase(channel) ? "A" : "B";

            int value = (int) intensity;

            // 1. clear-<channelNum>
            sendMessage("clear-" + channelNum);

            // 2. pulse-<A|B>:[...] (分块)
            var chunks = WaveformManager.getInstance().getWaveformChunks(waveId, 100);
            for (List<String> chunk : chunks) {
                sendPulseMessage(channelStr, chunk);
            }

            // 3. strength-<channelNum>+2+<value>
            sendMessage("strength-" + channelNum + "+2+" + value);

            // 更新状态
            if (channelNum == 1) {
                channelAIntensity = intensity;
                channelAStatus = waveId;
            } else {
                channelBIntensity = intensity;
                channelBStatus = waveId;
            }

        } catch (Exception e) {
            LOGGER.error("发送波形数据失败: " + e.getMessage());
        }
    }

    /**
     * 双通道同步发送 - 使用通道 ID 3 实现零延迟
     *
     * @param waveId 波形 ID (对应文件名)
     * @param intensity 强度值
     */
    private void sendWaveformDataDualChannel(String waveId, double intensity) {
        try {
            int value = (int) intensity;

            // ===== 第1步: 一次性清空双通道 =====
            sendMessage("clear-3");
            LOGGER.info("发送清空命令: clear-3 (双通道)");

            // ===== 第2步: 分别灌入 A/B 波形队列 =====
            var chunks = WaveformManager.getInstance().getWaveformChunks(waveId, 100);

            // 先发送所有 A 通道波形块
            for (List<String> chunk : chunks) {
                sendPulseMessage("A", chunk);
            }
            // 再发送所有 B 通道波形块
            for (List<String> chunk : chunks) {
                sendPulseMessage("B", chunk);
            }

            // ===== 第3步: 瞬间同时施加强度 =====
            // 注意: DGLab协议可能不支持 strength-3，需要分别发送到通道1和通道2
            sendMessage("strength-1+2+" + value);
            sendMessage("strength-2+2+" + value);
            LOGGER.info("发送强度: strength-1+2+{} + strength-2+2+{} (双通道同步)", value, value);

            // 更新通道状态
            channelAIntensity = intensity;
            channelAStatus = waveId;
            channelBIntensity = intensity;
            channelBStatus = waveId;

        } catch (Exception e) {
            LOGGER.error("发送双通道波形数据失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息辅助方法
     */
    private void sendMessage(String message) {
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "msg");
        msg.put("message", message);
        msg.put("clientId", sessionId);
        msg.put("targetId", targetId != null ? targetId : "");
        connectedClient.send(gson.toJson(msg));
    }

    /**
     * 发送波形消息辅助方法
     * 格式: pulse-A:["0A0A0A0A64646464", "1919181864646464"]
     * 每个元素必须是 16 位大写十六进制字符串
     */
    private void sendPulseMessage(String channelStr, List<String> chunk) {
        // 将列表转换为 JSON 数组字符串 (紧凑格式，无空格)
        StringBuilder hexArray = new StringBuilder("[");
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) hexArray.append(",");
            // 转换为大写，确保 16 位
            String hex = chunk.get(i).toUpperCase();
            // 补齐到 16 位
            while (hex.length() < 16) hex = "0" + hex;
            // 截断超过 16 位的内容
            if (hex.length() > 16) hex = hex.substring(0, 16);
            hexArray.append("\"").append(hex).append("\"");
        }
        hexArray.append("]");

        // 发送消息: pulse-A:[...]
        sendMessage("pulse-" + channelStr + ":" + hexArray.toString());
        LOGGER.info("发送波形分块: pulse-{}:{}", channelStr, hexArray);
    }

    /**
     * 停止刺激
     */
    public void stopStimulus(String channel) {
        int channelNum = "A".equalsIgnoreCase(channel) ? 1 : 2;

        if (channelNum == 1) {
            channelAIntensity = 0;
            channelAStatus = "Idle";
        } else {
            channelBIntensity = 0;
            channelBStatus = "Idle";
        }

        // 发送清除命令
        if (connectedClient != null && connectedClient.isOpen()) {
            Map<String, String> msg = new HashMap<>();
            msg.put("type", "msg");
            msg.put("message", "clear-" + channelNum);
            msg.put("clientId", sessionId);
            msg.put("targetId", targetId != null ? targetId : "");
            connectedClient.send(gson.toJson(msg));
        }
    }

    /**
     * 发送波形到客户端
     */
    public void sendWaveform(String channel, String waveform) {
        if (connectedClient == null || !connectedClient.isOpen()) {
            return;
        }

        int channelNum = "A".equalsIgnoreCase(channel) ? 1 : 2;

        // 先清除之前的波形
        Map<String, String> clearMsg = new HashMap<>();
        clearMsg.put("type", "msg");
        clearMsg.put("message", "clear-" + channelNum);
        clearMsg.put("clientId", sessionId);
        clearMsg.put("targetId", targetId != null ? targetId : "");
        connectedClient.send(gson.toJson(clearMsg));

        // 发送新波形
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "msg");
        msg.put("message", "pulse-" + (channelNum == 1 ? "A" : "B") + ":[" + waveform + "]");
        msg.put("clientId", sessionId);
        msg.put("targetId", targetId != null ? targetId : "");
        connectedClient.send(gson.toJson(msg));
    }

    /**
     * 生成二维码 URL（参考 DG_LAB 格式）
     */
    public String generateQrUrl() {
        // 使用 dungeon-lab.com 格式（参考 DG_LAB）
        String url = String.format("https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#ws://%s:%d/%s",
            localIp, port, FIXED_CLIENT_ID);

        // 生成二维码图片
        QRCodeGenerator.generateQRCode(url);
        return url;
    }

    /**
     * 获取本机局域网 IPv4 地址
     */
    private String getLocalIpAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;

                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof java.net.Inet4Address) {
                        String ip = addr.getHostAddress();
                        // 排除 127.x.x.x 和 169.254.x.x
                        if (!ip.startsWith("127.") && !ip.startsWith("169.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取 IP 地址失败: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    // Getters
    public boolean isRunning() { return isRunning; }
    public boolean isConnected() { return isBound && connectedClient != null && connectedClient.isOpen(); }
    public String getSessionId() { return sessionId; }
    public String getLocalIp() { return localIp; }
    public int getPort() { return port; }
    public String getConnectedClientId() { return connectedClientId; }

    // 获取 App 设置的最大强度
    public int getAppAMaxStrength() { return appAMaxStrength; }
    public int getAppBMaxStrength() { return appBMaxStrength; }

    public double getChannelAIntensity() { return channelAIntensity; }
    public double getChannelBIntensity() { return channelBIntensity; }
    public String getChannelAStatus() { return channelAStatus; }
    public String getChannelBStatus() { return channelBStatus; }
}

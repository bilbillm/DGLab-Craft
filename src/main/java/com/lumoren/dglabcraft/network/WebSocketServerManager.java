package com.lumoren.dglabcraft.network;

import com.google.gson.Gson;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
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
        sessionId = "1234-123456789-12345-12345-00";
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
                        // {"type":"bind","clientId":"1234-123456789-12345-12345-01","targetId":"","message":"DGLabCraft"}
                        conn.send("{\"type\":\"bind\",\"clientId\":\"" + FIXED_CLIENT_ID + "\",\"targetId\":\"\",\"message\":\"DGLabCraft\"}");

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
                // 2. 提取 App 发来的 clientId
                String appClientId = json.has("clientId") ? json.get("clientId").getAsString() : null;
                String appTargetId = json.has("targetId") ? json.get("targetId").getAsString() : null;

                LOGGER.info("收到 bind: appClientId={}, appTargetId={}", appClientId, appTargetId);

                // 3. 检查是否匹配
                if (FIXED_CLIENT_ID.equals(appClientId) && sessionId.equals(appTargetId)) {
                    // 4. 构造正确格式的响应
                    // {"type":"bind","clientId":"<PC_ID>","targetId":"<appClientId>","statusCode":200,"message":"200"}
                    com.google.gson.JsonObject response = new com.google.gson.JsonObject();
                    response.addProperty("type", "bind");
                    response.addProperty("clientId", sessionId);
                    response.addProperty("targetId", appClientId);  // 使用 App 的 clientId
                    response.addProperty("statusCode", 200);
                    response.addProperty("message", "200");

                    connectedClient.send(gson.toJson(response));

                    // 5. 标记绑定成功
                    connectedClientId = appClientId;
                    targetId = appClientId;
                    isBound = true;

                    LOGGER.info("设备绑定成功: " + appClientId);
                }
            } else if ("heartbeat".equals(type)) {
                // 6. 心跳响应 - 同样格式
                String appClientId = json.has("clientId") ? json.get("clientId").getAsString() : null;

                com.google.gson.JsonObject hbResponse = new com.google.gson.JsonObject();
                hbResponse.addProperty("type", "heartbeat");
                hbResponse.addProperty("clientId", sessionId);
                hbResponse.addProperty("targetId", appClientId != null ? appClientId : "");
                hbResponse.addProperty("statusCode", 200);
                hbResponse.addProperty("message", "200");

                connectedClient.send(gson.toJson(hbResponse));
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

            // 发送消息
            Map<String, String> msg = new HashMap<>();
            msg.put("type", "msg");
            msg.put("message", "strength-" + channelNum + "+" + mode + "+" + value);
            msg.put("clientId", sessionId);
            msg.put("targetId", targetId != null ? targetId : "");

            connectedClient.send(gson.toJson(msg));
        } catch (Exception e) {
            LOGGER.error("发送刺激失败: " + e.getMessage());
        }
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

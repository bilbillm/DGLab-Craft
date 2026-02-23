package com.lumoren.dglabcraft.network;

import com.google.gson.Gson;
import com.lumoren.dglabcraft.config.ModConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("DGLabCraft-WebSocket");
    private static WebSocketManager instance;

    private WebSocketClient webSocketClient;
    private ScheduledExecutorService reconnectScheduler;
    private boolean isConnected = false;
    private boolean isConnecting = false;

    // 当前通道状态 (用于 HUD 显示)
    private double channelAIntensity = 0;
    private double channelBIntensity = 0;
    private String channelAStatus = "Idle";
    private String channelBStatus = "Idle";

    private final Gson gson = new Gson();

    private WebSocketManager() {
        startReconnectScheduler();
    }

    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    /**
     * 启动重连调度器
     */
    private void startReconnectScheduler() {
        reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        reconnectScheduler.scheduleAtFixedRate(() -> {
            if (ModConfig.WS_ENABLED.get() && !isConnected && !isConnecting) {
                connect();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 连接到 WebSocket 服务器
     */
    public void connect() {
        if (isConnecting || isConnected) {
            return;
        }

        String host = ModConfig.WS_HOST.get();
        int port = ModConfig.WS_PORT.get();

        try {
            String url = "ws://" + host + ":" + port;
            LOGGER.info("尝试连接 WebSocket: {}", url);

            webSocketClient = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    isConnected = true;
                    isConnecting = false;
                    LOGGER.info("WebSocket 连接已建立");
                }

                @Override
                public void onMessage(String message) {
                    LOGGER.debug("收到消息: {}", message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    isConnecting = false;
                    LOGGER.info("WebSocket 连接关闭: {} - {}", code, reason);
                }

                @Override
                public void onError(Exception ex) {
                    isConnecting = false;
                    LOGGER.error("WebSocket 错误: {}", ex.getMessage());
                }
            };

            isConnecting = true;
            webSocketClient.connect();

        } catch (URISyntaxException e) {
            LOGGER.error("WebSocket URL 格式错误: {}", e.getMessage());
            isConnecting = false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        isConnected = false;
        isConnecting = false;
    }

    /**
     * 发送刺激数据到 DGLab
     * @param channel 通道 "A" 或 "B"
     * @param waveType 波形类型 (如 "sine", "square", "pulse", "constant")
     * @param intensity 强度 0.0 - 1.0
     * @param duration 持续时间 (毫秒)
     */
    public void sendStimulus(String channel, String waveType, double intensity, int duration) {
        if (!isConnected) {
            LOGGER.warn("WebSocket 未连接，无法发送刺激");
            return;
        }

        // 更新通道状态
        if ("A".equals(channel)) {
            channelAIntensity = intensity;
            channelAStatus = waveType;
        } else {
            channelBIntensity = intensity;
            channelBStatus = waveType;
        }

        // 构建 JSON 消息 (根据 DGLab API 格式)
        DGLabMessage message = new DGLabMessage(channel, waveType, intensity, duration);
        String json = gson.toJson(message);

        try {
            webSocketClient.send(json);
            LOGGER.debug("发送刺激: {}", json);
        } catch (Exception e) {
            LOGGER.error("发送刺激失败: {}", e.getMessage());
        }
    }

    /**
     * 停止指定通道的刺激
     */
    public void stopStimulus(String channel) {
        if ("A".equals(channel)) {
            channelAIntensity = 0;
            channelAStatus = "Idle";
        } else {
            channelBIntensity = 0;
            channelBStatus = "Idle";
        }
        sendStimulus(channel, "stop", 0, 0);
    }

    /**
     * 测试波形 - 发送一个测试信号
     */
    public void testConnection() {
        sendStimulus("A", "sine", 0.5, 1000);
        sendStimulus("B", "pulse", 0.5, 1000);
    }

    // Getters for HUD
    public boolean isConnected() { return isConnected; }
    public double getChannelAIntensity() { return channelAIntensity; }
    public double getChannelBIntensity() { return channelBIntensity; }
    public String getChannelAStatus() { return channelAStatus; }
    public String getChannelBStatus() { return channelBStatus; }

    /**
     * DGLab 消息格式
     */
    private static class DGLabMessage {
        String channel;
        String wave;
        double intensity;
        int duration;

        DGLabMessage(String channel, String wave, double intensity, int duration) {
            this.channel = channel;
            this.wave = wave;
            this.intensity = intensity;
            this.duration = duration;
        }
    }
}

package com.lumoren.dglabcraft.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lumoren.dglabcraft.config.ModConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LanConnectionTest {
    @Test void occupiedPortAutomaticallyFallsBackAndAppHandshakeUsesCorrectIds() throws Exception {
        ModConfig.SPEC.setConfig(CommentedConfig.inMemory());
        ModConfig.WS_RELAY_ENABLED.set(false); ModConfig.WS_ENABLED.set(true);
        ModConfig.WS_HOST.set("192.168.1.2");
        var constructor = WebSocketServerManager.class.getDeclaredConstructor(); constructor.setAccessible(true);
        try (ServerSocket occupied = new ServerSocket(0)) {
            ModConfig.WS_PORT.set(occupied.getLocalPort());
            WebSocketServerManager manager = constructor.newInstance();
            try {
                assertTrue(manager.pairingUrl().isEmpty()); manager.start();
                RelayConnectionTest.until(manager::isRunning);
                assertNotEquals(occupied.getLocalPort(), manager.getPort()); assertTrue(manager.getPort() > 0);
                String payload = URLDecoder.decode(manager.pairingUrl().split("#DGLAB-SOCKET#")[1], StandardCharsets.UTF_8);
                assertEquals(manager.getPort(), URI.create(payload).getPort());
                LinkedBlockingQueue<JsonObject> frames = new LinkedBlockingQueue<>();
                WebSocketClient phone = new WebSocketClient(URI.create("ws://127.0.0.1:" + manager.getPort() + "/" + manager.getSessionId())) {
                    @Override public void onOpen(ServerHandshake handshake) { }
                    @Override public void onMessage(String raw) { frames.add(JsonParser.parseString(raw).getAsJsonObject()); }
                    @Override public void onClose(int code, String reason, boolean remote) { }
                    @Override public void onError(Exception error) { }
                };
                try {
                    assertTrue(phone.connectBlocking(3, TimeUnit.SECONDS));
                    JsonObject hello;
                    do { hello = frames.poll(2, TimeUnit.SECONDS); assertNotNull(hello); }
                    while (!"bind".equals(hello.get("type").getAsString()));
                    String appId = hello.get("clientId").getAsString();
                    JsonObject bind = new JsonObject(); bind.addProperty("type", "bind");
                    bind.addProperty("clientId", manager.getSessionId()); bind.addProperty("targetId", appId); bind.addProperty("message", "DGLAB");
                    phone.send(bind.toString()); RelayConnectionTest.until(manager::isConnected);
                    assertEquals(appId, manager.getTargetId()); assertEquals(appId, manager.getConnectedClientId());
                    JsonObject ack;
                    do { ack = frames.poll(2, TimeUnit.SECONDS); assertNotNull(ack); }
                    while (!"bind".equals(ack.get("type").getAsString()));
                    assertEquals(appId, ack.get("targetId").getAsString());
                    assertEquals("200", ack.get("message").getAsString());
                    frames.clear(); manager.sendStrengthOnly("A", 0);
                    JsonObject command = frames.poll(2, TimeUnit.SECONDS); assertNotNull(command);
                    assertEquals("strength-1+2+0", command.get("message").getAsString());
                    ModConfig.MAX_INTENSITY_PERCENTAGE.set(0.0);
                    manager.sendStrengthOnly("A", 50);
                    assertEquals("strength-1+2+0", frames.poll(2, TimeUnit.SECONDS).get("message").getAsString());
                    ModConfig.MAX_INTENSITY_PERCENTAGE.set(50.0);
                    manager.parseStrengthMessage("strength-0+0+40+60");
                    manager.sendDualChannelStrengthOnly(999, 999);
                    assertEquals("strength-1+2+20", frames.poll(2, TimeUnit.SECONDS).get("message").getAsString());
                    assertEquals("strength-2+2+30", frames.poll(2, TimeUnit.SECONDS).get("message").getAsString());
                } finally { phone.closeBlocking(); }
            } finally { manager.stop(); }
            assertFalse(manager.isRunning()); assertTrue(manager.pairingUrl().isEmpty());
        } finally { ModConfig.SPEC.setConfig(null); }
    }
}

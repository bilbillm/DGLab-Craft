package com.lumoren.dglabcraft.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: pairs two test clients on DG-LAB's real relay; never targets a physical device. */
@Tag("live")
class OfficialRelaySmokeTest {
    @Test void officialRelayPairsAndForwardsZeroStrengthCommands() throws Exception {
        try (RelayConnection connection = new RelayConnection(RelayConnection.OFFICIAL_ENDPOINT, s -> {}, () -> {})) {
            connection.start();
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (connection.snapshot().qrUrl().isEmpty() && System.nanoTime() < end) Thread.sleep(50);
            assertFalse(connection.snapshot().qrUrl().isEmpty(), "Official relay must provide a pairing ID");
            String encoded = connection.snapshot().qrUrl().split("#DGLAB-SOCKET#", 2)[1];
            URI appUrl = URI.create(URLDecoder.decode(encoded, StandardCharsets.UTF_8));
            String controller = appUrl.getPath().substring(1);
            LinkedBlockingQueue<String> commands = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<String> allocated = new LinkedBlockingQueue<>();
            WebSocketClient phone = new WebSocketClient(appUrl) {
                @Override public void onOpen(ServerHandshake handshake) { }
                @Override public void onMessage(String message) {
                    JsonObject frame = JsonParser.parseString(message).getAsJsonObject();
                    String type = frame.get("type").getAsString();
                    String body = frame.has("message") ? frame.get("message").getAsString() : "";
                    if ("bind".equals(type) && "targetId".equals(body)) allocated.add(frame.get("clientId").getAsString());
                    if ("msg".equals(type)) commands.add(body);
                }
                @Override public void onClose(int code, String reason, boolean remote) { }
                @Override public void onError(Exception error) { }
            };
            try {
                assertTrue(phone.connectBlocking(10, TimeUnit.SECONDS));
                String appId = allocated.poll(5, TimeUnit.SECONDS);
                Thread.sleep(500); // Current relay pairs URL targets automatically; legacy servers require bind.
                if (connection.snapshot().state() != RelayConnection.State.CONNECTED) {
                    JsonObject bind = new JsonObject(); bind.addProperty("type", "bind");
                    bind.addProperty("clientId", controller); bind.addProperty("targetId", appId); bind.addProperty("message", "DGLAB");
                    phone.send(bind.toString());
                }
                end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while (connection.snapshot().state() != RelayConnection.State.CONNECTED && System.nanoTime() < end) Thread.sleep(50);
                assertEquals(RelayConnection.State.CONNECTED, connection.snapshot().state());
                assertTrue(connection.send("clear-1")); assertTrue(connection.send("strength-1+2+0"));
                assertEquals("clear-1", commands.poll(5, TimeUnit.SECONDS));
                assertEquals("strength-1+2+0", commands.poll(5, TimeUnit.SECONDS));
            } finally { phone.closeBlocking(); }
        }
    }
}

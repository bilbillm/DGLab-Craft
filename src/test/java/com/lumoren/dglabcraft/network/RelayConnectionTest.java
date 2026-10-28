package com.lumoren.dglabcraft.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class RelayConnectionTest {
    static final class Relay extends WebSocketServer implements AutoCloseable {
        final CountDownLatch ready = new CountDownLatch(1);
        final LinkedBlockingQueue<String> received = new LinkedBlockingQueue<>();
        final AtomicInteger generations = new AtomicInteger();
        volatile WebSocket controller;
        volatile String controllerId;
        volatile boolean greet = true;
        Relay() throws Exception {
            super(new InetSocketAddress("127.0.0.1", 0));
            start(); assertTrue(ready.await(3, TimeUnit.SECONDS));
        }
        URI endpoint() { return URI.create("ws://127.0.0.1:" + getPort()); }
        @Override public void onStart() { ready.countDown(); }
        @Override public void onOpen(WebSocket ws, ClientHandshake request) {
            controller = ws; controllerId = "controller-" + generations.incrementAndGet();
            if (greet) ws.send("{\"type\":\"bind\",\"clientId\":\"" + controllerId + "\",\"targetId\":\"\",\"message\":\"targetId\"}");
        }
        void frame(String type, String client, String peer, String message) {
            JsonObject json = new JsonObject();
            json.addProperty("type", type); json.addProperty("clientId", client);
            json.addProperty("targetId", peer); json.addProperty("message", message);
            controller.send(json.toString());
        }
        void pair() { frame("bind", controllerId, "phone", "200"); }
        @Override public void onMessage(WebSocket ws, String message) { received.add(message); }
        @Override public void onClose(WebSocket ws, int code, String reason, boolean remote) { }
        @Override public void onError(WebSocket ws, Exception error) { }
        @Override public void close() throws Exception { stop(1000); }
    }
    static void until(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        while (!condition.getAsBoolean() && System.nanoTime() < end) Thread.sleep(10);
        assertTrue(condition.getAsBoolean(), "Expected connection lifecycle transition");
    }
    @Test void opensAnOutboundConnectionAndOnlyPublishesReadyPairingCodes() throws Exception {
        try (Relay relay = new Relay(); RelayConnection connection = new RelayConnection(relay.endpoint(), s -> {}, () -> {})) {
            assertTrue(connection.snapshot().qrUrl().isEmpty());
            assertFalse(connection.send("strength-1+2+0"));
            connection.start();
            until(() -> connection.snapshot().state() == RelayConnection.State.WAITING_FOR_APP);
            assertTrue(connection.snapshot().qrUrl().contains("DGLAB-SOCKET#"));
            assertTrue(connection.snapshot().qrUrl().contains(relay.controllerId));
            assertFalse(connection.send("strength-1+2+0"));
            relay.pair(); until(() -> connection.snapshot().state() == RelayConnection.State.CONNECTED);
            assertTrue(connection.send("strength-1+2+0"));
            JsonObject packet = JsonParser.parseString(relay.received.poll(2, TimeUnit.SECONDS)).getAsJsonObject();
            assertEquals("msg", packet.get("type").getAsString());
            assertEquals(relay.controllerId, packet.get("clientId").getAsString());
            assertEquals("phone", packet.get("targetId").getAsString());
            assertEquals("strength-1+2+0", packet.get("message").getAsString());
        }
    }
    @Test void reconnectInvalidatesOldQrAndNeverReplaysFeedback() throws Exception {
        AtomicInteger resets = new AtomicInteger();
        try (Relay relay = new Relay(); RelayConnection connection = new RelayConnection(relay.endpoint(), s -> {}, resets::incrementAndGet)) {
            connection.start(); until(() -> !connection.snapshot().qrUrl().isEmpty());
            String oldCode = connection.snapshot().qrUrl(); relay.pair();
            until(() -> connection.snapshot().state() == RelayConnection.State.CONNECTED);
            relay.controller.close(); until(() -> relay.generations.get() >= 2 && !connection.snapshot().qrUrl().isEmpty());
            assertNotEquals(oldCode, connection.snapshot().qrUrl());
            assertEquals(RelayConnection.State.WAITING_FOR_APP, connection.snapshot().state());
            assertFalse(connection.send("strength-1+2+20")); assertTrue(relay.received.isEmpty());
            assertTrue(resets.get() > 0);
            connection.close(); assertEquals(RelayConnection.State.STOPPED, connection.snapshot().state());
            assertTrue(connection.snapshot().qrUrl().isEmpty());
            int count = relay.generations.get(); Thread.sleep(1200); assertEquals(count, relay.generations.get());
        }
    }
    @Test void ignoresForeignAndMalformedReportsAndSilencesOnPeerBreak() throws Exception {
        LinkedBlockingQueue<String> reports = new LinkedBlockingQueue<>();
        try (Relay relay = new Relay(); RelayConnection connection = new RelayConnection(relay.endpoint(), reports::add, () -> {})) {
            connection.start(); until(() -> !connection.snapshot().qrUrl().isEmpty());
            relay.frame("bind", "foreign", "phone", "200");
            relay.controller.send("{oops"); relay.controller.send("[]");
            relay.pair(); until(() -> connection.snapshot().state() == RelayConnection.State.CONNECTED);
            relay.frame("msg", "foreign", "phone", "strength-0+0+25+40");
            relay.frame("msg", relay.controllerId, "other-phone", "strength-0+0+25+40");
            relay.frame("msg", relay.controllerId, "phone", "strength-0+0+25+40");
            assertEquals("strength-0+0+25+40", reports.poll(2, TimeUnit.SECONDS)); assertTrue(reports.isEmpty());
            relay.frame("break", relay.controllerId, "phone", "209");
            until(() -> connection.snapshot().state() != RelayConnection.State.CONNECTED);
            assertFalse(connection.send("strength-1+2+10"));
        }
    }
    @Test void silentRelayDoesNotAdvertiseAStaleOrInventedQrCode() throws Exception {
        try (Relay relay = new Relay(); RelayConnection connection = new RelayConnection(relay.endpoint(), s -> {}, () -> {})) {
            relay.greet = false; connection.start(); until(() -> relay.generations.get() == 1);
            assertTrue(connection.snapshot().qrUrl().isEmpty());
            assertFalse(connection.send("strength-1+2+1"));
        }
    }
}

package com.lumoren.dglabcraft.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Owns the pairing lifecycle; callers send device commands without knowing relay IDs. */
public final class RelayConnection implements AutoCloseable {
    public static final URI OFFICIAL_ENDPOINT = URI.create("wss://ws.dungeon-lab.cn/");
    public enum State { IDLE, CONNECTING, WAITING_FOR_APP, CONNECTED, RETRYING, STOPPED }
    public record Snapshot(State state, String qrUrl, String peerId) { }
    private final URI endpoint;
    private final Consumer<String> report;
    private final Runnable disconnected;
    private final Object lock = new Object();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "DGLabCraft-Pairing"); thread.setDaemon(true); return thread;
    });
    private volatile Snapshot snapshot = new Snapshot(State.IDLE, "", "");
    private WebSocketClient socket;
    private String controllerId = "";
    private boolean started, closed;
    private int failures;

    public RelayConnection(URI endpoint, Consumer<String> report, Runnable disconnected) {
        Objects.requireNonNull(endpoint);
        boolean local = "127.0.0.1".equals(endpoint.getHost()) || "localhost".equals(endpoint.getHost());
        if (endpoint.getHost() == null || endpoint.getRawQuery() != null || endpoint.getFragment() != null
                || endpoint.getUserInfo() != null
                || !("wss".equals(endpoint.getScheme()) || local && "ws".equals(endpoint.getScheme()))) {
            throw new IllegalArgumentException("A secure WebSocket relay address is required");
        }
        this.endpoint = endpoint; this.report = Objects.requireNonNull(report);
        this.disconnected = Objects.requireNonNull(disconnected);
    }

    public void start() {
        synchronized (lock) {
            if (started || closed) return;
            started = true; snapshot = new Snapshot(State.CONNECTING, "", "");
            worker.execute(this::connect);
        }
    }

    private void connect() {
        synchronized (lock) {
            if (closed) return;
            controllerId = "";
            snapshot = new Snapshot(State.CONNECTING, "", "");
            WebSocketClient next = new WebSocketClient(endpoint) {
                @Override public void onOpen(ServerHandshake handshake) { }
                @Override public void onMessage(String message) { receive(this, message); }
                @Override public void onClose(int code, String reason, boolean remote) { lost(this); }
                @Override public void onError(Exception error) { lost(this); }
            };
            socket = next;
            next.setConnectionLostTimeout(20);
            try {
                next.connect();
                worker.schedule(() -> {
                    synchronized (lock) {
                        if (closed || socket != next || !controllerId.isEmpty()) return;
                    }
                    lost(next);
                }, 8, TimeUnit.SECONDS);
            } catch (RuntimeException error) { worker.execute(() -> lost(next)); }
        }
    }

    private void receive(WebSocketClient source, String message) {
        String delivered = null;
        boolean drop = false;
        try {
            var value = JsonParser.parseString(message);
            if (!value.isJsonObject()) return;
            JsonObject frame = value.getAsJsonObject();
            String type = text(frame, "type"), client = text(frame, "clientId");
            String peer = text(frame, "targetId"), body = text(frame, "message");
            synchronized (lock) {
                if (closed || source == null || socket != source) return;
                if ("bind".equals(type) && "targetId".equals(body) && peer.isEmpty()
                        && client.matches("[A-Za-z0-9-]{1,128}") && controllerId.isEmpty()) {
                    controllerId = client; failures = 0;
                    String base = endpoint.toString().replaceAll("/+$", "");
                    String qr = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#"
                            + URLEncoder.encode(base + "/" + controllerId, StandardCharsets.UTF_8);
                    snapshot = new Snapshot(State.WAITING_FOR_APP, qr, "");
                } else if ("bind".equals(type) && "200".equals(body)
                        && !controllerId.isEmpty() && controllerId.equals(client)
                        && peer.matches("[A-Za-z0-9-]{1,128}") && !peer.equals(client)
                        && (snapshot.peerId().isEmpty() || snapshot.peerId().equals(peer))) {
                    snapshot = new Snapshot(State.CONNECTED, snapshot.qrUrl(), peer);
                } else if ("msg".equals(type) && snapshot.state() == State.CONNECTED
                        && ((client.equals(controllerId) && peer.equals(snapshot.peerId()))
                            || (peer.equals(controllerId) && client.equals(snapshot.peerId())))) {
                    delivered = body;
                } else if ("error".equals(type) || "break".equals(type)
                        && client.equals(controllerId) && peer.equals(snapshot.peerId())) {
                    drop = true;
                }
            }
        } catch (RuntimeException malformed) { return; }
        // Never call game state while holding the transport lock (game tick sends in the reverse direction).
        if (delivered != null) report.accept(delivered);
        if (drop) lost(source);
    }

    private static String text(JsonObject frame, String key) {
        return frame.has(key) && frame.get(key).isJsonPrimitive() ? frame.get(key).getAsString() : "";
    }

    private void lost(WebSocketClient source) {
        synchronized (lock) {
            if (closed || source == null || socket != source) return;
            socket = null; controllerId = "";
            snapshot = new Snapshot(State.RETRYING, "", "");
            long delay = Math.min(15, 1L << Math.min(4, failures++));
            worker.schedule(this::connect, delay, TimeUnit.SECONDS);
        }
        source.close();
        disconnected.run();
    }

    public Snapshot snapshot() { return snapshot; }

    public boolean send(String command) {
        synchronized (lock) {
            if (closed || snapshot.state() != State.CONNECTED || socket == null || !socket.isOpen()) return false;
            JsonObject frame = new JsonObject();
            frame.addProperty("type", "msg"); frame.addProperty("clientId", controllerId);
            frame.addProperty("targetId", snapshot.peerId()); frame.addProperty("message", command);
            try { socket.send(frame.toString()); return true; }
            catch (RuntimeException failed) { WebSocketClient failedSocket = socket; worker.execute(() -> lost(failedSocket)); return false; }
        }
    }

    @Override public void close() {
        WebSocketClient previous;
        synchronized (lock) {
            if (closed) return;
            closed = true; previous = socket; socket = null; controllerId = "";
            snapshot = new Snapshot(State.STOPPED, "", ""); worker.shutdownNow();
        }
        if (previous != null) previous.close();
        disconnected.run();
    }
}

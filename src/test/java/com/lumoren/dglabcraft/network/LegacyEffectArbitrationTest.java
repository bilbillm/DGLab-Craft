package com.lumoren.dglabcraft.network;
import org.junit.Test;
import org.java_websocket.WebSocket;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.Assert.*;
public class LegacyEffectArbitrationTest {
    @Test public void damageOwnsChannelsAndRepeatPulseDoesNotClearTheQueue() throws Exception {
        final List<String> sent = new ArrayList<String>();
        Constructor<WebSocketServerManager> ctor = WebSocketServerManager.class.getDeclaredConstructor(); ctor.setAccessible(true);
        WebSocketServerManager ws = ctor.newInstance();
        WebSocket socket = (WebSocket) Proxy.newProxyInstance(WebSocket.class.getClassLoader(), new Class<?>[]{WebSocket.class}, (proxy, method, args) -> {
            if (method.getName().equals("send") && args[0] instanceof String) sent.add((String) args[0]);
            if (method.getReturnType() == boolean.class) return true;
            return null;
        });
        Field client = WebSocketServerManager.class.getDeclaredField("connectedClient"); client.setAccessible(true); client.set(ws, socket);
        ws.requestSyncedEffect(WebSocketServerManager.EffectSource.DAMAGE, "mob", "beat", 20, 30);
        assertTrue(sent.get(0).contains("clear-1")); assertTrue(sent.get(1).contains("clear-2"));
        assertTrue(sent.get(2).contains("pulse-A:"));
        assertTrue(sent.toString().contains("strength-1+2+20")); assertTrue(sent.toString().contains("strength-2+2+30"));
        int count = sent.size();
        ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "nether", "breath", 2, 3);
        assertEquals(count, sent.size());
        ws.requestSyncedEffect(WebSocketServerManager.EffectSource.DAMAGE, "mob", "beat", 21, 31);
        for (int i = count; i < sent.size(); i++) assertFalse(sent.get(i).contains("clear-"));
        assertTrue(sent.toString().contains("strength-1+2+21"));
    }
    @Test public void zeroEnvironmentMultiplierAndZeroCapStaySilent() throws Exception {
        final List<String> sent = new ArrayList<String>();
        Constructor<WebSocketServerManager> ctor = WebSocketServerManager.class.getDeclaredConstructor(); ctor.setAccessible(true);
        WebSocketServerManager ws = ctor.newInstance();
        WebSocket socket = (WebSocket) Proxy.newProxyInstance(WebSocket.class.getClassLoader(), new Class<?>[]{WebSocket.class}, (proxy, method, args) -> {
            if (method.getName().equals("send") && args[0] instanceof String) sent.add((String) args[0]);
            if (method.getReturnType() == boolean.class) return true;
            return null;
        });
        Field client = WebSocketServerManager.class.getDeclaredField("connectedClient"); client.setAccessible(true); client.set(ws, socket);
        Object handler = new com.lumoren.dglabcraft.events.EnvironmentHandler();
        Method send = handler.getClass().getDeclaredMethod("sendEnvironment", WebSocketServerManager.class, String.class, int.class, int.class, double.class, String.class);
        send.setAccessible(true);
        send.invoke(handler, ws, "breath", 100, 100, 0.0D, "disabled");
        send.invoke(handler, ws, "breath", 0, 0, 100.0D, "zero-cap");
        int strengths = 0;
        for (String packet : sent) if (packet.contains("strength-")) {
            strengths++;
            assertTrue(packet, packet.contains("+2+0"));
        }
        assertTrue("Expected real serialized strength packets", strengths > 0);
    }
}

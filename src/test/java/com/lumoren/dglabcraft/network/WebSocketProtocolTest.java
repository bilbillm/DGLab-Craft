package com.lumoren.dglabcraft.network;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSocketProtocolTest {
    @Test
    void formatsClearAndStrengthCommands() {
        assertEquals("clear-1", WebSocketProtocol.clearCommand(1));
        assertEquals("strength-2+2+35", WebSocketProtocol.strengthCommand(2, 35));
    }

    @Test
    void formatsPulseCommandWithQuotedUppercaseSixteenCharChunks() {
        assertEquals(
            "pulse-A:[\"00000000000000AB\",\"1234567890ABCDEF\",\"FFFFFFFFFFFFFFFF\"]",
            WebSocketProtocol.pulseCommand("a", List.of("ab", "1234567890abcdef", "ffffffffffffffffffff"))
        );
    }

    @Test
    void exposesEffectPriorityOrder() {
        assertEquals(3, WebSocketProtocol.priorityOf(WebSocketServerManager.EffectSource.DAMAGE));
        assertEquals(2, WebSocketProtocol.priorityOf(WebSocketServerManager.EffectSource.HEARTBEAT));
        assertEquals(1, WebSocketProtocol.priorityOf(WebSocketServerManager.EffectSource.ENVIRONMENT));
        assertEquals(-1, WebSocketProtocol.priorityOf(WebSocketServerManager.EffectSource.NONE));
    }

    @Test
    void computesLeaseTicksBySourceAndDetail() {
        assertEquals(55, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.HEARTBEAT, ""));
        assertEquals(40, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "portal"));
        assertEquals(55, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "nether"));
        assertEquals(45, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.ENVIRONMENT, "rain"));
        assertEquals(30, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "lava"));
        assertEquals(12, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.DAMAGE, "arrow"));
        assertEquals(0, WebSocketProtocol.leaseTicks(WebSocketServerManager.EffectSource.NONE, null));
    }
}

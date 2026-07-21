package com.lumoren.dglabcraft.network;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
            WebSocketProtocol.pulseCommand("a", Arrays.asList("ab", "1234567890abcdef", "ffffffffffffffffffff"))
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

    @Test
    void parsesDualChannelStrengthLimits() {
        assertEquals(new WebSocketProtocol.StrengthLimits(30, 10), WebSocketProtocol.parseStrengthLimits("strength-0+0+30+10", 100, 100));
    }

    @Test
    void parsesLegacySingleChannelStrengthLimits() {
        assertEquals(new WebSocketProtocol.StrengthLimits(50, 20), WebSocketProtocol.parseStrengthLimits("strength-1+2+50", 10, 20));
        assertEquals(new WebSocketProtocol.StrengthLimits(10, 75), WebSocketProtocol.parseStrengthLimits("strength-2+2+75", 10, 20));
    }

    @Test
    void keepsCurrentStrengthLimitsForInvalidMessages() {
        assertEquals(new WebSocketProtocol.StrengthLimits(10, 20), WebSocketProtocol.parseStrengthLimits("pulse-A:[]", 10, 20));
        assertEquals(new WebSocketProtocol.StrengthLimits(10, 20), WebSocketProtocol.parseStrengthLimits("strength-xyz", 10, 20));
        assertEquals(new WebSocketProtocol.StrengthLimits(10, 20), WebSocketProtocol.parseStrengthLimits(null, 10, 20));
    }
}

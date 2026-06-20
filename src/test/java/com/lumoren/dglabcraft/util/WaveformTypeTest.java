package com.lumoren.dglabcraft.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WaveformTypeTest {
    @Test
    void fromIdReturnsMatchingTypeCaseInsensitively() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId("pulse"));
        assertEquals(WaveformType.PULSE, WaveformType.fromId("PULSE"));
        assertEquals(WaveformType.SINE, WaveformType.fromId("sine"));
        assertEquals(WaveformType.CUSTOM, WaveformType.fromId("custom"));
    }

    @Test
    void fromIdFallsBackToPulse() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId(null));
        assertEquals(WaveformType.PULSE, WaveformType.fromId(""));
        assertEquals(WaveformType.PULSE, WaveformType.fromId("unknown"));
    }

    @Test
    void allTypesExposeStableIdAndDescription() {
        for (WaveformType type : WaveformType.values()) {
            assertNotNull(type.getWaveformId());
            assertFalse(type.getWaveformId().isEmpty());
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }
}

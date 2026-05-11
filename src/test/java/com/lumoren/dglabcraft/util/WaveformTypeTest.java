package com.lumoren.dglabcraft.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaveformTypeTest {

    @Test
    void fromId_returnsCorrectEnum() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId("pulse"));
        assertEquals(WaveformType.SINE, WaveformType.fromId("sine"));
        assertEquals(WaveformType.CUSTOM, WaveformType.fromId("custom"));
        assertEquals(WaveformType.VIBRATION, WaveformType.fromId("vibration"));
    }

    @Test
    void fromId_isCaseInsensitive() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId("PULSE"));
        assertEquals(WaveformType.PULSE, WaveformType.fromId("Pulse"));
        assertEquals(WaveformType.SINE, WaveformType.fromId("SINE"));
    }

    @Test
    void fromId_null_returnsPulse() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId(null));
    }

    @Test
    void fromId_unknown_returnsPulse() {
        assertEquals(WaveformType.PULSE, WaveformType.fromId("nonexistent"));
        assertEquals(WaveformType.PULSE, WaveformType.fromId(""));
        assertEquals(WaveformType.PULSE, WaveformType.fromId("random_garbage"));
    }

    @Test
    void getWaveformId_returnsCorrectId() {
        assertEquals("pulse", WaveformType.PULSE.getWaveformId());
        assertEquals("sine", WaveformType.SINE.getWaveformId());
        assertEquals("custom", WaveformType.CUSTOM.getWaveformId());
        assertEquals("vibration", WaveformType.VIBRATION.getWaveformId());
    }

    @Test
    void getDescription_notNull() {
        for (WaveformType type : WaveformType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
            assertNotNull(type.getWaveformId());
            assertFalse(type.getWaveformId().isEmpty());
        }
    }
}

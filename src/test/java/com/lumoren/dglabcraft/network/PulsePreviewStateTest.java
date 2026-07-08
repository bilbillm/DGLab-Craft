package com.lumoren.dglabcraft.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PulsePreviewStateTest {
    @Test
    void pulsePreviewIsActiveOnlyWithinLease() {
        PulsePreviewState state = new PulsePreviewState();

        state.record("fast_pinch", 1_000L, 600L);

        assertTrue(state.isActive(1_000L));
        assertTrue(state.isActive(1_599L));
        assertFalse(state.isActive(1_600L));
        assertEquals("fast_pinch", state.waveformId());
        assertEquals(0L, state.remainingMillis(1_700L));
    }

    @Test
    void invalidPulseClearsPreview() {
        PulsePreviewState state = new PulsePreviewState();

        state.record("beat", 1_000L, 600L);
        state.record("", 1_100L, 600L);

        assertFalse(state.isActive(1_100L));
        assertEquals("", state.waveformId());
    }

    @Test
    void nonPulseStatusesClearPreview() {
        PulsePreviewState state = new PulsePreviewState();

        state.record("beat", 1_000L, 600L);
        state.record("increase", 1_100L, 600L);

        assertFalse(state.isActive(1_100L));
        assertEquals("", state.waveformId());
    }

    @Test
    void clearStopsPreviewImmediately() {
        PulsePreviewState state = new PulsePreviewState();

        state.record("beat", 1_000L, 600L);
        state.clear();

        assertFalse(state.isActive(1_001L));
        assertEquals("", state.waveformId());
    }
}

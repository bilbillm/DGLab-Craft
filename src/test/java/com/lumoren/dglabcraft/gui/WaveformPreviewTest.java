package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaveformPreviewTest {
    @Test
    void parsesHexBytesAsClampedAmplitudes() {
        List<Integer> amplitudes = WaveformPreview.amplitudes(List.of("0a0a0a0a64646464", "ff0014"), 12);

        assertEquals(List.of(10, 10, 10, 10, 100, 100, 100, 100, 100, 0, 20), amplitudes);
    }

    @Test
    void ignoresMalformedBytesAndHonorsSampleLimit() {
        List<Integer> amplitudes = WaveformPreview.amplitudes(List.of("0aXX14", "6464"), 2);

        assertEquals(List.of(10, 20), amplitudes);
    }

    @Test
    void emptyInputReturnsEmptyPreview() {
        assertTrue(WaveformPreview.amplitudes(null, 8).isEmpty());
        assertTrue(WaveformPreview.amplitudes(List.of(), 8).isEmpty());
        assertTrue(WaveformPreview.amplitudes(List.of("0a"), 0).isEmpty());
    }
}

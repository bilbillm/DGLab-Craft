package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaveformPreviewTest {
    @Test
    void parsesProtocolWaveformStrengthBytesAsTwentyFiveMillisBars() {
        List<Integer> amplitudes = WaveformPreview.amplitudes(List.of("010203040A141E28", "64646464FF0014C8"), 12);

        assertEquals(List.of(10, 20, 30, 40, 100, 0, 20, 100), amplitudes);
    }

    @Test
    void ignoresFrequencyBytesAndMalformedStrengthBytes() {
        List<Integer> amplitudes = WaveformPreview.amplitudes(List.of("FFFFFFFF0AXX1400"), 2);

        assertEquals(List.of(10, 20), amplitudes);
    }

    @Test
    void emptyInputReturnsEmptyPreview() {
        assertTrue(WaveformPreview.amplitudes(null, 8).isEmpty());
        assertTrue(WaveformPreview.amplitudes(List.of(), 8).isEmpty());
        assertTrue(WaveformPreview.amplitudes(List.of("0a"), 0).isEmpty());
    }

    @Test
    void fallbackParsesShortChunksForRobustness() {
        assertEquals(List.of(10, 20), WaveformPreview.amplitudes(List.of("0aXX14"), 4));
    }

    @Test
    void buildsDglabStyleStrengthBarsFromNonZeroAmplitudes() {
        List<WaveformPreview.PulseBar> bars = WaveformPreview.pulseBars(List.of(0, 100, 0, 60, 4, 80), 120, 20, 0L);

        assertTrue(bars.size() >= 4);
        assertTrue(bars.stream().allMatch(bar -> bar.width() == 1));
        assertTrue(bars.stream().allMatch(bar -> bar.height() >= 2));
        assertTrue(bars.stream().allMatch(bar -> bar.alpha() >= 120 && bar.alpha() <= 255));
    }

    @Test
    void pulseBarsScrollHorizontallyOverTime() {
        List<Integer> samples = List.of(100, 100, 100, 100, 100, 100);

        List<WaveformPreview.PulseBar> first = WaveformPreview.pulseBars(samples, 120, 20, 0L);
        List<WaveformPreview.PulseBar> later = WaveformPreview.pulseBars(samples, 120, 20, 13L);

        assertTrue(later.get(0).x() < first.get(0).x());
    }

    @Test
    void eachStrengthBarRepresentsOneTenthSecondSample() {
        assertTrue(WaveformPreview.pulseBars(List.of(0), 1, 20, 0L).isEmpty());
        assertEquals(1, WaveformPreview.pulseBars(List.of(0, 100), 1, 20, 25L).size());
    }

    @Test
    void zeroStrengthKeepsTimelineMovingWithoutDrawingBars() {
        List<WaveformPreview.PulseBar> beforeZero = WaveformPreview.pulseBars(List.of(100), 1, 20, 0L);
        List<WaveformPreview.PulseBar> zeroSlot = WaveformPreview.pulseBars(List.of(100, 0), 1, 20, 25L);
        List<WaveformPreview.PulseBar> afterZero = WaveformPreview.pulseBars(List.of(100, 0, 100), 1, 20, 50L);

        assertEquals(1, beforeZero.size());
        assertTrue(zeroSlot.isEmpty());
        assertEquals(1, afterZero.size());
    }

    @Test
    void pulseBarsSkipIdleOrInvisibleInput() {
        assertTrue(WaveformPreview.pulseBars(null, 120, 20, 0L).isEmpty());
        assertTrue(WaveformPreview.pulseBars(List.of(), 120, 20, 0L).isEmpty());
        assertTrue(WaveformPreview.pulseBars(List.of(0, 0, 0), 120, 20, 0L).isEmpty());
        assertTrue(WaveformPreview.pulseBars(List.of(100), 0, 20, 0L).isEmpty());
        assertTrue(WaveformPreview.pulseBars(List.of(100), 120, 0, 0L).isEmpty());
    }
}

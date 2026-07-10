package com.lumoren.dglabcraft.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WaveformPreview {
    private static final int STRENGTH_BAR_WIDTH = 1;
    private static final int STRENGTH_BAR_GAP = 1;
    public static final long STRENGTH_BAR_MILLIS = 25L;

    private WaveformPreview() {
    }

    public static List<Integer> amplitudes(List<String> chunks, int maxSamples) {
        if (chunks == null || chunks.isEmpty() || maxSamples <= 0) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<Integer>();
        for (String chunk : chunks) {
            List<Integer> values = waveformStrengthsForChunk(chunk);
            for (Integer amplitude : values) {
                result.add(amplitude);
                if (result.size() >= maxSamples) {
                    break;
                }
            }
            if (result.size() >= maxSamples) {
                break;
            }
        }
        return result;
    }

    public static List<PulseBar> pulseBars(List<Integer> samples, int trackWidth, int trackHeight, long timeMillis) {
        if (samples == null || samples.isEmpty() || trackWidth <= 0 || trackHeight <= 0) {
            return Collections.emptyList();
        }
        int pitch = STRENGTH_BAR_WIDTH + STRENGTH_BAR_GAP;
        long safeTime = Math.max(0L, timeMillis);
        int phase = (int) ((safeTime % STRENGTH_BAR_MILLIS) * pitch / STRENGTH_BAR_MILLIS);
        int slots = trackWidth / pitch + 3;
        List<PulseBar> result = new ArrayList<PulseBar>();
        for (int slot = 0; slot < slots; slot++) {
            int x = trackWidth - STRENGTH_BAR_WIDTH - phase - slot * pitch;
            if (x < -STRENGTH_BAR_WIDTH || x > trackWidth) {
                continue;
            }
            int sampleIndex = samples.size() - 1 - slot;
            if (sampleIndex < 0) {
                break;
            }
            int amplitude = clamp(samples.get(sampleIndex), 0, 100);
            if (amplitude == 0) {
                continue;
            }
            int height = Math.max(2, amplitude * trackHeight / 100);
            int alpha = 120 + amplitude * 135 / 100;
            result.add(new PulseBar(x, STRENGTH_BAR_WIDTH, Math.min(trackHeight, height), Math.min(255, alpha)));
        }
        return result;
    }

    public static int sampleAt(List<Integer> samples, long elapsedMillis) {
        if (samples == null || samples.isEmpty()) {
            return 0;
        }
        int index = (int) ((Math.max(0L, elapsedMillis) / STRENGTH_BAR_MILLIS) % samples.size());
        return clamp(samples.get(index), 0, 100);
    }

    static List<Integer> waveformStrengthsForChunk(String chunk) {
        List<Integer> strengths = new ArrayList<Integer>();
        if (chunk == null) {
            return strengths;
        }
        String hex = chunk.trim();
        if (hex.length() >= 16) {
            appendByteValues(hex, 8, 16, strengths);
            return strengths;
        }
        appendByteValues(hex, 0, hex.length(), strengths);
        return strengths;
    }

    private static void appendByteValues(String hex, int startInclusive, int endExclusive, List<Integer> output) {
        int end = Math.min(hex.length(), endExclusive);
        for (int i = startInclusive; i + 2 <= end; i += 2) {
            try {
                output.add(clamp(Integer.parseInt(hex.substring(i, i + 2), 16), 0, 100));
            } catch (NumberFormatException ignored) {
                // Ignore malformed bytes.
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class PulseBar {
        private final int x;
        private final int width;
        private final int height;
        private final int alpha;

        PulseBar(int x, int width, int height, int alpha) {
            this.x = x;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
        }

        public int x() { return x; }
        public int width() { return width; }
        public int height() { return height; }
        public int alpha() { return alpha; }
    }
}

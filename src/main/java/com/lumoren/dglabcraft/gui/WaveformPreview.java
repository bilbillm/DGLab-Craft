package com.lumoren.dglabcraft.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WaveformPreview {
    private WaveformPreview() {
    }

    public static List<Integer> amplitudes(List<String> chunks, int maxSamples) {
        if (chunks == null || chunks.isEmpty() || maxSamples <= 0) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk == null) {
                continue;
            }
            String hex = chunk.trim();
            for (int i = 0; i + 2 <= hex.length() && result.size() < maxSamples; i += 2) {
                String byteHex = hex.substring(i, i + 2);
                try {
                    int value = Integer.parseInt(byteHex, 16);
                    result.add(Math.max(0, Math.min(100, value)));
                } catch (NumberFormatException ignored) {
                    // Skip malformed bytes while preserving valid samples in the same chunk.
                }
            }
            if (result.size() >= maxSamples) {
                break;
            }
        }
        return result;
    }
}

package com.lumoren.dglabcraft.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaveformManagerTest {

    @Test
    void mapsDamageSourcesToWaveformFilesWithFallbacks() {
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("cactus"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("explosion.player"));
        assertEquals("default", WaveformManager.getWaveformIdForDamage("unknown"));
    }

    @Test
    void parsesArrayAndObjectWaveformJson() {
        WaveformManager manager = WaveformManager.getInstance();
        Gson gson = new Gson();

        assertEquals(
            List.of("0A0A0A0A0A0A0A0A", "1414141414141414"),
            manager.parseWaveformJson("[\"0A0A0A0A0A0A0A0A\",\"1414141414141414\"]", gson)
        );
        assertEquals(
            List.of("1E1E1E1E1E1E1E1E"),
            manager.parseWaveformJson("{\"data\":[\"1E1E1E1E1E1E1E1E\"]}", gson)
        );
    }

    @Test
    void chunksWaveformListsByRequestedSize() {
        WaveformManager manager = WaveformManager.getInstance();

        assertEquals(
            List.of(List.of("a", "b"), List.of("c")),
            manager.chunkList(List.of("a", "b", "c"), 2)
        );
    }
}

package com.lumoren.dglabcraft.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import static org.junit.jupiter.api.Assertions.*;

class WaveformManagerTest {

    private WaveformManager manager;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        // 每次测试获取新的实例（singleton 在测试间可能共享状态）
        manager = WaveformManager.getInstance();
    }

    // ===== getWaveformIdForDamage =====

    @Test
    void getWaveformIdForDamage_fastPinch() {
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("cactus"));
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("arrow"));
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("trident"));
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("stalagmite"));
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("sweetberrybush"));
    }

    @Test
    void getWaveformIdForDamage_beat() {
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("fall"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("explosion"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("fireworks"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("mob"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("mob_attack"));
    }

    @Test
    void getWaveformIdForDamage_burn() {
        assertEquals("burn", WaveformManager.getWaveformIdForDamage("onfire"));
        assertEquals("burn", WaveformManager.getWaveformIdForDamage("infire"));
        assertEquals("burn", WaveformManager.getWaveformIdForDamage("lava"));
        assertEquals("burn", WaveformManager.getWaveformIdForDamage("hotfloor"));
    }

    @Test
    void getWaveformIdForDamage_compress() {
        assertEquals("compress", WaveformManager.getWaveformIdForDamage("inwall"));
        assertEquals("compress", WaveformManager.getWaveformIdForDamage("cramming"));
        assertEquals("compress", WaveformManager.getWaveformIdForDamage("falling_block"));
        assertEquals("compress", WaveformManager.getWaveformIdForDamage("anvil"));
    }

    @Test
    void getWaveformIdForDamage_drown() {
        assertEquals("drown", WaveformManager.getWaveformIdForDamage("drown"));
        assertEquals("drown", WaveformManager.getWaveformIdForDamage("drowning"));
        assertEquals("drown", WaveformManager.getWaveformIdForDamage("freeze"));
    }

    @Test
    void getWaveformIdForDamage_tide() {
        assertEquals("tide", WaveformManager.getWaveformIdForDamage("magic"));
        assertEquals("tide", WaveformManager.getWaveformIdForDamage("wither"));
        assertEquals("tide", WaveformManager.getWaveformIdForDamage("dragon_breath"));
        assertEquals("tide", WaveformManager.getWaveformIdForDamage("starve"));
        assertEquals("tide", WaveformManager.getWaveformIdForDamage("indirect_magic"));
    }

    @Test
    void getWaveformIdForDamage_compoundSource_usesBaseType() {
        // "explosion.player" should match "explosion" → "beat"
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("explosion.player"));
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("explosion.player.damage"));
    }

    @Test
    void getWaveformIdForDamage_caseInsensitive() {
        assertEquals("beat", WaveformManager.getWaveformIdForDamage("EXPLOSION"));
        assertEquals("burn", WaveformManager.getWaveformIdForDamage("LAVA"));
        assertEquals("fast_pinch", WaveformManager.getWaveformIdForDamage("CACTUS"));
    }

    @Test
    void getWaveformIdForDamage_unknown_returnsDefault() {
        assertEquals("default", WaveformManager.getWaveformIdForDamage("unknown_damage"));
        assertEquals("default", WaveformManager.getWaveformIdForDamage(""));
    }

    // ===== getWaveform / chunking =====

    @Test
    void getWaveform_defaultAlwaysAvailable() {
        // 即使未初始化，default 波形也应该可用
        java.util.List<String> waveform = manager.getWaveform("default");
        assertNotNull(waveform);
        assertFalse(waveform.isEmpty());
        assertEquals(3, waveform.size());
    }

    @Test
    void getWaveform_unknown_returnsDefault() {
        java.util.List<String> waveform = manager.getWaveform("nonexistent_waveform");
        assertNotNull(waveform);
        assertFalse(waveform.isEmpty());
    }

    @Test
    void getWaveformChunks_chunkSizeLargerThanData_returnsSingleChunk() {
        java.util.List<java.util.List<String>> chunks = manager.getWaveformChunks("default", 100);
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals(3, chunks.get(0).size());
    }

    @Test
    void getWaveformChunks_chunkSizeOne_returnsMultipleChunks() {
        java.util.List<java.util.List<String>> chunks = manager.getWaveformChunks("default", 1);
        assertNotNull(chunks);
        assertEquals(3, chunks.size());
        chunks.forEach(chunk -> assertEquals(1, chunk.size()));
    }

    // ===== parseWaveformJson =====

    @Test
    void parseWaveformJson_arrayFormat() {
        String json = "[\"0A0A0A0A0A0A0A0A\",\"1414141414141414\"]";
        java.util.List<String> result = manager.parseWaveformJson(json, gson);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("0A0A0A0A0A0A0A0A", result.get(0));
        assertEquals("1414141414141414", result.get(1));
    }

    @Test
    void parseWaveformJson_objectFormat() {
        String json = "{\"name_cn\":\"test\",\"frame_count\":2,\"data\":[\"AA00FF00BB00CC00\",\"1122334411223344\"]}";
        java.util.List<String> result = manager.parseWaveformJson(json, gson);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AA00FF00BB00CC00", result.get(0));
        assertEquals("1122334411223344", result.get(1));
    }

    @Test
    void parseWaveformJson_emptyArray() {
        java.util.List<String> result = manager.parseWaveformJson("[]", gson);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseWaveformJson_emptyObject() {
        java.util.List<String> result = manager.parseWaveformJson("{}", gson);
        assertNull(result);
    }

    @Test
    void parseWaveformJson_objectWithEmptyData() {
        java.util.List<String> result = manager.parseWaveformJson("{\"data\":[]}", gson);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseWaveformJson_objectWithNullData() {
        java.util.List<String> result = manager.parseWaveformJson("{\"data\":null}", gson);
        assertNull(result);
    }

    @Test
    void parseWaveformJson_notJson() {
        java.util.List<String> result = manager.parseWaveformJson("not json at all", gson);
        assertNull(result);
    }

    @Test
    void parseWaveformJson_singleElement() {
        java.util.List<String> result = manager.parseWaveformJson("[\"DEADBEEFDEADBEEF\"]", gson);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DEADBEEFDEADBEEF", result.get(0));
    }
}

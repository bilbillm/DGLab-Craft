package com.lumoren.dglabcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaveformManager {
    private static final Logger LOGGER = LogManager.getLogger("DGLabCraft-WaveformManager");
    private static WaveformManager instance;
    private static final List<String> DEFAULT_WAVEFORM = Arrays.asList(
        "0A0A0A0A0A0A0A0A",
        "1414141414141414",
        "1E1E1E1E1E1E1E1E"
    );
    private static final String[] WAVEFORM_FILES = {
        "burn", "drown", "beat", "compress", "fast_pinch",
        "tide", "heartbeat", "breath", "pinch_intensify",
        "rhythm_step", "grain_friction", "bounce_gradual",
        "wave_ripple", "rain_wash", "variable_speed",
        "signal_light", "tease1", "tease2"
    };

    private final Map<String, List<String>> waveformPool = new HashMap<String, List<String>>();
    private boolean initialized = false;

    private WaveformManager() {
    }

    public static WaveformManager getInstance() {
        if (instance == null) {
            instance = new WaveformManager();
        }
        return instance;
    }

    public List<String> getWaveform(String waveId) {
        if (!initialized) {
            initFromClasspath();
        }
        List<String> waveform = waveformPool.get(waveId);
        if (waveform == null || waveform.isEmpty()) {
            return waveformPool.containsKey("default") ? waveformPool.get("default") : DEFAULT_WAVEFORM;
        }
        return waveform;
    }

    private void initFromClasspath() {
        Gson gson = new Gson();
        for (String fileName : WAVEFORM_FILES) {
            String resourcePath = "assets/dglabcraft/waveforms/" + fileName + ".json";
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                LOGGER.warn("找不到波形资源: {}", resourcePath);
                continue;
            }
            try {
                String jsonContent = readUtf8(is);
                List<String> waveformData = parseWaveformJson(jsonContent, gson);
                if (waveformData != null && !waveformData.isEmpty()) {
                    waveformPool.put(fileName, waveformData);
                }
            } catch (Exception e) {
                LOGGER.error("加载波形文件失败: {} - {}", fileName, e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
        if (!waveformPool.containsKey("default")) {
            waveformPool.put("default", DEFAULT_WAVEFORM);
        }
        initialized = true;
        LOGGER.info("波形管理器初始化完成，共加载 {} 个波形", waveformPool.size());
    }

    private static String readUtf8(InputStream inputStream) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    List<String> parseWaveformJson(String jsonContent, Gson gson) {
        jsonContent = jsonContent.trim();
        if (jsonContent.startsWith("[")) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(jsonContent, listType);
        }
        if (jsonContent.startsWith("{")) {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = gson.fromJson(jsonContent, mapType);
            Object dataObj = map.get("data");
            if (dataObj instanceof List) {
                List<?> dataList = (List<?>) dataObj;
                List<String> result = new ArrayList<String>();
                for (Object item : dataList) {
                    result.add(String.valueOf(item));
                }
                return result;
            }
        }
        return null;
    }

    public List<List<String>> getWaveformChunks(String waveId, int chunkSize) {
        return chunkList(getWaveform(waveId), chunkSize);
    }

    List<List<String>> chunkList(List<String> list, int chunkSize) {
        List<List<String>> chunks = new ArrayList<List<String>>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }

    public static String getWaveformIdForDamage(String damageSourceId) {
        if (damageSourceId == null) {
            return "default";
        }
        String id = damageSourceId.toLowerCase();
        if (DAMAGE_WAVEFORM_MAP.containsKey(id)) {
            return DAMAGE_WAVEFORM_MAP.get(id);
        }
        if (id.contains(".")) {
            String baseType = id.split("\\.")[0];
            if (DAMAGE_WAVEFORM_MAP.containsKey(baseType)) {
                return DAMAGE_WAVEFORM_MAP.get(baseType);
            }
        }
        return "default";
    }

    private static final Map<String, String> DAMAGE_WAVEFORM_MAP = new HashMap<String, String>();

    static {
        DAMAGE_WAVEFORM_MAP.put("cactus", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("arrow", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("fall", "beat");
        DAMAGE_WAVEFORM_MAP.put("mob", "beat");
        DAMAGE_WAVEFORM_MAP.put("player", "beat");
        DAMAGE_WAVEFORM_MAP.put("explosion", "beat");
        DAMAGE_WAVEFORM_MAP.put("explosion.player", "beat");
        DAMAGE_WAVEFORM_MAP.put("fireworks", "beat");
        DAMAGE_WAVEFORM_MAP.put("onfire", "burn");
        DAMAGE_WAVEFORM_MAP.put("infire", "burn");
        DAMAGE_WAVEFORM_MAP.put("lava", "burn");
        DAMAGE_WAVEFORM_MAP.put("hotfloor", "burn");
        DAMAGE_WAVEFORM_MAP.put("inwall", "compress");
        DAMAGE_WAVEFORM_MAP.put("cramming", "compress");
        DAMAGE_WAVEFORM_MAP.put("fallingblock", "compress");
        DAMAGE_WAVEFORM_MAP.put("anvil", "compress");
        DAMAGE_WAVEFORM_MAP.put("drown", "drown");
        DAMAGE_WAVEFORM_MAP.put("magic", "tide");
        DAMAGE_WAVEFORM_MAP.put("wither", "tide");
        DAMAGE_WAVEFORM_MAP.put("dragonbreath", "tide");
        DAMAGE_WAVEFORM_MAP.put("starve", "tide");
        DAMAGE_WAVEFORM_MAP.put("indirectmagic", "tide");
    }
}

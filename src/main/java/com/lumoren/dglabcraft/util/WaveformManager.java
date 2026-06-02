package com.lumoren.dglabcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 波形管理器 - 数据驱动的波形加载系统
 * 从资源目录加载 JSON 波形文件
 */
public class WaveformManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("DGLabCraft-WaveformManager");
    private static WaveformManager instance;

    // 波形池: 文件名(不含后缀) -> Hex 字符串列表
    private Map<String, List<String>> waveformPool = new HashMap<>();

    // 默认波形 (兜底)
    private static final List<String> DEFAULT_WAVEFORM = Arrays.asList(
        "0A0A0A0A0A0A0A0A",
        "1414141414141414",
        "1E1E1E1E1E1E1E1E"
    );

    private boolean initialized = false;

    private WaveformManager() {}

    public static WaveformManager getInstance() {
        if (instance == null) {
            instance = new WaveformManager();
        }
        return instance;
    }

    /**
     * 初始化波形池 - 从资源目录加载
     */
    public void init(ResourceManager resourceManager) {
        if (initialized) {
            LOGGER.info("波形管理器已初始化，跳过");
            return;
        }

        LOGGER.info("开始初始化波形管理器...");

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>(){}.getType();

        // 遍历 waveforms 目录下的所有 JSON 文件
        Collection<ResourceLocation> resources = resourceManager.listResources("waveforms",
            path -> path.endsWith(".json"));

        LOGGER.info("找到 {} 个波形资源文件", resources.size());

        for (ResourceLocation path : resources) {
            try {
                String pathString = path.toString();
                LOGGER.info("处理波形文件: {}", pathString);

                // 提取文件名作为波形 ID (去除 .json 后缀)
                String fileName = path.getPath().replace("waveforms/", "").replace(".json", "");
                LOGGER.info("提取波形ID: {}", fileName);

                // 读取资源
                Resource resource = resourceManager.getResource(path);
                String jsonContent = new String(resource.getInputStream().readAllBytes());
                LOGGER.info("JSON内容: {}", jsonContent);
                List<String> waveformData = gson.fromJson(jsonContent, listType);

                if (waveformData != null && !waveformData.isEmpty()) {
                    waveformPool.put(fileName, waveformData);
                    LOGGER.info("加载波形: {} ({} 个数据块)", fileName, waveformData.size());
                }
            } catch (Exception e) {
                LOGGER.error("加载波形文件失败: {} - {}", path, e.getMessage());
            }
        }

        // 输出所有已加载的波形
        LOGGER.info("已加载波形列表: {}", waveformPool.keySet());

        // 确保默认波形存在
        if (!waveformPool.containsKey("default")) {
            waveformPool.put("default", DEFAULT_WAVEFORM);
        }

        initialized = true;
        LOGGER.info("波形管理器初始化完成, 共加载 {} 个波形", waveformPool.size());
    }

    /**
     * 获取波形数据
     * @param waveId 波形文件名(不含后缀)
     * @return 波形 Hex 字符串列表，找不到返回默认波形
     */
    public List<String> getWaveform(String waveId) {
        // 懒加载：如果未初始化，从 classpath 加载
        if (!initialized) {
            LOGGER.info("懒加载波形管理器...");
            initFromClasspath();
        }

        List<String> waveform = waveformPool.get(waveId);
        if (waveform == null || waveform.isEmpty()) {
            LOGGER.debug("找不到波形: {}，使用默认波形", waveId);
            return waveformPool.getOrDefault("default", DEFAULT_WAVEFORM);
        }
        return waveform;
    }

    /**
     * 从 classpath 直接加载波形文件
     */
    private void initFromClasspath() {
        LOGGER.info("从 classpath 加载波形文件...");

        Gson gson = new Gson();

        // 波形文件名列表
        String[] waveformFiles = {
            "burn", "drown", "beat", "compress", "fast_pinch",
            "tide", "heartbeat", "breath", "pinch_intensify",
            "rhythm_step", "grain_friction", "bounce_gradual",
            "wave_ripple", "rain_wash", "variable_speed",
            "signal_light", "tease1", "tease2"
        };

        for (String fileName : waveformFiles) {
            try {
                // 从 classpath 加载
                String resourcePath = "assets/dglabcraft/waveforms/" + fileName + ".json";
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);

                if (is != null) {
                    String jsonContent = new String(is.readAllBytes());
                    List<String> waveformData = parseWaveformJson(jsonContent, gson);

                    if (waveformData != null && !waveformData.isEmpty()) {
                        waveformPool.put(fileName, waveformData);
                        LOGGER.info("加载波形: {} ({} 个数据块)", fileName, waveformData.size());
                    }
                    is.close();
                } else {
                    LOGGER.warn("找不到资源: {}", resourcePath);
                }
            } catch (Exception e) {
                LOGGER.error("加载波形文件失败: {} - {}", fileName, e.getMessage());
            }
        }

        // 确保默认波形存在
        if (!waveformPool.containsKey("default")) {
            waveformPool.put("default", DEFAULT_WAVEFORM);
        }

        initialized = true;
        LOGGER.info("波形管理器初始化完成 (classpath), 共加载 {} 个波形", waveformPool.size());
    }

    /**
     * 解析波形 JSON，支持两种格式：
     * 1. 简单数组: ["0a64...", ...]
     * 2. 对象格式: { "data": ["0a64...", ...] }
     */
    private List<String> parseWaveformJson(String jsonContent, Gson gson) {
        jsonContent = jsonContent.trim();

        // 尝试解析为简单数组
        if (jsonContent.startsWith("[")) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(jsonContent, listType);
        }

        // 尝试解析为对象，从 data 字段提取
        if (jsonContent.startsWith("{")) {
            java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = gson.fromJson(jsonContent, mapType);

            if (map.containsKey("data")) {
                Object dataObj = map.get("data");
                if (dataObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> dataList = (List<Object>) dataObj;
                    List<String> result = new java.util.ArrayList<>();
                    for (Object item : dataList) {
                        result.add(item.toString());
                    }
                    return result;
                }
            }
        }

        LOGGER.warn("未知的 JSON 格式");
        return null;
    }

    /**
     * 获取波形并分块
     * @param waveId 波形文件名
     * @param chunkSize 每块最大元素数
     * @return 分块后的波形列表
     */
    public List<List<String>> getWaveformChunks(String waveId, int chunkSize) {
        List<String> waveform = getWaveform(waveId);
        return chunkList(waveform, chunkSize);
    }

    /**
     * 将列表分块
     */
    private List<List<String>> chunkList(List<String> list, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }

    /**
     * 获取伤害源对应的波形 ID
     */
    public static String getWaveformIdForDamage(String damageSourceId) {
        String id = damageSourceId.toLowerCase();

        // 先尝试完整匹配
        if (DAMAGE_WAVEFORM_MAP.containsKey(id)) {
            return DAMAGE_WAVEFORM_MAP.get(id);
        }

        // 处理复合伤害来源 (如 explosion.player -> explosion)
        if (id.contains(".")) {
            String baseType = id.split("\\.")[0];
            if (DAMAGE_WAVEFORM_MAP.containsKey(baseType)) {
                return DAMAGE_WAVEFORM_MAP.get(baseType);
            }
        }

        return "default";
    }

    /**
     * 伤害源 -> 波形文件 映射表
     */
    private static final Map<String, String> DAMAGE_WAVEFORM_MAP = new HashMap<>();

    static {
        // ===== 1. fast_pinch (锐器与穿刺) =====
        DAMAGE_WAVEFORM_MAP.put("cactus", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("sweetberrybush", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("arrow", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("trident", "fast_pinch");
        DAMAGE_WAVEFORM_MAP.put("stalagmite", "fast_pinch");

        // ===== 2. beat (钝器与撞击) ===== ok
        DAMAGE_WAVEFORM_MAP.put("fall", "beat");
        DAMAGE_WAVEFORM_MAP.put("mob_attack", "beat");
        DAMAGE_WAVEFORM_MAP.put("player_attack", "beat");
        DAMAGE_WAVEFORM_MAP.put("flyintowall", "beat");
        DAMAGE_WAVEFORM_MAP.put("explosion", "beat");
        DAMAGE_WAVEFORM_MAP.put("explosion.player", "beat");
        DAMAGE_WAVEFORM_MAP.put("fireworks", "beat");

        // ===== 3. burn (高温与持续灼烧) ===== ok
        DAMAGE_WAVEFORM_MAP.put("onfire", "burn");
        DAMAGE_WAVEFORM_MAP.put("infire", "burn");
        DAMAGE_WAVEFORM_MAP.put("lava", "burn");
        DAMAGE_WAVEFORM_MAP.put("hotfloor", "burn");

        // ===== 4. compress (挤压与窒息) ===== ok
        DAMAGE_WAVEFORM_MAP.put("inwall", "compress");
        DAMAGE_WAVEFORM_MAP.put("cramming", "compress");
        DAMAGE_WAVEFORM_MAP.put("falling_block", "compress");
        DAMAGE_WAVEFORM_MAP.put("anvil", "compress");

        // ===== 5. drown (环境异常与缺氧) ===== ok
        DAMAGE_WAVEFORM_MAP.put("drown", "drown");
        DAMAGE_WAVEFORM_MAP.put("drowning", "drown");
        DAMAGE_WAVEFORM_MAP.put("freeze", "drown");

        // ===== 6. tide (魔法与毒素) ===== ok
        DAMAGE_WAVEFORM_MAP.put("magic", "tide");
        DAMAGE_WAVEFORM_MAP.put("wither", "tide");
        DAMAGE_WAVEFORM_MAP.put("dragon_breath", "tide");
        DAMAGE_WAVEFORM_MAP.put("starve", "tide");

        // ===== 其他兼容映射 =====
        DAMAGE_WAVEFORM_MAP.put("mob", "beat");
        DAMAGE_WAVEFORM_MAP.put("mobattack", "beat");
        DAMAGE_WAVEFORM_MAP.put("mobAttack", "beat");
        DAMAGE_WAVEFORM_MAP.put("player_attack", "beat");
        DAMAGE_WAVEFORM_MAP.put("playerattack", "beat");
        DAMAGE_WAVEFORM_MAP.put("indirect_magic", "tide");
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        // 重新加载波形
        initialized = false;
        init(resourceManager);
    }
}

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
            return;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>(){}.getType();

        // 遍历 waveforms 目录下的所有 JSON 文件
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("assets/dglabcraft/waveforms",
            path -> path.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try {
                ResourceLocation path = entry.getKey();
                String pathString = path.toString();

                // 提取文件名作为波形 ID (去除 .json 后缀)
                String fileName = pathString.replace("assets/dglabcraft/waveforms/", "").replace(".json", "");

                // 读取资源
                Resource resource = entry.getValue();
                String jsonContent = new String(resource.open().readAllBytes());
                List<String> waveformData = gson.fromJson(jsonContent, listType);

                if (waveformData != null && !waveformData.isEmpty()) {
                    waveformPool.put(fileName, waveformData);
                    LOGGER.info("加载波形: {} ({} 个数据块)", fileName, waveformData.size());
                }
            } catch (Exception e) {
                LOGGER.error("加载波形文件失败: {} - {}", entry.getKey(), e.getMessage());
            }
        }

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
        if (!initialized) {
            LOGGER.warn("波形管理器未初始化，返回默认波形");
            return DEFAULT_WAVEFORM;
        }

        List<String> waveform = waveformPool.get(waveId);
        if (waveform == null || waveform.isEmpty()) {
            LOGGER.debug("找不到波形: {}，使用默认波形", waveId);
            return waveformPool.getOrDefault("default", DEFAULT_WAVEFORM);
        }
        return waveform;
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
        // 映射表: DamageSource msgId -> 波形文件名
        return DAMAGE_WAVEFORM_MAP.getOrDefault(damageSourceId.toLowerCase(), "default");
    }

    /**
     * 伤害源 -> 波形文件 映射表
     */
    private static final Map<String, String> DAMAGE_WAVEFORM_MAP = new HashMap<>();

    static {
        // 压缩/挤压
        DAMAGE_WAVEFORM_MAP.put("in_wall", "compress");

        // 仙人掌
        DAMAGE_WAVEFORM_MAP.put("cactus", "fast_pinch");

        // 火焰相关
        DAMAGE_WAVEFORM_MAP.put("on_fire", "burn");
        DAMAGE_WAVEFORM_MAP.put("in_fire", "burn");
        DAMAGE_WAVEFORM_MAP.put("lava", "burn");
        DAMAGE_WAVEFORM_MAP.put("hot_floor", "burn");
        DAMAGE_WAVEFORM_MAP.put("fireball", "burn");

        // 溺水
        DAMAGE_WAVEFORM_MAP.put("drown", "drown");
        DAMAGE_WAVEFORM_MAP.put("in_water", "drown");

        // 生物攻击
        DAMAGE_WAVEFORM_MAP.put("mob", "beat");
        DAMAGE_WAVEFORM_MAP.put("player_attack", "beat");
        DAMAGE_WAVEFORM_MAP.put("arrow", "beat");
        DAMAGE_WAVEFORM_MAP.put("trident", "beat");

        // 跌落
        DAMAGE_WAVEFORM_MAP.put("fall", "beat");
        DAMAGE_WAVEFORM_MAP.put("falling_block", "beat");

        // 中毒/魔法
        DAMAGE_WAVEFORM_MAP.put("poison", "beat");
        DAMAGE_WAVEFORM_MAP.put("wither", "beat");
        DAMAGE_WAVEFORM_MAP.put("magic", "beat");
        DAMAGE_WAVEFORM_MAP.put("indirect_magic", "beat");

        // 下界
        DAMAGE_WAVEFORM_MAP.put("wither_spawn", "beat");
        DAMAGE_WAVEFORM_MAP.put("dragon_breath", "beat");

        // 爆炸
        DAMAGE_WAVEFORM_MAP.put("explosion", "beat");
        DAMAGE_WAVEFORM_MAP.put("bad_respawn_point", "beat");

        // 灵魂沙/泥土
        DAMAGE_WAVEFORM_MAP.put("soul_sand", "drown");
        DAMAGE_WAVEFORM_MAP.put("soul_soil", "drown");

        // 甜蜜泥块
        DAMAGE_WAVEFORM_MAP.put("honey_block", "compress");

        // 末地传送门
        DAMAGE_WAVEFORM_MAP.put("end_portal", "compress");
        DAMAGE_WAVEFORM_MAP.put("end_gateway", "compress");

        // 下界传送门
        DAMAGE_WAVEFORM_MAP.put("nether_portal", "compress");
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        // 重新加载波形
        initialized = false;
        init(resourceManager);
    }
}

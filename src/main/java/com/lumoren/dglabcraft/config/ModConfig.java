package com.lumoren.dglabcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ForgeConfigSpec configSpec;

    /**
     * 显式保存配置到文件
     */
    public static void save() {
        if (configSpec != null) {
            configSpec.save();
            LOGGER.info("配置已保存到文件");
        }
    }

    /**
     * 设置配置规格引用（用于保存）
     */
    public static void setConfigSpec(ForgeConfigSpec spec) {
        configSpec = spec;
    }

    /**
     * 计算实际强度上限
     * 公式: appMaxStrength × MAX_INTENSITY_PERCENTAGE / 100
     */
    public static int getEffectiveMaxIntensity(int appMaxStrength) {
        int percentage = MAX_INTENSITY_PERCENTAGE.get().intValue();
        return appMaxStrength * percentage / 100;
    }

    // ========== 全局设置 ==========
    public static final ForgeConfigSpec.ConfigValue<Integer> BASE_MAX_INTENSITY; // 保留用于兼容，实际使用动态计算
    public static final ForgeConfigSpec.ConfigValue<Double> MAX_INTENSITY_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HUD_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUD_POSITION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SYNC_CHANNELS;

    // ========== WebSocket 设置 ==========
    public static final ForgeConfigSpec.ConfigValue<String> WS_HOST;
    public static final ForgeConfigSpec.ConfigValue<Integer> WS_PORT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WS_ENABLED;

    // ========== 锐器与穿刺 (fast_pinch) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> CACTUS_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SWEETBERRY_BUSH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> ARROW_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> TRIDENT_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> STALAGMITE_MULTIPLIER;

    // ========== 钝器与撞击 (beat) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> FALL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> MOB_ATTACK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> PLAYER_ATTACK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> FLY_INTO_WALL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> EXPLOSION_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> FIREWORKS_MULTIPLIER;

    // ========== 高温与灼烧 (burn) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> ON_FIRE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> IN_FIRE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> LAVA_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> HOT_FLOOR_MULTIPLIER;

    // ========== 挤压与窒息 (compress) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> IN_WALL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> CRAMMING_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> FALLING_BLOCK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> ANVIL_MULTIPLIER;

    // ========== 环境与缺氧 (drown) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> DROWN_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> FREEZE_MULTIPLIER;

    // ========== 魔法与毒素 (tide) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> MAGIC_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> WITHER_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> DRAGON_BREATH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> STARVE_MULTIPLIER;

    // ========== 环境维度 ==========
    public static final ForgeConfigSpec.ConfigValue<Double> NETHER_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> END_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> PORTAL_MULTIPLIER;

    // ========== 心跳设置 ==========
    public static final ForgeConfigSpec.ConfigValue<Double> HEARTBEAT_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> HEARTBEAT_MULTIPLIER;

    // ========== Buff 强度倍率 (保留) ==========
    public static final ForgeConfigSpec.ConfigValue<Double> BUFF_MULTIPLIER;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // ===== 全局设置 =====
        builder.push("general");
        BASE_MAX_INTENSITY = builder.comment("A/B 通道全局基础强度上限 (保留)")
                .define("baseMaxIntensity", 100);
        MAX_INTENSITY_PERCENTAGE = builder.comment("全局强度上限百分比 (0-100)")
                .define("maxIntensityPercentage", 100.0);
        HUD_ENABLED = builder.comment("是否显示HUD")
                .define("hudEnabled", true);
        HUD_POSITION = builder.comment("HUD位置 (0=左上, 1=右上, 2=左下, 3=右下)")
                .define("hudPosition", 0);
        SYNC_CHANNELS = builder.comment("A/B 通道同步")
                .define("syncChannels", true);
        builder.pop();

        // ===== WebSocket 配置 =====
        builder.push("websocket");
        WS_HOST = builder.comment("手动覆盖的局域网 IP 地址（留空时自动获取）")
                .define("host", "localhost");
        WS_PORT = builder.comment("WebSocket 服务器端口")
                .define("port", 8877);
        WS_ENABLED = builder.comment("是否启用 WebSocket 连接")
                .define("enabled", true);
        builder.pop();

        // ===== 锐器与穿刺 (fast_pinch) =====
        builder.push("fast_pinch");
        CACTUS_MULTIPLIER = builder.comment("仙人掌伤害倍率")
                .define("cactus", 1.0);
        SWEETBERRY_BUSH_MULTIPLIER = builder.comment("甜浆果丛伤害倍率")
                .define("sweetberrybush", 1.0);
        ARROW_MULTIPLIER = builder.comment("弓箭伤害倍率")
                .define("arrow", 1.0);
        TRIDENT_MULTIPLIER = builder.comment("三叉戟伤害倍率")
                .define("trident", 1.0);
        STALAGMITE_MULTIPLIER = builder.comment("钟乳石伤害倍率")
                .define("stalagmite", 1.0);
        builder.pop();

        // ===== 钝器与撞击 (beat) =====
        builder.push("beat");
        FALL_MULTIPLIER = builder.comment("跌落伤害倍率")
                .define("fall", 1.0);
        MOB_ATTACK_MULTIPLIER = builder.comment("生物攻击伤害倍率")
                .define("mobAttack", 1.0);
        PLAYER_ATTACK_MULTIPLIER = builder.comment("玩家攻击伤害倍率")
                .define("playerAttack", 1.0);
        FLY_INTO_WALL_MULTIPLIER = builder.comment("撞墙动能伤害倍率")
                .define("flyIntoWall", 1.0);
        EXPLOSION_MULTIPLIER = builder.comment("爆炸伤害倍率")
                .define("explosion", 1.0);
        FIREWORKS_MULTIPLIER = builder.comment("烟花伤害倍率")
                .define("fireworks", 1.0);
        builder.pop();

        // ===== 高温与灼烧 (burn) =====
        builder.push("burn");
        ON_FIRE_MULTIPLIER = builder.comment("着火伤害倍率")
                .define("onFire", 1.0);
        IN_FIRE_MULTIPLIER = builder.comment("火中伤害倍率")
                .define("inFire", 1.0);
        LAVA_MULTIPLIER = builder.comment("岩浆伤害倍率")
                .define("lava", 1.0);
        HOT_FLOOR_MULTIPLIER = builder.comment("岩浆块烫脚倍率")
                .define("hotFloor", 1.0);
        builder.pop();

        // ===== 挤压与窒息 (compress) =====
        builder.push("compress");
        IN_WALL_MULTIPLIER = builder.comment("墙内窒息倍率")
                .define("inWall", 1.0);
        CRAMMING_MULTIPLIER = builder.comment("实体挤压倍率")
                .define("cramming", 1.0);
        FALLING_BLOCK_MULTIPLIER = builder.comment("坠落方块倍率")
                .define("fallingBlock", 1.0);
        ANVIL_MULTIPLIER = builder.comment("铁砧砸击倍率")
                .define("anvil", 1.0);
        builder.pop();

        // ===== 环境与缺氧 (drown) =====
        builder.push("drown");
        DROWN_MULTIPLIER = builder.comment("溺水伤害倍率")
                .define("drown", 1.0);
        FREEZE_MULTIPLIER = builder.comment("细雪冰冻倍率")
                .define("freeze", 1.0);
        builder.pop();

        // ===== 魔法与毒素 (tide) =====
        builder.push("tide");
        MAGIC_MULTIPLIER = builder.comment("魔法伤害倍率")
                .define("magic", 1.0);
        WITHER_MULTIPLIER = builder.comment("凋零效果倍率")
                .define("wither", 1.0);
        DRAGON_BREATH_MULTIPLIER = builder.comment("龙息伤害倍率")
                .define("dragonBreath", 1.0);
        STARVE_MULTIPLIER = builder.comment("饥饿伤害倍率")
                .define("starve", 1.0);
        builder.pop();

        // ===== 环境维度 =====
        builder.push("environment");
        NETHER_MULTIPLIER = builder.comment("下界环境强度上限百分比 (0-100)")
                .define("nether", 20.0);
        END_MULTIPLIER = builder.comment("末地环境强度上限百分比 (0-100)")
                .define("end", 20.0);
        PORTAL_MULTIPLIER = builder.comment("传送门强度上限百分比 (0-100)")
                .define("portal", 20.0);
        builder.pop();

        // ===== 心跳设置 =====
        builder.push("heartbeat");
        HEARTBEAT_THRESHOLD = builder.comment("触发心跳的血量阈值 (生命值百分比 0-100)")
                .define("threshold", 30.0);
        HEARTBEAT_MULTIPLIER = builder.comment("心跳强度倍率")
                .define("multiplier", 1.0);
        builder.pop();

        // ===== Buff 强度 =====
        builder.push("buff");
        BUFF_MULTIPLIER = builder.comment("增益效果强度倍率")
                .define("buff", 1.0);
        builder.pop();

        SPEC = builder.build();
        setConfigSpec(SPEC);
    }
}

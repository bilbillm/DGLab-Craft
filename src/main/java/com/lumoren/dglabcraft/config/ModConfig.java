package com.lumoren.dglabcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    // 全局设置
    public static final ForgeConfigSpec.ConfigValue<Integer> BASE_MAX_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HUD_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUD_POSITION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SYNC_CHANNELS;

    // WebSocket 设置
    public static final ForgeConfigSpec.ConfigValue<String> WS_HOST;
    public static final ForgeConfigSpec.ConfigValue<Integer> WS_PORT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WS_ENABLED;

    // 伤害强度倍率
    public static final ForgeConfigSpec.ConfigValue<Double> FIRE_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Double> FALL_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Double> DROWN_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Double> POISON_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Double> WITHER_INTENSITY;

    // 环境强度倍率
    public static final ForgeConfigSpec.ConfigValue<Double> COLD_INTENSITY;
    public static final ForgeConfigSpec.ConfigValue<Double> NETHER_INTENSITY;

    // Buff 强度倍率
    public static final ForgeConfigSpec.ConfigValue<Double> BUFF_INTENSITY;

    // 心跳设置
    public static final ForgeConfigSpec.ConfigValue<Double> HEARTBEAT_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> HEARTBEAT_INTENSITY;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // 全局设置
        builder.push("general");
        BASE_MAX_INTENSITY = builder.comment("A/B 通道全局基础强度上限")
                .define("baseMaxIntensity", 100);
        HUD_ENABLED = builder.comment("是否显示HUD (0=关闭, 1=开启)")
                .define("hudEnabled", true);
        HUD_POSITION = builder.comment("HUD位置 (0=左上, 1=右上, 2=左下, 3=右下)")
                .define("hudPosition", 0);
        SYNC_CHANNELS = builder.comment("A/B 通道同步")
                .define("syncChannels", true);
        builder.pop();

        // WebSocket 配置分组
        builder.push("websocket");
        WS_HOST = builder.comment("WebSocket 服务器地址")
                .define("host", "localhost");
        WS_PORT = builder.comment("WebSocket 服务器端口")
                .define("port", 8877);
        WS_ENABLED = builder.comment("是否启用 WebSocket 连接")
                .define("enabled", true);
        builder.pop();

        // 伤害强度配置分组
        builder.push("damage_intensity");
        FIRE_INTENSITY = builder.comment("火焰/岩浆伤害强度倍率")
                .define("fire", 1.0);
        FALL_INTENSITY = builder.comment("跌落伤害强度倍率")
                .define("fall", 1.0);
        DROWN_INTENSITY = builder.comment("溺水伤害强度倍率")
                .define("drown", 1.0);
        POISON_INTENSITY = builder.comment("中毒伤害强度倍率")
                .define("poison", 1.0);
        WITHER_INTENSITY = builder.comment("凋零伤害强度倍率")
                .define("wither", 1.0);
        builder.pop();

        // 环境强度配置分组
        builder.push("environment_intensity");
        COLD_INTENSITY = builder.comment("寒冷环境强度倍率")
                .define("cold", 1.0);
        NETHER_INTENSITY = builder.comment("下界环境强度倍率")
                .define("nether", 1.0);
        builder.pop();

        // Buff 强度配置分组
        builder.push("buff_intensity");
        BUFF_INTENSITY = builder.comment("增益效果强度倍率")
                .define("buff", 1.0);
        builder.pop();

        // 心跳配置分组
        builder.push("heartbeat");
        HEARTBEAT_THRESHOLD = builder.comment("触发心跳的血量阈值 (格子数)")
                .define("threshold", 6.0);
        HEARTBEAT_INTENSITY = builder.comment("心跳强度倍率")
                .define("intensity", 1.0);
        builder.pop();

        SPEC = builder.build();
    }
}

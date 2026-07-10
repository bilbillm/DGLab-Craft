package com.lumoren.dglabcraft.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class ModConfig {
    private static Configuration configuration;

    public static final Value<Integer> BASE_MAX_INTENSITY = new Value<Integer>("general", "baseMaxIntensity", 100);
    public static final Value<Double> MAX_INTENSITY_PERCENTAGE = new Value<Double>("general", "maxIntensityPercentage", 100.0D);
    public static final Value<Boolean> HUD_ENABLED = new Value<Boolean>("general", "hudEnabled", true);
    public static final Value<Integer> HUD_POSITION = new Value<Integer>("general", "hudPosition", 0);
    public static final Value<Double> HUD_X_RATIO = new Value<Double>("general", "hudXRatio", -1.0D);
    public static final Value<Double> HUD_Y_RATIO = new Value<Double>("general", "hudYRatio", -1.0D);
    public static final Value<Double> HUD_SCALE = new Value<Double>("general", "hudScale", 1.0D);
    public static final Value<Boolean> WAVEFORM_OVERLAY_ENABLED = new Value<Boolean>("general", "waveformOverlayEnabled", true);
    public static final Value<Double> WAVEFORM_X_RATIO = new Value<Double>("general", "waveformXRatio", -1.0D);
    public static final Value<Double> WAVEFORM_Y_RATIO = new Value<Double>("general", "waveformYRatio", -1.0D);
    public static final Value<Double> WAVEFORM_SCALE = new Value<Double>("general", "waveformScale", 1.0D);
    public static final Value<Boolean> SYNC_CHANNELS = new Value<Boolean>("general", "syncChannels", true);

    public static final Value<String> WS_HOST = new Value<String>("websocket", "host", "localhost");
    public static final Value<Integer> WS_PORT = new Value<Integer>("websocket", "port", 8877);
    public static final Value<Boolean> WS_ENABLED = new Value<Boolean>("websocket", "enabled", true);

    public static final Value<Double> CACTUS_MULTIPLIER = new Value<Double>("fast_pinch", "cactus", 1.0D);
    public static final Value<Double> SWEETBERRY_BUSH_MULTIPLIER = new Value<Double>("fast_pinch", "sweetberrybush", 1.0D);
    public static final Value<Double> ARROW_MULTIPLIER = new Value<Double>("fast_pinch", "arrow", 1.0D);
    public static final Value<Double> TRIDENT_MULTIPLIER = new Value<Double>("fast_pinch", "trident", 1.0D);
    public static final Value<Double> STALAGMITE_MULTIPLIER = new Value<Double>("fast_pinch", "stalagmite", 1.0D);

    public static final Value<Double> FALL_MULTIPLIER = new Value<Double>("beat", "fall", 1.0D);
    public static final Value<Double> MOB_ATTACK_MULTIPLIER = new Value<Double>("beat", "mobAttack", 1.0D);
    public static final Value<Double> PLAYER_ATTACK_MULTIPLIER = new Value<Double>("beat", "playerAttack", 1.0D);
    public static final Value<Double> FLY_INTO_WALL_MULTIPLIER = new Value<Double>("beat", "flyIntoWall", 1.0D);
    public static final Value<Double> EXPLOSION_MULTIPLIER = new Value<Double>("beat", "explosion", 1.0D);
    public static final Value<Double> FIREWORKS_MULTIPLIER = new Value<Double>("beat", "fireworks", 1.0D);

    public static final Value<Double> ON_FIRE_MULTIPLIER = new Value<Double>("burn", "onFire", 1.0D);
    public static final Value<Double> IN_FIRE_MULTIPLIER = new Value<Double>("burn", "inFire", 1.0D);
    public static final Value<Double> LAVA_MULTIPLIER = new Value<Double>("burn", "lava", 1.0D);
    public static final Value<Double> HOT_FLOOR_MULTIPLIER = new Value<Double>("burn", "hotFloor", 1.0D);

    public static final Value<Double> IN_WALL_MULTIPLIER = new Value<Double>("compress", "inWall", 1.0D);
    public static final Value<Double> CRAMMING_MULTIPLIER = new Value<Double>("compress", "cramming", 1.0D);
    public static final Value<Double> FALLING_BLOCK_MULTIPLIER = new Value<Double>("compress", "fallingBlock", 1.0D);
    public static final Value<Double> ANVIL_MULTIPLIER = new Value<Double>("compress", "anvil", 1.0D);

    public static final Value<Double> DROWN_MULTIPLIER = new Value<Double>("drown", "drown", 1.0D);
    public static final Value<Double> FREEZE_MULTIPLIER = new Value<Double>("drown", "freeze", 1.0D);

    public static final Value<Double> MAGIC_MULTIPLIER = new Value<Double>("tide", "magic", 1.0D);
    public static final Value<Double> WITHER_MULTIPLIER = new Value<Double>("tide", "wither", 1.0D);
    public static final Value<Double> DRAGON_BREATH_MULTIPLIER = new Value<Double>("tide", "dragonBreath", 1.0D);
    public static final Value<Double> STARVE_MULTIPLIER = new Value<Double>("tide", "starve", 1.0D);

    public static final Value<Double> NETHER_MULTIPLIER = new Value<Double>("environment", "nether", 20.0D);
    public static final Value<Double> END_MULTIPLIER = new Value<Double>("environment", "end", 20.0D);
    public static final Value<Double> PORTAL_MULTIPLIER = new Value<Double>("environment", "portal", 20.0D);

    public static final Value<Double> HEARTBEAT_THRESHOLD = new Value<Double>("heartbeat", "threshold", 30.0D);
    public static final Value<Double> HEARTBEAT_MULTIPLIER = new Value<Double>("heartbeat", "multiplier", 1.0D);
    public static final Value<Double> BUFF_MULTIPLIER = new Value<Double>("buff", "buff", 1.0D);

    private ModConfig() {
    }

    public static void init(File file) {
        configuration = new Configuration(file);
        configuration.load();
        save();
    }

    public static void save() {
        if (configuration != null && configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static int getEffectiveMaxIntensity(int appMaxStrength) {
        int percentage = MAX_INTENSITY_PERCENTAGE.get().intValue();
        return appMaxStrength * percentage / 100;
    }

    public static final class Value<T> {
        private final String category;
        private final String key;
        private final T defaultValue;
        private T value;

        private Value(String category, String key, T defaultValue) {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public T get() {
            if (configuration == null) {
                return value;
            }
            if (defaultValue instanceof Boolean) {
                return cast(Boolean.valueOf(configuration.getBoolean(key, category, ((Boolean) defaultValue).booleanValue(), "")));
            }
            if (defaultValue instanceof Integer) {
                return cast(Integer.valueOf(configuration.getInt(key, category, ((Integer) defaultValue).intValue(), Integer.MIN_VALUE, Integer.MAX_VALUE, "")));
            }
            if (defaultValue instanceof Double) {
                return cast(Double.valueOf(configuration.getFloat(key, category, ((Double) defaultValue).floatValue(), -1000000.0F, 1000000.0F, "")));
            }
            if (defaultValue instanceof String) {
                return cast(configuration.getString(key, category, (String) defaultValue, ""));
            }
            return value;
        }

        public void set(T value) {
            this.value = value;
            if (configuration == null) {
                return;
            }
            if (value instanceof Boolean) {
                configuration.get(category, key, ((Boolean) defaultValue).booleanValue()).set(((Boolean) value).booleanValue());
            } else if (value instanceof Integer) {
                configuration.get(category, key, ((Integer) defaultValue).intValue()).set(((Integer) value).intValue());
            } else if (value instanceof Double) {
                configuration.get(category, key, ((Double) defaultValue).doubleValue()).set(((Double) value).doubleValue());
            } else if (value instanceof String) {
                configuration.get(category, key, (String) defaultValue).set((String) value);
            }
        }

        @SuppressWarnings("unchecked")
        private T cast(Object object) {
            this.value = (T) object;
            return value;
        }
    }
}

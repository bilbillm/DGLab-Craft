package com.lumoren.dglabcraft.config;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger("DGLabCraft-Config");
    private static final Properties PROPERTIES = new Properties();
    private static final List<ConfigValue<?>> VALUES = new ArrayList<>();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dglabcraft-fabric.properties");

    public static final ConfigValue<Integer> BASE_MAX_INTENSITY = define("general.baseMaxIntensity", 100, Integer::parseInt);
    public static final ConfigValue<Double> MAX_INTENSITY_PERCENTAGE = define("general.maxIntensityPercentage", 100.0, Double::parseDouble);
    public static final ConfigValue<Boolean> HUD_ENABLED = define("general.hudEnabled", true, Boolean::parseBoolean);
    public static final ConfigValue<Integer> HUD_POSITION = define("general.hudPosition", 0, Integer::parseInt);
    public static final ConfigValue<Double> HUD_X_RATIO = define("general.hudXRatio", -1.0, Double::parseDouble);
    public static final ConfigValue<Double> HUD_Y_RATIO = define("general.hudYRatio", -1.0, Double::parseDouble);
    public static final ConfigValue<Double> HUD_SCALE = define("general.hudScale", 1.0, Double::parseDouble);
    public static final ConfigValue<Boolean> WAVEFORM_OVERLAY_ENABLED = define("general.waveformOverlayEnabled", true, Boolean::parseBoolean);
    public static final ConfigValue<Double> WAVEFORM_X_RATIO = define("general.waveformXRatio", -1.0, Double::parseDouble);
    public static final ConfigValue<Double> WAVEFORM_Y_RATIO = define("general.waveformYRatio", -1.0, Double::parseDouble);
    public static final ConfigValue<Double> WAVEFORM_SCALE = define("general.waveformScale", 1.0, Double::parseDouble);
    public static final ConfigValue<Boolean> SYNC_CHANNELS = define("general.syncChannels", true, Boolean::parseBoolean);

    public static final ConfigValue<String> WS_HOST = define("websocket.host", "localhost", value -> value);
    public static final ConfigValue<Integer> WS_PORT = define("websocket.port", 8877, Integer::parseInt);
    public static final ConfigValue<Boolean> WS_ENABLED = define("websocket.enabled", true, Boolean::parseBoolean);

    public static final ConfigValue<Double> CACTUS_MULTIPLIER = define("fast_pinch.cactus", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> SWEETBERRY_BUSH_MULTIPLIER = define("fast_pinch.sweetberrybush", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> ARROW_MULTIPLIER = define("fast_pinch.arrow", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> TRIDENT_MULTIPLIER = define("fast_pinch.trident", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> STALAGMITE_MULTIPLIER = define("fast_pinch.stalagmite", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> FALL_MULTIPLIER = define("beat.fall", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> MOB_ATTACK_MULTIPLIER = define("beat.mobAttack", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> PLAYER_ATTACK_MULTIPLIER = define("beat.playerAttack", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> FLY_INTO_WALL_MULTIPLIER = define("beat.flyIntoWall", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> EXPLOSION_MULTIPLIER = define("beat.explosion", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> FIREWORKS_MULTIPLIER = define("beat.fireworks", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> ON_FIRE_MULTIPLIER = define("burn.onFire", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> IN_FIRE_MULTIPLIER = define("burn.inFire", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> LAVA_MULTIPLIER = define("burn.lava", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> HOT_FLOOR_MULTIPLIER = define("burn.hotFloor", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> IN_WALL_MULTIPLIER = define("compress.inWall", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> CRAMMING_MULTIPLIER = define("compress.cramming", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> FALLING_BLOCK_MULTIPLIER = define("compress.fallingBlock", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> ANVIL_MULTIPLIER = define("compress.anvil", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> DROWN_MULTIPLIER = define("drown.drown", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> FREEZE_MULTIPLIER = define("drown.freeze", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> MAGIC_MULTIPLIER = define("tide.magic", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> WITHER_MULTIPLIER = define("tide.wither", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> DRAGON_BREATH_MULTIPLIER = define("tide.dragonBreath", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> STARVE_MULTIPLIER = define("tide.starve", 1.0, Double::parseDouble);

    public static final ConfigValue<Double> NETHER_MULTIPLIER = define("environment.nether", 20.0, Double::parseDouble);
    public static final ConfigValue<Double> END_MULTIPLIER = define("environment.end", 20.0, Double::parseDouble);
    public static final ConfigValue<Double> PORTAL_MULTIPLIER = define("environment.portal", 20.0, Double::parseDouble);

    public static final ConfigValue<Double> HEARTBEAT_THRESHOLD = define("heartbeat.threshold", 30.0, Double::parseDouble);
    public static final ConfigValue<Double> HEARTBEAT_MULTIPLIER = define("heartbeat.multiplier", 1.0, Double::parseDouble);
    public static final ConfigValue<Double> BUFF_MULTIPLIER = define("buff.buff", 1.0, Double::parseDouble);

    static {
        load();
    }

    private ModConfig() {
    }

    public static void save() {
        for (ConfigValue<?> value : VALUES) {
            PROPERTIES.setProperty(value.key, String.valueOf(value.get()));
        }
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                PROPERTIES.store(output, "DGLab Craft Fabric config");
            }
        } catch (IOException e) {
            LOGGER.warn("Could not save config {}", CONFIG_PATH, e);
        }
    }

    public static int getEffectiveMaxIntensity(int appMaxStrength) {
        int percentage = MAX_INTENSITY_PERCENTAGE.get().intValue();
        return appMaxStrength * percentage / 100;
    }

    private static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                PROPERTIES.load(input);
            } catch (IOException e) {
                LOGGER.warn("Could not read config {}, using defaults", CONFIG_PATH, e);
            }
        }

        boolean changed = false;
        for (ConfigValue<?> value : VALUES) {
            changed |= value.load(PROPERTIES);
        }
        if (changed || !Files.exists(CONFIG_PATH)) {
            save();
        }
    }

    private static <T> ConfigValue<T> define(String key, T defaultValue, Parser<T> parser) {
        ConfigValue<T> value = new ConfigValue<>(key, defaultValue, parser);
        VALUES.add(value);
        return value;
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(String value);
    }

    public static final class ConfigValue<T> {
        private final String key;
        private final T defaultValue;
        private final Parser<T> parser;
        private T value;

        private ConfigValue(String key, T defaultValue, Parser<T> parser) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.parser = parser;
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value == null ? defaultValue : value;
        }

        private boolean load(Properties properties) {
            String raw = properties.getProperty(key);
            if (raw == null) {
                properties.setProperty(key, String.valueOf(defaultValue));
                value = defaultValue;
                return true;
            }
            try {
                value = parser.parse(raw);
                return false;
            } catch (RuntimeException e) {
                LOGGER.warn("Invalid config value {}={}, using default {}", key, raw, defaultValue);
                value = defaultValue;
                properties.setProperty(key, String.valueOf(defaultValue));
                return true;
            }
        }
    }
}

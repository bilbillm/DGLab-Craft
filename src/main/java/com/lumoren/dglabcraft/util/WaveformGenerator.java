package com.lumoren.dglabcraft.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 波形生成器
 * 参考 CaiJi-ikun/DG_LAB 算法实现
 * 将时间-强度对转换为 DG_Lab 协议的十六进制波形数据
 */
public class WaveformGenerator {

    /**
     * 伤害类型到官方波形 ID 的映射
     * 返回 DG-Lab 官方预设的波形 ID
     */
    public static String getOfficialWaveformId(String effectType) {
        // 映射伤害/效果类型到官方波形 ID
        WaveformType waveformType = getWaveformType(effectType);
        return waveformType.getWaveformId();
    }

    /**
     * 获取伤害类型对应的波形类型枚举
     */
    public static WaveformType getWaveformType(String effectType) {
        if (effectType == null) return WaveformType.PULSE;

        // 转换为小写进行匹配
        String type = effectType.toLowerCase();

        // 查找映射
        WaveformType waveformType = DAMAGE_WAVEFORM_MAP.get(type);
        if (waveformType != null) {
            return waveformType;
        }

        // 检查直接匹配
        try {
            return WaveformType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 未找到匹配的波形类型，使用默认
            return WaveformType.PULSE;
        }
    }

    /**
     * 伤害类型 -> 波形类型 映射表
     */
    private static final Map<String, WaveformType> DAMAGE_WAVEFORM_MAP = new HashMap<>();

    static {
        // 火焰相关 -> A通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("fire", WaveformType.ADAMAGE);
        DAMAGE_WAVEFORM_MAP.put("lava", WaveformType.ADAMAGE);
        DAMAGE_WAVEFORM_MAP.put("on_fire", WaveformType.ADAMAGE);
        DAMAGE_WAVEFORM_MAP.put("in_fire", WaveformType.ADAMAGE);

        // 跌落 -> A通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("fall", WaveformType.ADAMAGE);
        DAMAGE_WAVEFORM_MAP.put("fall_damage", WaveformType.ADAMAGE);

        // 仙人掌 -> A通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("cactus", WaveformType.ADAMAGE);

        // 溺水/水相关 -> B通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("drown", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("water", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("in_water", WaveformType.BDAMAGE);

        // 中毒/凋零 -> B通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("poison", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("wither", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("magic", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("indirect_magic", WaveformType.BDAMAGE);

        // 下界相关 -> B通道伤害波形
        DAMAGE_WAVEFORM_MAP.put("nether", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("wither_spawn", WaveformType.BDAMAGE);

        // 灵魂沙/土 -> B通道压迫波形
        DAMAGE_WAVEFORM_MAP.put("soul_sand", WaveformType.BDAMAGE);
        DAMAGE_WAVEFORM_MAP.put("soul_soil", WaveformType.BDAMAGE);

        // 传送门 -> 特殊波形
        DAMAGE_WAVEFORM_MAP.put("nether_portal", WaveformType.VIBRATION);
        DAMAGE_WAVEFORM_MAP.put("end_portal", WaveformType.VIBRATION);
        DAMAGE_WAVEFORM_MAP.put("end_gateway", WaveformType.VIBRATION);

        // 粘液块 -> 弹跳波形
        DAMAGE_WAVEFORM_MAP.put("slime", WaveformType.TAPPING);

        // 甜蜜泥块 -> 粘稠波形
        DAMAGE_WAVEFORM_MAP.put("honey", WaveformType.CONTINUOUS);

        // 寒冷 -> 振动波形
        DAMAGE_WAVEFORM_MAP.put("cold", WaveformType.VIBRATION);
        DAMAGE_WAVEFORM_MAP.put("snow", WaveformType.VIBRATION);

        // 心跳 -> 脉冲波形
        DAMAGE_WAVEFORM_MAP.put("heartbeat", WaveformType.PULSE);

        //  Buff/增益效果 -> 治疗波形
        DAMAGE_WAVEFORM_MAP.put("speed", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("strength", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("jump", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("regeneration", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("heal", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("health_boost", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("absorption", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("resistance", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("fire_resistance", WaveformType.AHEAL);
        DAMAGE_WAVEFORM_MAP.put("night_vision", WaveformType.AHEAL);

        // 通用波形名称映射
        DAMAGE_WAVEFORM_MAP.put("pulse", WaveformType.PULSE);
        DAMAGE_WAVEFORM_MAP.put("sine", WaveformType.SINE);
        DAMAGE_WAVEFORM_MAP.put("square", WaveformType.SQUARE);
        DAMAGE_WAVEFORM_MAP.put("triangle", WaveformType.TRIANGLE);
        DAMAGE_WAVEFORM_MAP.put("wave", WaveformType.WAVE);
        DAMAGE_WAVEFORM_MAP.put("tapping", WaveformType.TAPPING);
        DAMAGE_WAVEFORM_MAP.put("continuous", WaveformType.CONTINUOUS);
        DAMAGE_WAVEFORM_MAP.put("vibration", WaveformType.VIBRATION);
    }

    /**
     * 将时间-强度对转换为十六进制波形字符串 (JSON 数组格式)
     * 输入格式: "time,strength;time,strength;..."
     * 示例: "5,30;5,60" = 5单位强度30 → 5单位递进到强度60
     *
     * 输出格式: ["0A0A0A0A1E1E1E1E", "1E3C3C3C3C3C3C3C", ...]
     * 每个数组元素是 16 个十六进制字符
     */
    public static String textToWaveform(String text) {
        if (text == null || text.isEmpty()) {
            return "[\"0A0A0A0A0A0A0A0A\"]";
        }

        List<WaveformPair> pairs = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        int length = 0;

        // 解析 "time,strength;time,strength;..." 格式
        for (char ch : text.toCharArray()) {
            if (Character.isDigit(ch)) {
                number.append(ch);
            } else if (ch == ',') {
                WaveformPair pair = new WaveformPair();
                pair.time = Integer.parseInt(number.toString());
                number = new StringBuilder();
                pairs.add(pair);
            } else if (ch == ';') {
                pairs.get(pairs.size() - 1).strength = Integer.parseInt(number.toString());
                length += pairs.get(pairs.size() - 1).time + 1;
                number = new StringBuilder();
            }
        }

        // 处理最后一对
        if (pairs.size() > 0 && number.length() > 0) {
            pairs.get(pairs.size() - 1).strength = Integer.parseInt(number.toString());
            length += pairs.get(pairs.size() - 1).time + 1;
        }

        if (pairs.isEmpty()) {
            return "[\"0A0A0A0A0A0A0A0A\"]";
        }

        // 构建频率数组并进行线性插值
        double[] frequency = new double[length + 1];
        frequency[0] = 0;
        int j = 1;

        for (WaveformPair pair : pairs) {
            double max = pair.strength;
            double min = frequency[j - 1];
            double difference = (max - min) / (pair.time + 1);

            for (int i = 0; i <= pair.time; i++) {
                frequency[j] = frequency[j - 1] + difference;
                j++;
            }
        }

        // 转换为十六进制字符串 (每 8 个字符一组，每组作为 JSON 数组的一个元素)
        StringBuilder hexResult = new StringBuilder();
        hexResult.append("[");

        StringBuilder currentBlock = new StringBuilder();
        for (int i = 0; i <= length; i++) {
            // 每 8 个值用 0A0A0A0A 分隔
            if (i > 0 && i % 4 == 0) {
                currentBlock.append("0A0A0A0A");
            }

            // 将强度值转换为 2 位十六进制
            int value = Math.min(100, Math.max(0, (int) frequency[i]));
            currentBlock.append(String.format("%02X", value));

            // 每 16 个字符（8个值 + 分隔符）作为一个完整的块
            if (currentBlock.length() >= 16) {
                if (hexResult.length() > 1) {
                    hexResult.append(", ");
                }
                // 截取前 16 个字符
                String block = currentBlock.substring(0, 16);
                hexResult.append("\"").append(block).append("\"");
                // 保留剩余部分
                currentBlock = new StringBuilder(currentBlock.substring(16));
            }
        }

        // 处理剩余的不完整块
        if (currentBlock.length() > 0) {
            // 用 0 填充到 16 个字符
            while (currentBlock.length() < 16) {
                currentBlock.append("0");
            }
            if (hexResult.length() > 1) {
                hexResult.append(", ");
            }
            hexResult.append("\"").append(currentBlock.substring(0, 16)).append("\"");
        }

        hexResult.append("]");
        return hexResult.toString();
    }

    /**
     * 预定义波形 - 火焰/岩浆 (持续高频)
     * 模拟灼热感
     */
    public static String getFireWaveform() {
        // 持续高频波形
        return textToWaveform("10,80;10,100;10,80;10,100;10,80;10,100;10,80;10,100");
    }

    /**
     * 预定义波形 - 跌落伤害 (瞬间重击)
     * 模拟单次重击
     */
    public static String getFallWaveform() {
        // 瞬间重击波形
        return textToWaveform("2,100;2,0;2,100;2,0;2,100;2,0");
    }

    /**
     * 预定义波形 - 溺水 (缓慢增强)
     * 模拟水下压迫感
     */
    public static String getDrownWaveform() {
        // 缓慢增强波形
        return textToWaveform("20,20;20,40;20,60;20,80;20,100;20,80;20,60;20,40;20,20");
    }

    /**
     * 预定义波形 - 中毒/凋零 (节奏抽搐)
     * 模拟痉挛感
     */
    public static String getPoisonWaveform() {
        // 节奏抽搐波形
        return textToWaveform("5,30;5,80;5,30;5,80;5,30;5,80;5,30;5,80");
    }

    /**
     * 预定义波形 - 心跳 (双次跳动)
     * 模拟心跳节奏
     */
    public static String getHeartbeatWaveform() {
        // 双次跳动波形
        return textToWaveform("3,100;3,30;3,100;3,30;3,80;3,30;3,80;3,30");
    }

    /**
     * 预定义波形 - 速度 (轻微脉动)
     */
    public static String getSpeedWaveform() {
        return textToWaveform("10,40;10,60;10,40;10,60");
    }

    /**
     * 预定义波形 - 力量 (脉动)
     */
    public static String getStrengthWaveform() {
        return textToWaveform("8,50;8,70;8,50;8,70");
    }

    /**
     * 预定义波形 - 跳跃提升 (刺激)
     */
    public static String getJumpBoostWaveform() {
        return textToWaveform("5,60;5,90;5,60;5,90");
    }

    /**
     * 预定义波形 - 生命恢复 (平缓舒适)
     */
    public static String getRegenerationWaveform() {
        return textToWaveform("15,30;15,50;15,30;15,50");
    }

    /**
     * 预定义波形 - 抗性提升 (轻微振动)
     */
    public static String getResistanceWaveform() {
        return textToWaveform("20,25;20,35;20,25;20,35");
    }

    /**
     * 预定义波形 - 防火 (轻微温暖)
     */
    public static String getFireResistanceWaveform() {
        return textToWaveform("30,20;30,30;30,20;30,30");
    }

    /**
     * 预定义波形 - 夜视 (轻微脉动)
     */
    public static String getNightVisionWaveform() {
        return textToWaveform("25,25;25,35;25,25;25,35");
    }

    /**
     * 预定义波形 - 生命提升/吸收 (轻微脉动)
     */
    public static String getHealthBoostWaveform() {
        return textToWaveform("15,35;15,45;15,35;15,45");
    }

    /**
     * 预定义波形 - 仙人掌 (持续刺痛)
     */
    public static String getCactusWaveform() {
        return textToWaveform("3,80;3,100;3,80;3,100;3,80;3,100;3,80;3,100");
    }

    /**
     * 预定义波形 - 粘液块 (弹跳感)
     */
    public static String getSlimeWaveform() {
        return textToWaveform("5,60;5,90;5,60;5,90;5,60");
    }

    /**
     * 预定义波形 - 灵魂沙/土 (低沉压迫)
     */
    public static String getSoulSandWaveform() {
        return textToWaveform("15,60;15,80;15,60;15,80;15,60");
    }

    /**
     * 预定义波形 - 下界传送门 (空间扭曲)
     */
    public static String getNetherPortalWaveform() {
        return textToWaveform("8,50;8,80;8,50;8,80;8,50");
    }

    /**
     * 预定义波形 - 终界传送门 (空间传送)
     */
    public static String getEndPortalWaveform() {
        return textToWaveform("10,70;10,100;10,70;10,100;10,70");
    }

    /**
     * 预定义波形 - 甜蜜泥浆 (轻微粘稠)
     */
    public static String getHoneyBlockWaveform() {
        return textToWaveform("20,30;20,40;20,30;20,40");
    }

    /**
     * 预定义波形 - 寒冷 (高频细碎麻木)
     */
    public static String getColdWaveform() {
        return textToWaveform("5,50;5,70;5,50;5,70;5,50;5,70");
    }

    /**
     * 预定义波形 - 下界环境 (低频沉闷)
     */
    public static String getNetherWaveform() {
        return textToWaveform("20,60;20,80;20,60;20,80");
    }

    /**
     * 根据伤害/效果类型获取波形
     */
    public static String getWaveform(String effectType) {
        // 将常见的波形名称映射到效果类型
        String mappedType = mapWaveformNameToEffect(effectType);
        effectType = mappedType != null ? mappedType : effectType;

        switch (effectType.toLowerCase()) {
            case "fire":
            case "lava":
                return getFireWaveform();
            case "fall":
            case "fall_damage":
                return getFallWaveform();
            case "drown":
            case "water":
                return getDrownWaveform();
            case "poison":
            case "wither":
                return getPoisonWaveform();
            case "heartbeat":
                return getHeartbeatWaveform();
            case "speed":
                return getSpeedWaveform();
            case "strength":
                return getStrengthWaveform();
            case "jump":
                return getJumpBoostWaveform();
            case "regeneration":
            case "heal":
                return getRegenerationWaveform();
            case "resistance":
                return getResistanceWaveform();
            case "fire_resistance":
                return getFireResistanceWaveform();
            case "night_vision":
                return getNightVisionWaveform();
            case "health_boost":
            case "absorption":
                return getHealthBoostWaveform();
            case "cactus":
                return getCactusWaveform();
            case "slime":
                return getSlimeWaveform();
            case "soul_sand":
            case "soul_soil":
                return getSoulSandWaveform();
            case "nether_portal":
                return getNetherPortalWaveform();
            case "end_portal":
            case "end_gateway":
                return getEndPortalWaveform();
            case "honey":
                return getHoneyBlockWaveform();
            case "cold":
            case "snow":
                return getColdWaveform();
            case "nether":
                return getNetherWaveform();
            case "pulse":
                return getPulseWaveform();
            case "sine":
                return getSineWaveform();
            case "square":
                return getSquareWaveform();
            default:
                // 默认波形 - 使用通用脉动
                return textToWaveform("10,50;10,70;10,50");
        }
    }

    /**
     * 将波形名称映射到效果类型
     */
    private static String mapWaveformNameToEffect(String waveformName) {
        if (waveformName == null) return null;

        switch (waveformName.toLowerCase()) {
            case "pulse":
                // 脉动 - 使用通用脉动波形
                return "pulse";
            case "sine":
                // 正弦 - 使用平缓波形
                return "sine";
            case "square":
                // 方波 - 使用重击波形
                return "square";
            default:
                return null;
        }
    }

    /**
     * 获取通用脉动波形 (用于 "pulse" 类型)
     */
    public static String getPulseWaveform() {
        return textToWaveform("10,50;10,80;10,50;10,80");
    }

    /**
     * 获取正弦波形 (用于 "sine" 类型)
     */
    public static String getSineWaveform() {
        return textToWaveform("15,40;15,60;15,40;15,60");
    }

    /**
     * 获取方波波形 (用于 "square" 类型)
     */
    public static String getSquareWaveform() {
        return textToWaveform("5,100;5,0;5,100;5,0;5,100");
    }

    /**
     * 辅助类：时间-强度对
     */
    private static class WaveformPair {
        int time = 0;
        int strength = 0;
    }
}

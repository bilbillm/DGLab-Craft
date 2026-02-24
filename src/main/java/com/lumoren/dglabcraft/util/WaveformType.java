package com.lumoren.dglabcraft.util;

/**
 * DG-Lab 官方波形类型枚举
 * 包含官方预设的波形 ID
 */
public enum WaveformType {
    // 官方预设波形 ID
    ADAMAGE("ADamage", "A通道伤害波形"),
    BDAMAGE("BDamage", "B通道伤害波形"),
    AHEAL("AHeal", "A通道治疗波形"),
    BHEAL("BHeal", "B通道治疗波形"),
    CONTINUOUS("continuous", "持续波形"),
    PULSE("pulse", "脉冲波形"),
    TAPPING("tapping", "敲击波形"),
    WAVE("wave", "波浪波形"),
    VIBRATION("vibration", "振动波形"),
    SINE("sine", "正弦波形"),
    SQUARE("square", "方波波形"),
    TRIANGLE("triangle", "三角波形"),
    RAMP("ramp", "斜坡波形"),
    NOISE("noise", "噪声波形"),
    CUSTOM("custom", "自定义波形");

    private final String waveformId;
    private final String description;

    WaveformType(String waveformId, String description) {
        this.waveformId = waveformId;
        this.description = description;
    }

    /**
     * 获取波形 ID
     */
    public String getWaveformId() {
        return waveformId;
    }

    /**
     * 获取波形描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据波形 ID 获取枚举
     */
    public static WaveformType fromId(String id) {
        if (id == null) return PULSE;
        for (WaveformType type : values()) {
            if (type.waveformId.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return PULSE;
    }
}

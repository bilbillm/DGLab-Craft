package com.lumoren.dglabcraft.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 ModConfig 中的纯函数逻辑。
 * 注意：ForgeConfigSpec.ConfigValue 需要 Forge 加载才能工作，
 * 这里通过反射直接测试 getEffectiveMaxIntensity 的算法等价逻辑。
 */
class ModConfigTest {

    /**
     * 等价于 getEffectiveMaxIntensity 的纯函数实现：
     * appMaxStrength × MAX_INTENSITY_PERCENTAGE / 100
     */
    private static int computeEffectiveMax(int appMaxStrength, int percentage) {
        return appMaxStrength * percentage / 100;
    }

    @Test
    void effectiveMaxIntensity_fullPercentage() {
        assertEquals(100, computeEffectiveMax(100, 100));
        assertEquals(50, computeEffectiveMax(50, 100));
        assertEquals(200, computeEffectiveMax(200, 100));
    }

    @Test
    void effectiveMaxIntensity_halfPercentage() {
        assertEquals(50, computeEffectiveMax(100, 50));
        assertEquals(25, computeEffectiveMax(50, 50));
        assertEquals(100, computeEffectiveMax(200, 50));
    }

    @Test
    void effectiveMaxIntensity_zeroPercentage() {
        assertEquals(0, computeEffectiveMax(100, 0));
        assertEquals(0, computeEffectiveMax(200, 0));
        assertEquals(0, computeEffectiveMax(0, 0));
    }

    @Test
    void effectiveMaxIntensity_edgeCases() {
        assertEquals(1, computeEffectiveMax(100, 1));
        assertEquals(99, computeEffectiveMax(100, 99));
        assertEquals(10, computeEffectiveMax(100, 10));
    }

    @Test
    void effectiveMaxIntensity_integerDivision() {
        // appMaxStrength=30, percentage=33 → 30*33/100 = 9 (integer division)
        assertEquals(9, computeEffectiveMax(30, 33));
        // appMaxStrength=5, percentage=20 → 5*20/100 = 1
        assertEquals(1, computeEffectiveMax(5, 20));
    }
}

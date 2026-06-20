package com.lumoren.dglabcraft.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModConfigTest {
    private static int computeEffectiveMax(int appMaxStrength, int percentage) {
        return appMaxStrength * percentage / 100;
    }

    @Test
    void effectiveMaxIntensityUsesConfiguredPercentage() {
        assertEquals(100, computeEffectiveMax(100, 100));
        assertEquals(50, computeEffectiveMax(100, 50));
        assertEquals(0, computeEffectiveMax(100, 0));
        assertEquals(9, computeEffectiveMax(30, 33));
    }
}

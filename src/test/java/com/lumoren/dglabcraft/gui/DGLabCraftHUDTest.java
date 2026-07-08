package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DGLabCraftHUDTest {
    @Test
    void fallsBackToStatusWaveformWhenRuntimeStateIsUnavailable() {
        assertEquals("beat", DGLabCraftHUD.fallbackWaveformFromStatus(12.0D, "beat"));
    }

    @Test
    void fallbackStatusIgnoresIdleZeroAndRampOnlyStates() {
        assertEquals("", DGLabCraftHUD.fallbackWaveformFromStatus(0.0D, "beat"));
        assertEquals("", DGLabCraftHUD.fallbackWaveformFromStatus(12.0D, "Idle"));
        assertEquals("", DGLabCraftHUD.fallbackWaveformFromStatus(12.0D, "increase"));
        assertEquals("", DGLabCraftHUD.fallbackWaveformFromStatus(12.0D, "decrease"));
    }
}

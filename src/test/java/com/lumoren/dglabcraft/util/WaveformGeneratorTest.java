package com.lumoren.dglabcraft.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaveformGeneratorTest {

    @Test
    void mapsKnownDamageTypesToWaveformIds() {
        assertEquals("ADamage", WaveformGenerator.getOfficialWaveformId("lava"));
        assertEquals("BDamage", WaveformGenerator.getOfficialWaveformId("drown"));
        assertEquals("vibration", WaveformGenerator.getOfficialWaveformId("nether_portal"));
    }

    @Test
    void fallsBackToPulseForUnknownOrNullEffectTypes() {
        assertEquals("pulse", WaveformGenerator.getOfficialWaveformId("unknown_damage"));
        assertEquals("pulse", WaveformGenerator.getOfficialWaveformId(null));
    }
}

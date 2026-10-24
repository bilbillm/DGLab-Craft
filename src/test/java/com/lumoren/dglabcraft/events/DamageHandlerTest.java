package com.lumoren.dglabcraft.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageHandlerTest {

    private final DamageHandler handler = new DamageHandler();

    @Test
    void mapsVanillaMessageIdsToStableSourceIds() {
        assertEquals("mob", handler.normalizeDamageSourceId("mob"));
        assertEquals("mob", handler.normalizeDamageSourceId("mob_attack"));
        assertEquals("player", handler.normalizeDamageSourceId("player_attack"));
        assertEquals("sweetberrybush", handler.normalizeDamageSourceId("sweet_berry_bush"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hot_floor"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hotFloor"));
        assertEquals("onFire", handler.normalizeDamageSourceId("onfire"));
        assertEquals("inFire", handler.normalizeDamageSourceId("inFire"));
        assertEquals("fallingBlock", handler.normalizeDamageSourceId("falling_block"));
        assertEquals("dragonBreath", handler.normalizeDamageSourceId("dragon_breath"));
        assertEquals("flyIntoWall", handler.normalizeDamageSourceId("fly_into_wall"));
        assertEquals("magic", handler.normalizeDamageSourceId("indirect_magic"));
    }

    @Test
    void stripsVariantSuffixAndHandlesMissingIds() {
        assertEquals("explosion", handler.normalizeDamageSourceId("explosion.player"));
        assertEquals("mob", handler.normalizeDamageSourceId(null));
        assertEquals("mob", handler.normalizeDamageSourceId(""));
    }
}

package com.lumoren.dglabcraft.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageHandlerTest {
    private DamageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DamageHandler();
    }

    @Test
    void normalizeNullOrEmptyReturnsMob() {
        assertEquals("mob", handler.normalizeDamageSourceId(null));
        assertEquals("mob", handler.normalizeDamageSourceId(""));
    }

    @Test
    void normalizeUnderscoreNamesToRuntimeIds() {
        assertEquals("sweetberrybush", handler.normalizeDamageSourceId("sweet_berry_bush"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hot_floor"));
        assertEquals("inWall", handler.normalizeDamageSourceId("in_wall"));
        assertEquals("fallingBlock", handler.normalizeDamageSourceId("falling_block"));
        assertEquals("dragonBreath", handler.normalizeDamageSourceId("dragon_breath"));
        assertEquals("flyIntoWall", handler.normalizeDamageSourceId("fly_into_wall"));
    }

    @Test
    void normalizeAttackAndMagicAliases() {
        assertEquals("mob", handler.normalizeDamageSourceId("mob_attack"));
        assertEquals("mob", handler.normalizeDamageSourceId("mobattack"));
        assertEquals("player", handler.normalizeDamageSourceId("player_attack"));
        assertEquals("player", handler.normalizeDamageSourceId("playerattack"));
        assertEquals("magic", handler.normalizeDamageSourceId("indirect_magic"));
    }

    @Test
    void normalizeCompoundSourceStripsDotSuffixFirst() {
        assertEquals("explosion", handler.normalizeDamageSourceId("explosion.player"));
        assertEquals("mob", handler.normalizeDamageSourceId("mob.north"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hot_floor.player"));
    }

    @Test
    void normalizeKnownCollapsedNames() {
        assertEquals("onFire", handler.normalizeDamageSourceId("onfire"));
        assertEquals("inFire", handler.normalizeDamageSourceId("infire"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hotfloor"));
        assertEquals("fallingBlock", handler.normalizeDamageSourceId("fallingblock"));
    }

    @Test
    void normalizeDefaultLowercasePassthrough() {
        assertEquals("cactus", handler.normalizeDamageSourceId(" Cactus "));
        assertEquals("lava", handler.normalizeDamageSourceId("lava"));
        assertEquals("trident", handler.normalizeDamageSourceId("trident"));
    }
}

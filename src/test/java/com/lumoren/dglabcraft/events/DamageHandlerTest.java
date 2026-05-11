package com.lumoren.dglabcraft.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DamageHandlerTest {

    private DamageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DamageHandler();
    }

    // ===== normalizeDamageSourceId =====

    @Test
    void normalize_nullOrEmpty_returnsMob() {
        assertEquals("mob", handler.normalizeDamageSourceId(null));
        assertEquals("mob", handler.normalizeDamageSourceId(""));
    }

    @Test
    void normalize_underscoreToCamelCase() {
        assertEquals("sweetberrybush", handler.normalizeDamageSourceId("sweet_berry_bush"));
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hot_floor"));
        assertEquals("inWall", handler.normalizeDamageSourceId("in_wall"));
        assertEquals("fallingBlock", handler.normalizeDamageSourceId("falling_block"));
        assertEquals("dragonBreath", handler.normalizeDamageSourceId("dragon_breath"));
        assertEquals("flyIntoWall", handler.normalizeDamageSourceId("fly_into_wall"));
    }

    @Test
    void normalize_mobAttackAliases() {
        assertEquals("mob", handler.normalizeDamageSourceId("mob_attack"));
        assertEquals("mob", handler.normalizeDamageSourceId("mobattack"));
        assertEquals("mob", handler.normalizeDamageSourceId("mobAttack"));
    }

    @Test
    void normalize_playerAttackAliases() {
        assertEquals("player", handler.normalizeDamageSourceId("player_attack"));
        assertEquals("player", handler.normalizeDamageSourceId("playerattack"));
    }

    @Test
    void normalize_compoundSource_stripsDotSuffix() {
        // 带 . 的复合来源取第一部分
        assertEquals("explosion", handler.normalizeDamageSourceId("explosion.player"));
        assertEquals("mob", handler.normalizeDamageSourceId("mob.something"));
        assertEquals("player", handler.normalizeDamageSourceId("player.extra"));
    }

    @Test
    void normalize_cardinalDirectionSuffix() {
        // "mob.north" → strip dot → "mob"
        assertEquals("mob", handler.normalizeDamageSourceId("mob.north"));
        assertEquals("player", handler.normalizeDamageSourceId("player.south"));
    }

    @Test
    void normalize_indirectMagic() {
        assertEquals("magic", handler.normalizeDamageSourceId("indirect_magic"));
        assertEquals("magic", handler.normalizeDamageSourceId("indirectMagic"));
    }

    @Test
    void normalize_fireSources() {
        assertEquals("onFire", handler.normalizeDamageSourceId("onfire"));
        assertEquals("onFire", handler.normalizeDamageSourceId("onFire"));
        assertEquals("inFire", handler.normalizeDamageSourceId("infire"));
        assertEquals("inFire", handler.normalizeDamageSourceId("inFire"));
    }

    @Test
    void normalize_lowercaseIn() {
        assertEquals("inWall", handler.normalizeDamageSourceId("inwall"));
        assertEquals("inFire", handler.normalizeDamageSourceId("infire"));
        assertEquals("onFire", handler.normalizeDamageSourceId("onfire"));
    }

    @Test
    void normalize_crammingPreserved() {
        assertEquals("cramming", handler.normalizeDamageSourceId("cramming"));
    }

    @Test
    void normalize_defaultPassthrough() {
        assertEquals("cactus", handler.normalizeDamageSourceId("cactus"));
        assertEquals("fall", handler.normalizeDamageSourceId("fall"));
        assertEquals("arrow", handler.normalizeDamageSourceId("arrow"));
        assertEquals("lava", handler.normalizeDamageSourceId("lava"));
        assertEquals("magic", handler.normalizeDamageSourceId("magic"));
        assertEquals("wither", handler.normalizeDamageSourceId("wither"));
        assertEquals("starve", handler.normalizeDamageSourceId("starve"));
        assertEquals("anvil", handler.normalizeDamageSourceId("anvil"));
        assertEquals("freeze", handler.normalizeDamageSourceId("freeze"));
        assertEquals("trident", handler.normalizeDamageSourceId("trident"));
    }

    @Test
    void normalize_trimsWhitespace() {
        assertEquals("cactus", handler.normalizeDamageSourceId(" cactus "));
    }

    @Test
    void normalize_upperCase_passedThroughAfterUnderscoreMap() {
        assertEquals("cactus", handler.normalizeDamageSourceId("CACTUS"));
        assertEquals("cactus", handler.normalizeDamageSourceId("Cactus"));
    }

    // ===== edge cases =====

    @Test
    void normalize_dotThenUnderscore_compoundFirst() {
        // "hot_floor.player" → strip dot → "hot_floor" → camelCase → "hotFloor"
        assertEquals("hotFloor", handler.normalizeDamageSourceId("hot_floor.player"));
    }

    @Test
    void normalize_dragonBreath_dotSuffix() {
        assertEquals("dragonBreath", handler.normalizeDamageSourceId("dragon_breath.player"));
    }

    @Test
    void normalize_dotOnly_noPrefix_emptyAfterSplit() {
        // ".player" → split → [ "", "player" ] → first part "" → default passthrough → ""
        assertEquals("", handler.normalizeDamageSourceId(".player"));
    }
}

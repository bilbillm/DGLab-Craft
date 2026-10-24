package com.lumoren.dglabcraft.events;
import net.minecraft.client.Minecraft;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
class DamageSessionTest {
    @Test void leavingWorldDiscardsHealthAndSourceHistory() throws Exception {
        DamageHandler handler = new DamageHandler();
        field("lastHealth").setFloat(handler, 20.0f);
        field("lastDamageSourceId").set(handler, "lava");
        handler.updateSession(null, null);
        assertEquals(-1.0f, field("lastHealth").getFloat(handler));
        assertNull(field("lastDamageSourceId").get(handler));
    }
    @Test void switchingWorldOrRespawningStartsANewHealthBaseline() throws Exception {
        DamageHandler handler = new DamageHandler();
        Object player = new Object(), world = new Object();
        assertTrue(handler.updateSession(player, world));
        field("lastHealth").setFloat(handler, 20.0f);
        assertFalse(handler.updateSession(player, world));
        assertEquals(20.0f, field("lastHealth").getFloat(handler));
        assertTrue(handler.updateSession(player, new Object()));
        assertEquals(-1.0f, field("lastHealth").getFloat(handler));
        field("lastHealth").setFloat(handler, 8.0f);
        assertTrue(handler.updateSession(new Object(), world));
        assertEquals(-1.0f, field("lastHealth").getFloat(handler));
    }
    private Field field(String name) throws Exception {
        Field f = DamageHandler.class.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }
}

package com.lumoren.dglabcraft.events;
import org.junit.Test;
import static org.junit.Assert.*;
public class LegacyDamageHistoryTest {
    @Test public void firstObservationAndSessionChangesNeverCountAsDamage() {
        LegacyDamageHistory h = new LegacyDamageHistory(); Object p = new Object(), w = new Object();
        assertEquals(0, h.sample(p, w, 20), 0);
        assertEquals(4, h.sample(p, w, 16), 0);
        assertEquals(0, h.sample(null, null, 0), 0);
        assertEquals(0, h.sample(p, w, 8), 0);
        assertEquals(0, h.sample(new Object(), w, 4), 0);
        assertEquals(0, h.sample(p, new Object(), 2), 0);
    }
    @Test public void damageUsesHealthDeltaNotEventAvailabilityAndDebouncesBursts() {
        LegacyDamageHistory h = new LegacyDamageHistory(); Object p = new Object(), w = new Object();
        h.sample(p, w, 20); assertEquals(3, h.sample(p, w, 17), 0);
        assertEquals(0, h.sample(p, w, 16), 0);
        for (int i = 0; i < 5; i++) h.sample(p, w, 16);
        assertEquals(2, h.sample(p, w, 14), 0);
        assertEquals(0, h.sample(p, w, 20), 0);
    }
}

package com.lumoren.dglabcraft.events;
final class LegacyDamageHistory {
    private Object player, world;
    private float health = -1.0F;
    private int tick, lastTrigger = -6;
    boolean sameSession(Object p, Object w) { return p != null && w != null && p == player && w == world; }
    float sample(Object p, Object w, float current) {
        if (!sameSession(p, w)) { player = p; world = w; health = -1.0F; tick = 0; lastTrigger = -6; }
        if (p == null || w == null || Float.isNaN(current) || Float.isInfinite(current)) { health = -1.0F; return 0; }
        tick++;
        float delta = health < 0 ? 0 : health - current;
        health = current;
        if (delta <= 0.01F || tick - lastTrigger < 6) return 0;
        lastTrigger = tick;
        return delta;
    }
}

package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class HeartbeatHandler {
    private int tickCounter = 0;
    private boolean wasHeartbeatActive = false;
    private static float currentMaxHealth = 20.0F;

    public static float getCurrentMaxHealth() {
        return currentMaxHealth;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.player.world.isRemote) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !event.player.getUniqueID().equals(mc.player.getUniqueID())) {
            return;
        }
        EntityPlayer player = event.player;
        tickCounter++;
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        currentMaxHealth = maxHealth;
        int thresholdPercent = ModConfig.HEARTBEAT_THRESHOLD.get().intValue();
        int thresholdHealth = (int) Math.ceil(maxHealth * thresholdPercent / 100.0D);
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        int maxIntensityA = ModConfig.getEffectiveMaxIntensity(ws.getAppAMaxStrength());
        int maxIntensityB = ModConfig.getEffectiveMaxIntensity(ws.getAppBMaxStrength());

        if (health <= thresholdHealth && thresholdPercent > 0) {
            if (tickCounter % 40 == 0) {
                double percentage = 0.2D + (double) (thresholdHealth - health) / Math.max(1, thresholdHealth) * 0.8D;
                percentage = Math.max(0.2D, Math.min(1.0D, percentage));
                double multiplier = ModConfig.HEARTBEAT_MULTIPLIER.get();
                if (ModConfig.SYNC_CHANNELS.get()) {
                    int intensityA = clamp((int) (maxIntensityA * percentage * multiplier), 1, maxIntensityA);
                    int intensityB = clamp((int) (maxIntensityB * percentage * multiplier), 1, maxIntensityB);
                    ws.sendWaveformDataDualChannelWithDifferentIntensity("heartbeat", intensityA, intensityB);
                    ws.noteSyncRuntime(WebSocketServerManager.EffectSource.HEARTBEAT, "low_health", "heartbeat", Math.max(intensityA, intensityB));
                    FadeManager.updateHeartbeat(intensityA, intensityB);
                } else {
                    int intensity = clamp((int) (maxIntensityB * percentage * multiplier), 1, maxIntensityB);
                    ws.sendWaveformData("B", "heartbeat", intensity);
                    ws.noteChannelRuntime("B", WebSocketServerManager.EffectSource.HEARTBEAT, "low_health", "heartbeat", intensity);
                    FadeManager.updateHeartbeat(0, intensity);
                }
            }
            wasHeartbeatActive = true;
        } else if (wasHeartbeatActive) {
            FadeManager.stopHeartbeat();
            wasHeartbeatActive = false;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

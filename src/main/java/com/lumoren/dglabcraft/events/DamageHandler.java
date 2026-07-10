package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageHandler {
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(event.getEntityLiving() instanceof EntityPlayer) || mc.player == null || mc.world == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!player.getUniqueID().equals(mc.player.getUniqueID())) {
            return;
        }

        DamageSource source = event.getSource();
        String msgId = normalizeDamageType(source.getDamageType());
        float damage = event.getAmount();
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        String waveform = WaveformManager.getWaveformIdForDamage(msgId);
        double multiplier = getMultiplierForDamage(msgId);
        float healthRatio = damage / Math.max(1.0F, player.getMaxHealth());

        if (ModConfig.SYNC_CHANNELS.get()) {
            int effectiveMaxA = ModConfig.getEffectiveMaxIntensity(ws.getAppAMaxStrength());
            int effectiveMaxB = ModConfig.getEffectiveMaxIntensity(ws.getAppBMaxStrength());
            int strengthA = clamp((int) (effectiveMaxA * healthRatio * 2 * multiplier), 1, effectiveMaxA);
            int strengthB = clamp((int) (effectiveMaxB * healthRatio * 2 * multiplier), 1, effectiveMaxB);
            ws.sendWaveformDataDualChannelWithDifferentIntensity(waveform, strengthA, strengthB);
            ws.noteSyncRuntime(WebSocketServerManager.EffectSource.DAMAGE, msgId, waveform, Math.max(strengthA, strengthB));
            FadeManager.updateDamage(strengthA, strengthB);
        } else {
            int effectiveMax = ModConfig.getEffectiveMaxIntensity(ws.getAppAMaxStrength());
            int strength = clamp((int) (effectiveMax * healthRatio * 2 * multiplier), 1, effectiveMax);
            ws.sendWaveformData("A", waveform, strength);
            ws.noteChannelRuntime("A", WebSocketServerManager.EffectSource.DAMAGE, msgId, waveform, strength);
            FadeManager.updateDamage(strength, 0);
        }
    }

    private static String normalizeDamageType(String type) {
        if (type == null) {
            return "default";
        }
        return type.replace(".", "").replace("_", "").toLowerCase();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double getMultiplierForDamage(String msgId) {
        if ("cactus".equals(msgId)) return ModConfig.CACTUS_MULTIPLIER.get();
        if ("arrow".equals(msgId)) return ModConfig.ARROW_MULTIPLIER.get();
        if ("fall".equals(msgId)) return ModConfig.FALL_MULTIPLIER.get();
        if ("mob".equals(msgId)) return ModConfig.MOB_ATTACK_MULTIPLIER.get();
        if ("player".equals(msgId)) return ModConfig.PLAYER_ATTACK_MULTIPLIER.get();
        if ("flyintowall".equals(msgId)) return ModConfig.FLY_INTO_WALL_MULTIPLIER.get();
        if ("explosion".equals(msgId) || "explosionplayer".equals(msgId)) return ModConfig.EXPLOSION_MULTIPLIER.get();
        if ("fireworks".equals(msgId)) return ModConfig.FIREWORKS_MULTIPLIER.get();
        if ("onfire".equals(msgId)) return ModConfig.ON_FIRE_MULTIPLIER.get();
        if ("infire".equals(msgId)) return ModConfig.IN_FIRE_MULTIPLIER.get();
        if ("lava".equals(msgId)) return ModConfig.LAVA_MULTIPLIER.get();
        if ("hotfloor".equals(msgId)) return ModConfig.HOT_FLOOR_MULTIPLIER.get();
        if ("inwall".equals(msgId)) return ModConfig.IN_WALL_MULTIPLIER.get();
        if ("cramming".equals(msgId)) return ModConfig.CRAMMING_MULTIPLIER.get();
        if ("fallingblock".equals(msgId)) return ModConfig.FALLING_BLOCK_MULTIPLIER.get();
        if ("anvil".equals(msgId)) return ModConfig.ANVIL_MULTIPLIER.get();
        if ("drown".equals(msgId)) return ModConfig.DROWN_MULTIPLIER.get();
        if ("magic".equals(msgId) || "indirectmagic".equals(msgId)) return ModConfig.MAGIC_MULTIPLIER.get();
        if ("wither".equals(msgId)) return ModConfig.WITHER_MULTIPLIER.get();
        if ("dragonbreath".equals(msgId)) return ModConfig.DRAGON_BREATH_MULTIPLIER.get();
        if ("starve".equals(msgId)) return ModConfig.STARVE_MULTIPLIER.get();
        return 1.0D;
    }
}

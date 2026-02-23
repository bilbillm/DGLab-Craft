package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 伤害事件处理
 * 处理火焰、岩浆、跌落、溺水、中毒、凋零等伤害
 */
public class DamageHandler {

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();

        // 计算基础强度: 伤害值 / 最大生命值 * 基础阈值
        float maxHealth = player.getMaxHealth();
        float baseIntensity = (damage / maxHealth) * 0.8f; // 0.8 是基础阈值

        String waveType = "constant";
        String channel = "A";
        int duration = 1000;

        // 根据伤害来源确定波形类型
        if (source.isFire()) {
            // 火焰/岩浆 - 持续高频波形
            double intensity = baseIntensity * ModConfig.FIRE_INTENSITY.get();
            waveType = "pulse";
            duration = 2000;
            WebSocketManager.getInstance().sendStimulus(channel, waveType, intensity, duration);
        }
        else if (source == DamageSource.FALL) {
            // 跌落 - 瞬间重击型波形
            double intensity = baseIntensity * ModConfig.FALL_INTENSITY.get();
            waveType = "square";
            duration = 500;
            WebSocketManager.getInstance().sendStimulus(channel, waveType, intensity, duration);
        }
        else if (source == DamageSource.DROWN) {
            // 溺水 - 缓慢增强、有压迫感
            double intensity = baseIntensity * ModConfig.DROWN_INTENSITY.get();
            waveType = "sine";
            duration = 3000;
            WebSocketManager.getInstance().sendStimulus(channel, waveType, intensity, duration);
        }
        else if (source == DamageSource.WITHER) {
            // 凋零 - 间歇性抽搐
            double intensity = baseIntensity * ModConfig.WITHER_INTENSITY.get();
            waveType = "pulse";
            duration = 800;
            WebSocketManager.getInstance().sendStimulus(channel, waveType, intensity, duration);
        }
        else if (source.getMsgId().contains("poison")) {
            // 中毒 - 间歇性抽搐
            double intensity = baseIntensity * ModConfig.POISON_INTENSITY.get();
            waveType = "pulse";
            duration = 600;
            WebSocketManager.getInstance().sendStimulus(channel, waveType, intensity, duration);
        }
    }

    /**
     * 监听玩家是否在火中或岩浆中
     */
    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // 检查是否在火中
        if (player.isOnFire()) {
            float maxHealth = player.getMaxHealth();
            // 持续伤害 - 使用较低强度
            double intensity = 0.1 * ModConfig.FIRE_INTENSITY.get();
            WebSocketManager.getInstance().sendStimulus("A", "pulse", intensity, 200);
        }

        // 检查是否在水中/潜水
        if (player.isInWater() && player.getAirSupply() < 100) {
            double intensity = 0.15 * ModConfig.DROWN_INTENSITY.get();
            WebSocketManager.getInstance().sendStimulus("B", "sine", intensity, 200);
        }
    }
}

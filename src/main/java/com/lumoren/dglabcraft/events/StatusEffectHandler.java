package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/**
 * 状态效果处理
 * 参考 DG_LAB 算法实现
 * 处理增益效果 (Buffs) 如生命恢复、抗性提升等
 */
public class StatusEffectHandler {

    private int tickCounter = 0;

    // 正面效果列表
    private static final MobEffect REGENERATION = net.minecraft.world.effect.MobEffects.REGENERATION;
    private static final MobEffect RESISTANCE = net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE;
    private static final MobEffect HEAL = net.minecraft.world.effect.MobEffects.HEAL;
    private static final MobEffect HEALTH_BOOST = net.minecraft.world.effect.MobEffects.HEALTH_BOOST;
    private static final MobEffect ABSORPTION = net.minecraft.world.effect.MobEffects.ABSORPTION;
    private static final MobEffect SATURATION = net.minecraft.world.effect.MobEffects.SATURATION;
    private static final MobEffect GLOWING = net.minecraft.world.effect.MobEffects.GLOWING;
    private static final MobEffect FIRE_RESISTANCE = net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE;
    private static final MobEffect INVISIBILITY = net.minecraft.world.effect.MobEffects.INVISIBILITY;
    private static final MobEffect NIGHT_VISION = net.minecraft.world.effect.MobEffects.NIGHT_VISION;
    private static final MobEffect SPEED = net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED;
    private static final MobEffect JUMP_BOOST = net.minecraft.world.effect.MobEffects.JUMP;
    private static final MobEffect STRENGTH = net.minecraft.world.effect.MobEffects.DAMAGE_BOOST;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        Player player = (Player) event.getEntity();
        Collection<MobEffectInstance> effects = player.getActiveEffects();

        int maxIntensity = ModConfig.BASE_MAX_INTENSITY.get();

        // 检查是否有正面效果
        for (MobEffectInstance effect : effects) {
            MobEffect mobEffect = effect.getEffect();
            int amplifier = effect.getAmplifier();

            // 生命恢复 - 平缓舒适的按摩波形
            if (mobEffect == REGENERATION || mobEffect == HEAL) {
                int intensity = (int)((5 + amplifier * 3) * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 0);
            }
            // 抗性提升 - 轻微振动感
            else if (mobEffect == RESISTANCE) {
                int intensity = (int)(5.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 0);
            }
            // 力量 - 轻微脉动
            else if (mobEffect == STRENGTH) {
                int intensity = (int)(4.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
            }
            // 速度 - 轻微脉动
            else if (mobEffect == SPEED) {
                int intensity = (int)(3.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 0);
            }
            // 跳跃提升 - 轻微刺激
            else if (mobEffect == JUMP_BOOST) {
                int intensity = (int)(4.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
            }
            // 防火 - 轻微温暖感
            else if (mobEffect == FIRE_RESISTANCE) {
                int intensity = (int)(2.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("A", "sine", intensity, 0);
            }
            // 夜视 - 轻微脉动
            else if (mobEffect == NIGHT_VISION) {
                int intensity = (int)(2.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 0);
            }
            // 生命提升/吸收 - 轻微脉动
            else if (mobEffect == HEALTH_BOOST || mobEffect == ABSORPTION) {
                int intensity = (int)(3.0 * ModConfig.BUFF_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
            }
        }
    }
}

package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/**
 * 状态效果处理
 * 处理增益效果 (Buffs) 如生命恢复、抗性提升等
 */
public class StatusEffectHandler {

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

    // 负面效果 (已在 DamageHandler 中处理)
    // private static final MobEffect POISON = net.minecraft.world.effect.MobEffects.POISON;
    // private static final MobEffect WITHER = net.minecraft.world.effect.MobEffects.WITHER;
    // private static final MobEffect HUNGER = net.minecraft.world.effect.MobEffects.HUNGER;
    // private static final MobEffect BLINDNESS = net.minecraft.world.effect.MobEffects.BLINDNESS;
    // private static final MobEffect NAUSEA = net.minecraft.world.effect.MobEffects.HARM;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Collection<MobEffectInstance> effects = player.getActiveEffects();

        // 检查是否有正面效果
        for (MobEffectInstance effect : effects) {
            MobEffect mobEffect = effect.getEffect();

            // 生命恢复 - 平缓舒适的按摩波形
            if (mobEffect == REGENERATION || mobEffect == HEAL) {
                int amplifier = effect.getAmplifier();
                double intensity = (0.1 + amplifier * 0.05) * ModConfig.BUFF_INTENSITY.get();
                // 使用低强度持续波形
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 500);
            }
            // 抗性提升 - 轻微振动感
            else if (mobEffect == RESISTANCE) {
                double intensity = 0.08 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 300);
            }
            // 力量 - 轻微脉动
            else if (mobEffect == STRENGTH) {
                double intensity = 0.06 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 400);
            }
            // 速度 - 轻微脉动
            else if (mobEffect == SPEED) {
                double intensity = 0.05 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 200);
            }
            // 跳跃提升 - 轻微刺激
            else if (mobEffect == JUMP_BOOST) {
                double intensity = 0.07 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 300);
            }
            // 防火 - 轻微温暖感
            else if (mobEffect == FIRE_RESISTANCE) {
                double intensity = 0.04 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("A", "sine", intensity, 500);
            }
            // 夜视 - 轻微脉动
            else if (mobEffect == NIGHT_VISION) {
                double intensity = 0.03 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 300);
            }
            // 隐身 - 轻微脉动
            else if (mobEffect == INVISIBILITY) {
                double intensity = 0.03 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 300);
            }
            // 饱和 - 轻微舒适感
            else if (mobEffect == SATURATION) {
                double intensity = 0.04 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 200);
            }
            // 生命提升/吸收 - 轻微脉动
            else if (mobEffect == HEALTH_BOOST || mobEffect == ABSORPTION) {
                double intensity = 0.05 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 400);
            }
            // 发光 - 轻微脉动
            else if (mobEffect == GLOWING) {
                double intensity = 0.03 * ModConfig.BUFF_INTENSITY.get();
                WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 200);
            }
        }
    }
}

package com.lumoren.dglabcraft.events;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 状态效果处理
 *
 * 注意：药水效果触发的波形现在由 DamageHandler 的 LivingDamageEvent 处理
 * - 负面效果（凋零、中毒）造成伤害时会触发 LivingDamageEvent
 * - 这样可以实现：每受到一次伤害才发送一次波形，而不是每 tick 持续发送
 *
 * 此处理器目前保留用于未来可能的效果处理需求
 */
public class StatusEffectHandler {

    /**
     * 玩家 tick 事件
     * 当前已禁用持续触发逻辑，避免每 tick 都发送波形
     * 负面效果（凋零、中毒）伤害由 DamageHandler 处理
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 已禁用 - 效果伤害由 DamageHandler 的 LivingDamageEvent 处理
    }
}

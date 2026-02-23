package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 濒死心跳处理
 * 当玩家血量低于阈值时触发心跳模拟
 */
public class HeartbeatHandler {

    private int tickCounter = 0;
    private boolean isHeartbeatActive = false;
    private int heartbeatPhase = 0; // 0: off, 1: first beat, 2: pause

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // 只在客户端处理
        if (!player.level.isClientSide) return;

        tickCounter++;

        // 获取玩家当前血量
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int healthHalfHearts = (int) (health / 0.5f); // 转换为半心
        int threshold = ModConfig.HEARTBEAT_THRESHOLD.get().intValue();

        // 检查是否低于阈值 (默认 6 = 3 颗心)
        if (healthHalfHearts <= threshold * 2 && threshold > 0) {
            // 血量越低，心跳越快
            // 满血时 (1心) 约 1.5 秒一次
            // 极低血量时 (0.5心) 约 0.4 秒一次
            float healthRatio = health / maxHealth;
            int baseInterval = 30; // 基础间隔 (tick)
            int minInterval = 8;   // 最小间隔
            int interval = Math.max(minInterval, (int) (baseInterval * (healthRatio + 0.1f)));

            // 心跳模式: 双次跳动
            if (tickCounter % interval == 0) {
                if (heartbeatPhase == 0 || heartbeatPhase == 2) {
                    // 第一跳
                    heartbeatPhase = 1;
                    double intensity = ModConfig.HEARTBEAT_INTENSITY.get() * (1.0 - healthRatio + 0.3);
                    WebSocketServerManager.getInstance().sendStimulus("A", "pulse", Math.min(1.0, intensity), 100);
                }
            }

            if (tickCounter % interval == 5 && heartbeatPhase == 1) {
                // 第二跳 (比第一跳弱)
                heartbeatPhase = 2;
                double intensity = ModConfig.HEARTBEAT_INTENSITY.get() * (1.0 - healthRatio + 0.3) * 0.7;
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", Math.min(1.0, intensity), 80);
            }

            if (tickCounter % interval >= interval - 5) {
                heartbeatPhase = 0;
            }

            isHeartbeatActive = true;
        } else {
            // 血量恢复后停止心跳
            if (isHeartbeatActive) {
                WebSocketServerManager.getInstance().stopStimulus("A");
                isHeartbeatActive = false;
                heartbeatPhase = 0;
            }
        }
    }
}

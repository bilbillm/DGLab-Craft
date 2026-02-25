package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 濒死心跳处理
 * 参考 DG_LAB 算法实现
 * 当玩家血量低于阈值时触发心跳模拟
 */
public class HeartbeatHandler {

    private int tickCounter = 0;
    private boolean isHeartbeatActive = false;
    private int heartbeatPhase = 0; // 0: off, 1: first beat, 2: pause

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        Player player = (Player) event.getEntity();

        tickCounter++;

        // 获取玩家当前血量
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int healthHalfHearts = (int) (health / 0.5f); // 转换为半心
        int threshold = ModConfig.HEARTBEAT_THRESHOLD.get().intValue();

        // 获取 WebSocket 管理器
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // 计算 A/B 通道实际强度上限
        int appMaxStrengthA = ws.getAppAMaxStrength();
        int appMaxStrengthB = ws.getAppBMaxStrength();
        int maxIntensityA = ModConfig.getEffectiveMaxIntensity(appMaxStrengthA);
        int maxIntensityB = ModConfig.getEffectiveMaxIntensity(appMaxStrengthB);

        // 检查是否低于阈值 (默认 6 = 3 颗心)
        if (healthHalfHearts <= threshold * 2 && threshold > 0) {
            // 血量越低，心跳越快
            float healthRatio = health / maxHealth;
            int interval = 40; // 固定间隔 (2秒 = 40 ticks)

            // 心跳模式: 单次跳动
            if (tickCounter % interval == 0) {
                double baseIntensity = ModConfig.HEARTBEAT_MULTIPLIER.get() * 20; // 基础强度

                if (ModConfig.SYNC_CHANNELS.get()) {
                    // 同步模式：分别用 A/B 通道上限计算
                    int intensityA = (int)(baseIntensity * (1.0 - healthRatio + 0.3));
                    intensityA = Math.max(1, Math.min(intensityA, maxIntensityA));
                    int intensityB = (int)(baseIntensity * (1.0 - healthRatio + 0.3));
                    intensityB = Math.max(1, Math.min(intensityB, maxIntensityB));
                    ws.sendWaveformDataDualChannelWithDifferentIntensity("heartbeat", intensityA, intensityB);
                } else {
                    // 非同步模式：只用 B 通道
                    int intensity = (int)(baseIntensity * (1.0 - healthRatio + 0.3));
                    intensity = Math.max(1, Math.min(intensity, maxIntensityB));
                    ws.sendWaveformData("B", "heartbeat", intensity);
                }
            }

            isHeartbeatActive = true;
        } else {
            // 血量恢复后停止心跳 - 停止双通道
            if (isHeartbeatActive) {
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isHeartbeatActive = false;
                heartbeatPhase = 0;
            }
        }
    }
}

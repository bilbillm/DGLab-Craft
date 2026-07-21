package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 濒死心跳处理
 * 参考 DG_LAB 算法实现
 * 当玩家血量低于阈值时触发心跳模拟
 */
public class HeartbeatHandler {

    private int tickCounter = 0;
    private boolean wasHeartbeatActive = false;

    // 静态变量，存储当前玩家的最大生命值（供UI使用）
    private static float currentMaxHealth = 20.0f;

    /**
     * 获取当前玩家的最大生命值
     */
    public static float getCurrentMaxHealth() {
        return currentMaxHealth;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (!event.player.level.isClientSide()) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        PlayerEntity eventPlayer = event.player;
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        PlayerEntity player = event.player;

        tickCounter++;

        // 获取玩家当前血量
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        // 存储当前玩家的最大生命值，供UI使用
        currentMaxHealth = maxHealth;
        // 获取阈值百分比 (0-100)
        int thresholdPercent = ModConfig.HEARTBEAT_THRESHOLD.get().intValue();
        // 计算触发阈值：maxHealth * 百分比，向上取整
        int thresholdHealth = (int) Math.ceil(maxHealth * thresholdPercent / 100.0);

        // 获取 WebSocket 管理器
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // 计算 A/B 通道实际强度上限
        int appMaxStrengthA = ws.getAppAMaxStrength();
        int appMaxStrengthB = ws.getAppBMaxStrength();
        int maxIntensityA = ModConfig.getEffectiveMaxIntensity(appMaxStrengthA);
        int maxIntensityB = ModConfig.getEffectiveMaxIntensity(appMaxStrengthB);

        // 检查是否低于阈值 (百分比触发)
        if (health <= thresholdHealth && thresholdPercent > 0) {
            int interval = 40; // 固定间隔 (2秒 = 40 ticks)

            // 心跳模式: 单次跳动
            if (tickCounter % interval == 0) {
                // 计算百分比: 20% + (血量距离/阈值血量) * 80%
                // 血量=阈值时: 20%, 血量=0时: 100%
                double percentage = 0.2 + (double)(thresholdHealth - health) / thresholdHealth * 0.8;
                percentage = Math.max(0.2, Math.min(1.0, percentage));

                // 获取心跳倍率
                double multiplier = ModConfig.HEARTBEAT_MULTIPLIER.get();

                if (ModConfig.SYNC_CHANNELS.get()) {
                    // 同步模式：分别用 A/B 通道上限计算
                    int intensityA = (int)(maxIntensityA * percentage * multiplier);
                    intensityA = Math.max(1, Math.min(intensityA, maxIntensityA));
                    int intensityB = (int)(maxIntensityB * percentage * multiplier);
                    intensityB = Math.max(1, Math.min(intensityB, maxIntensityB));
                    ws.sendWaveformDataDualChannelWithDifferentIntensity("heartbeat", intensityA, intensityB);
                    // 更新 FadeManager
                    FadeManager.updateHeartbeat(intensityA, intensityB);
                } else {
                    // 非同步模式：只用 B 通道
                    int intensity = (int)(maxIntensityB * percentage * multiplier);
                    intensity = Math.max(1, Math.min(intensity, maxIntensityB));
                    ws.sendWaveformData("B", "heartbeat", intensity);
                    // 更新 FadeManager（B通道用intensity，A通道用0）
                    FadeManager.updateHeartbeat(0, intensity);
                }
            }

            wasHeartbeatActive = true;
        } else {
            // 血量恢复后通知 FadeManager 开始渐变
            if (wasHeartbeatActive) {
                FadeManager.stopHeartbeat();
                wasHeartbeatActive = false;
            }
        }
    }
}

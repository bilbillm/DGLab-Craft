package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 统一管理所有刺激的渐变消失效果
 * 当玩家退出某种状态后，延迟2秒后开始渐变，再用2秒时间将强度降为0
 */
@Mod.EventBusSubscriber
public class FadeManager {

    private static int tickCounter = 0;

    // 心跳状态
    private static int lastHeartbeatTick = 0;
    private static int lastHeartbeatIntensityA = 0;
    private static int lastHeartbeatIntensityB = 0;
    private static boolean isHeartbeatActive = false;
    private static boolean isHeartbeatFading = false;

    // 环境状态
    private static int lastEnvironmentTick = 0;
    private static int lastEnvironmentIntensityA = 0;
    private static int lastEnvironmentIntensityB = 0;
    private static boolean isEnvironmentActive = false;
    private static boolean isEnvironmentFading = false;

    // 伤害状态
    private static int lastDamageTick = 0;
    private static int lastDamageIntensityA = 0;
    private static int lastDamageIntensityB = 0;
    private static boolean isDamageActive = false;
    private static boolean isDamageFading = false;

    // 延迟时间（2秒 = 40 tick）
    private static final int FADE_DELAY = 40;
    // 渐变时间（2秒 = 40 tick）
    private static final int FADE_DURATION = 40;

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        tickCounter++;

        // 更新每种状态的渐变
        updateHeartbeatFade();
        updateEnvironmentFade();
        updateDamageFade();
    }

    /**
     * 更新心跳渐变
     */
    private static void updateHeartbeatFade() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        if (!isHeartbeatActive && !isHeartbeatFading) {
            return;
        }

        int elapsed = tickCounter - lastHeartbeatTick;

        if (isHeartbeatActive) {
            // 状态激活中，保持强度
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isHeartbeatActive = false;
                isHeartbeatFading = false;
            } else if (elapsed >= FADE_DELAY) {
                // 开始渐变
                isHeartbeatFading = true;
                isHeartbeatActive = false;
                sendHeartbeatFade(ws, elapsed);
            }
        } else if (isHeartbeatFading) {
            // 正在渐变
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isHeartbeatFading = false;
            } else {
                sendHeartbeatFade(ws, elapsed);
            }
        }
    }

    /**
     * 发送心跳渐变强度
     */
    private static void sendHeartbeatFade(WebSocketServerManager ws, int elapsed) {
        int fadeProgress = elapsed - FADE_DELAY;
        double fadeFactor = 1.0 - (double) fadeProgress / FADE_DURATION;

        int intensityA = (int)(lastHeartbeatIntensityA * fadeFactor);
        int intensityB = (int)(lastHeartbeatIntensityB * fadeFactor);

        if (intensityA > 0 || intensityB > 0) {
            if (ModConfig.SYNC_CHANNELS.get()) {
                ws.sendWaveformDataDualChannelWithDifferentIntensity("heartbeat", intensityA, intensityB);
            } else {
                ws.sendWaveformData("B", "heartbeat", intensityB);
            }
        }
    }

    /**
     * 更新环境渐变
     */
    private static void updateEnvironmentFade() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        if (!isEnvironmentActive && !isEnvironmentFading) {
            return;
        }

        int elapsed = tickCounter - lastEnvironmentTick;

        if (isEnvironmentActive) {
            // 状态激活中，保持强度
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isEnvironmentActive = false;
                isEnvironmentFading = false;
            } else if (elapsed >= FADE_DELAY) {
                // 开始渐变
                isEnvironmentFading = true;
                isEnvironmentActive = false;
                sendEnvironmentFade(ws, elapsed);
            }
        } else if (isEnvironmentFading) {
            // 正在渐变
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isEnvironmentFading = false;
            } else {
                sendEnvironmentFade(ws, elapsed);
            }
        }
    }

    /**
     * 发送环境渐变强度
     */
    private static void sendEnvironmentFade(WebSocketServerManager ws, int elapsed) {
        int fadeProgress = elapsed - FADE_DELAY;
        double fadeFactor = 1.0 - (double) fadeProgress / FADE_DURATION;

        int intensityA = (int)(lastEnvironmentIntensityA * fadeFactor);
        int intensityB = (int)(lastEnvironmentIntensityB * fadeFactor);

        if (intensityA > 0 || intensityB > 0) {
            if (ModConfig.SYNC_CHANNELS.get()) {
                ws.sendWaveformDataDualChannelWithDifferentIntensity("environment", intensityA, intensityB);
            } else {
                ws.sendWaveformData("B", "environment", intensityB);
            }
        }
    }

    /**
     * 更新伤害渐变
     */
    private static void updateDamageFade() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        if (!isDamageActive && !isDamageFading) {
            return;
        }

        int elapsed = tickCounter - lastDamageTick;

        if (isDamageActive) {
            // 状态激活中，保持强度
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isDamageActive = false;
                isDamageFading = false;
            } else if (elapsed >= FADE_DELAY) {
                // 开始渐变
                isDamageFading = true;
                isDamageActive = false;
                sendDamageFade(ws, elapsed);
            }
        } else if (isDamageFading) {
            // 正在渐变
            if (elapsed >= FADE_DELAY + FADE_DURATION) {
                // 渐变结束，停止刺激
                ws.stopStimulus("A");
                ws.stopStimulus("B");
                isDamageFading = false;
            } else {
                sendDamageFade(ws, elapsed);
            }
        }
    }

    /**
     * 发送伤害渐变强度
     */
    private static void sendDamageFade(WebSocketServerManager ws, int elapsed) {
        int fadeProgress = elapsed - FADE_DELAY;
        double fadeFactor = 1.0 - (double) fadeProgress / FADE_DURATION;

        int intensityA = (int)(lastDamageIntensityA * fadeFactor);
        int intensityB = (int)(lastDamageIntensityB * fadeFactor);

        if (intensityA > 0 || intensityB > 0) {
            if (ModConfig.SYNC_CHANNELS.get()) {
                ws.sendWaveformDataDualChannelWithDifferentIntensity("damage", intensityA, intensityB);
            } else {
                ws.sendWaveformData("A", "damage", intensityA);
            }
        }
    }

    // ==================== 外部调用接口 ====================

    /**
     * 更新心跳状态
     */
    public static void updateHeartbeat(int intensityA, int intensityB) {
        lastHeartbeatTick = tickCounter;
        lastHeartbeatIntensityA = intensityA;
        lastHeartbeatIntensityB = intensityB;
        isHeartbeatActive = true;
        isHeartbeatFading = false;
    }

    /**
     * 停止心跳状态
     */
    public static void stopHeartbeat() {
        isHeartbeatActive = false;
    }

    /**
     * 更新环境状态
     */
    public static void updateEnvironment(int intensityA, int intensityB) {
        lastEnvironmentTick = tickCounter;
        lastEnvironmentIntensityA = intensityA;
        lastEnvironmentIntensityB = intensityB;
        isEnvironmentActive = true;
        isEnvironmentFading = false;
    }

    /**
     * 停止环境状态
     */
    public static void stopEnvironment() {
        isEnvironmentActive = false;
    }

    /**
     * 更新伤害状态
     */
    public static void updateDamage(int intensityA, int intensityB) {
        lastDamageTick = tickCounter;
        lastDamageIntensityA = intensityA;
        lastDamageIntensityB = intensityB;
        isDamageActive = true;
        isDamageFading = false;
    }

    /**
     * 停止伤害状态
     */
    public static void stopDamage() {
        isDamageActive = false;
    }

    /**
     * 获取当前tickCounter（供Handler使用）
     */
    public static int getTickCounter() {
        return tickCounter;
    }
}

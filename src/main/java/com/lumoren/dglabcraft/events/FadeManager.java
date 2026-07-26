package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 安全归零看门狗 (与 Fabric 分支对齐)
 * 当所有效果的租约都到期后, 一次性发送归零+清空, 代替旧的逐源渐变引擎
 */
public class FadeManager {

    private static final int CHECK_INTERVAL_TICKS = 10;

    private static int tickCounter = 0;
    private static boolean silenced = true;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        tickCounter++;
        if (tickCounter % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        if (!ws.hasActiveEffects()) {
            if (!silenced) {
                ws.safeSilenceAll();
                silenced = true;
            }
        }
    }

    /**
     * 标记伤害效果活动 (重置归零标志)
     */
    public static void updateDamage(int intensityA, int intensityB) {
        silenced = false;
    }

    /**
     * 标记环境效果活动 (重置归零标志)
     */
    public static void updateEnvironment(int intensityA, int intensityB) {
        silenced = false;
    }

    /**
     * 标记心跳效果活动 (重置归零标志)
     */
    public static void updateHeartbeat(int intensityA, int intensityB) {
        silenced = false;
    }

    /**
     * 旧接口保留: 租约到期由看门狗统一处理, 无需逐源渐变
     */
    public static void stopHeartbeat() {
    }

    public static void stopEnvironment() {
    }

    public static void stopDamage() {
    }
}

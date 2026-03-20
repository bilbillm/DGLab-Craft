package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 安全归零管理器
 *
 * 该类不再负责复杂的来源渐变仲裁，
 * 只在 heartbeat / damage / environment 全部静默时统一执行安全归零。
 */
@Mod.EventBusSubscriber
public class FadeManager {

    private static int tickCounter = 0;
    private static boolean idleResetSent = false;
    private static final int CHECK_INTERVAL = 10;

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        tickCounter++;

        if (tickCounter % CHECK_INTERVAL != 0) {
            return;
        }

        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        if (!ws.hasActiveEffects()) {
            if (!idleResetSent) {
                ws.safeSilenceAll();
                idleResetSent = true;
            }
        } else {
            idleResetSent = false;
        }
    }

    public static void updateHeartbeat(int intensityA, int intensityB) {
        idleResetSent = false;
    }

    public static void stopHeartbeat() {
    }

    public static void updateEnvironment(int intensityA, int intensityB) {
        idleResetSent = false;
    }

    public static void stopEnvironment() {
    }

    public static void updateDamage(int intensityA, int intensityB) {
        idleResetSent = false;
    }

    public static void stopDamage() {
    }
}

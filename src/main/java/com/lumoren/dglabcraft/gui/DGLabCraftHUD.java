package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.ClientModEvents;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

/**
 * HUD 渲染 - 显示 A/B 通道状态和强度
 * 使用客户端Tick事件，每20tick在聊天栏显示状态
 */
@Mod.EventBusSubscriber(modid = "dglabcraft", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DGLabCraftHUD {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 检查 HUD 是否启用
        if (!ModConfig.HUD_ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 在设置界面中不显示
        if (mc.screen != null) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        boolean isConnected = ws.isConnected();

        if (isConnected) {
            double intensityA = ws.getChannelAIntensity();
            double intensityB = ws.getChannelBIntensity();
            String statusA = ws.getChannelAStatus();
            String statusB = ws.getChannelBStatus();

            // 在玩家头像上方显示状态（通过发送系统消息）
            String msg = String.format("\u00a7aDGLab\u00a7r | A: \u00a76%.0f%%\u00a7r %s | B: \u00a79%.0f%%\u00a7r %s",
                intensityA, statusA, intensityB, statusB);
            mc.player.displayClientMessage(Component.literal(msg), true);
        } else {
            // 未连接时显示提示，使用实际绑定的按键
            String keyName = ClientModEvents.OPEN_SETTINGS_KEY.get().getKey().getDisplayName().getString();
            mc.player.displayClientMessage(Component.literal("\u00a7eDGLab\u00a7r | \u00a7c未连接\u00a7r | 按 " + keyName + " 打开设置"), true);
        }
    }
}

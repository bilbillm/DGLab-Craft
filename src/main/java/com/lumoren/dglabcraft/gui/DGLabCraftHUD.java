package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD 渲染 - 显示 A/B 通道状态
 * 暂时禁用 - 需要修复 Forge 1.19.2 API 兼容性
 */
@Mod.EventBusSubscriber(modid = "dglabcraft", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DGLabCraftHUD {

    // 临时禁用 - 等待修复
    /*
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        // ...
    }
    */

    // 暂时使用控制台输出调试
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.screen != null) return;

        // 只每 20 tick 输出一次状态
        if (mc.player.tickCount % 20 == 0) {
            WebSocketManager ws = WebSocketManager.getInstance();
            String statusA = ws.getChannelAStatus();
            String statusB = ws.getChannelBStatus();
            boolean connected = ws.isConnected();

            // 可以打开聊天框看到调试信息
            // mc.player.sendSystemMessage(Component.literal("DGLab: A=" + statusA + " B=" + statusB + " Conn=" + connected));
        }
    }
}

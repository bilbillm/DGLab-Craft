package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.ClientModEvents;
import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.gui.GuiGraphics;

/**
 * HUD 渲染 - 显示 A/B 通道状态和强度
 * 使用 GUI 覆盖层渲染事件，在屏幕四角落显示状态
 */
@EventBusSubscriber(modid = "dglabcraft", value = Dist.CLIENT)
public class DGLabCraftHUD {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Post event) {
        // 检查 HUD 是否启用
        if (!DGLabConfig.HUD_ENABLED.get()) return;

        // 只在渲染完所有原生 GUI 后渲染（例如热栏之后）
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 在设置界面中不显示
        if (mc.screen != null) return;

        // 获取屏幕尺寸
        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();

        // 获取要显示的文本
        String text = getStatusText();

        // 计算文本宽度
        int textWidth = mc.font.width(text);

        // 根据配置计算位置
        int[] pos = calculatePosition(textWidth, screenWidth, screenHeight);

        // 渲染文本 - 使用 PoseStack
        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.drawString(mc.font, Component.literal(text), pos[0], pos[1], 0xFFFFFF, false);
    }

    /**
     * 根据 HUD_POSITION 配置计算渲染坐标
     * 0 = 左上, 1 = 右上, 2 = 左下, 3 = 右下
     */
    private static int[] calculatePosition(int textWidth, int screenWidth, int screenHeight) {
        int pos = DGLabConfig.HUD_POSITION.get();
        int x, y;

        switch (pos) {
            case 1: // 右上角
                x = screenWidth - textWidth - 5;
                y = 5;
                break;
            case 2: // 左下角
                x = 5;
                y = screenHeight - 15;
                break;
            case 3: // 右下角
                x = screenWidth - textWidth - 5;
                y = screenHeight - 15;
                break;
            case 0: // 左上角 (默认)
            default:
                x = 5;
                y = 5;
                break;
        }

        return new int[]{x, y};
    }

    /**
     * 获取要显示的状态文本
     */
    private static String getStatusText() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        boolean isConnected = ws.isConnected();

        if (isConnected) {
            double intensityA = ws.getChannelAIntensity();
            double intensityB = ws.getChannelBIntensity();
            String statusA = ws.getChannelAStatus();
            String statusB = ws.getChannelBStatus();

            return String.format("\u00a7aDGLab\u00a7r | A: \u00a76%.0f%%\u00a7r %s | B: \u00a79%.0f%%\u00a7r %s",
                intensityA, statusA, intensityB, statusB);
        } else {
            // 未连接时显示提示
            String keyName = ClientModEvents.OPEN_SETTINGS_KEY.get().getKey().getDisplayName().getString();
            return "\u00a7eDGLab\u00a7r | \u00a7c未连接\u00a7r | 按 " + keyName + " 打开设置";
        }
    }
}

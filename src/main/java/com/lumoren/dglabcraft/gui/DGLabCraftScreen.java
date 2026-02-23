package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

/**
 * DGLab Craft 设置面板
 * 显示连接状态和二维码按钮
 */
public class DGLabCraftScreen extends Screen {

    private Button openQrButton;

    public DGLabCraftScreen(Screen parent) {
        super(Component.literal("DGLab Craft Settings"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 280;
        int buttonY = this.height / 2 + 20;

        // 创建打开二维码的按钮 (Minecraft 1.19.2 API)
        this.openQrButton = new Button(
            centerX - buttonWidth / 2,
            buttonY,
            buttonWidth,
            20,
            Component.translatable("button.dglabcraft.open_qr"),
            (button) -> {
                // 生成二维码图片
                WebSocketServerManager.getInstance().generateQrUrl();
                // 使用 openFile 打开本地图片（避免 URI 解析错误）
                File qrFile = new File(Minecraft.getInstance().gameDirectory, "dglab-qrcode.png");
                net.minecraft.Util.getPlatform().openFile(qrFile);
            }
        );

        this.addRenderableWidget(this.openQrButton);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        int centerX = this.width / 2;
        int startY = 40;

        // 标题
        drawCenteredString(pPoseStack, this.font, "DGLab Craft Settings", centerX, startY, 0xFFFFFF);

        // 获取服务器状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();

        // 连接状态
        boolean isConnected = server.isConnected();
        String statusText = isConnected ? "已连接 (Connected)" : "等待连接 (Waiting for connection)";
        int statusColor = isConnected ? 0x00FF00 : 0xFFFF00;
        drawCenteredString(pPoseStack, this.font, statusText, centerX, startY + 30, statusColor);

        // 如果已连接，显示客户端 ID
        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                drawCenteredString(pPoseStack, this.font, "Device: " + clientId, centerX, startY + 50, 0xAAAAAA);
            }
        }

        // 显示服务器信息
        drawCenteredString(pPoseStack, this.font, "Server: " + server.getLocalIp() + ":" + server.getPort(),
            centerX, startY + 80, 0xAAAAAA);

        // 未连接时显示提示和按钮
        if (!isConnected) {
            // 提示文字
            drawCenteredString(pPoseStack, this.font, "请点击下方按钮，在浏览器中扫码连接", centerX, this.height / 2 - 10, 0xAAAAAA);

            // 按钮在 init() 中已添加
        } else {
            // 已连接时隐藏按钮
            if (this.openQrButton != null) {
                this.openQrButton.visible = false;
            }
        }

        // 返回提示
        drawCenteredString(pPoseStack, this.font, "Press ESC to close", centerX, this.height - 30, 0x888888);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

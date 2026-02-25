package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

/**
 * DGLab Craft 连接设置界面
 * 显示连接状态和二维码
 */
public class ConnectionScreen extends Screen {

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button doneButton;

    public ConnectionScreen(Screen parent) {
        super(Component.literal("连接设置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 200;

        // 刷新二维码按钮
        this.refreshQrButton = new Button(
            centerX - buttonWidth / 2,
            this.height / 2 + 20,
            buttonWidth,
            20,
            Component.literal("刷新二维码"),
            (button) -> {
                String qrUrl = WebSocketServerManager.getInstance().generateQrUrl();
                QRCodeGenerator.generateQRCode(qrUrl);
            }
        );
        this.addRenderableWidget(this.refreshQrButton);

        // 打开二维码按钮
        this.openQrButton = new Button(
            centerX - buttonWidth / 2,
            this.height / 2 + 50,
            buttonWidth,
            20,
            Component.literal("打开二维码图片"),
            (button) -> {
                File qrFile = new File(Minecraft.getInstance().gameDirectory, "dglab-qrcode.png");
                if (qrFile.exists()) {
                    net.minecraft.Util.getPlatform().openFile(qrFile);
                }
            }
        );
        this.addRenderableWidget(this.openQrButton);

        // 完成按钮
        this.doneButton = new Button(
            centerX - buttonWidth / 2,
            this.height - 40,
            buttonWidth,
            20,
            Component.literal("完成"),
            (button) -> this.onClose()
        );
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        int centerX = this.width / 2;

        // 标题
        drawCenteredString(pPoseStack, this.font, "连接设置", centerX, 30, 0xFFFFFF);

        // 连接状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        String statusText;
        int statusColor;
        if (isConnected) {
            statusText = "已连接";
            statusColor = 0x00FF00;
        } else {
            statusText = "等待连接...";
            statusColor = 0xFFFF00;
        }
        drawCenteredString(pPoseStack, this.font, statusText, centerX, 50, statusColor);

        // 服务器信息
        String serverInfo = "地址: " + server.getLocalIp() + ":" + server.getPort();
        drawCenteredString(pPoseStack, this.font, serverInfo, centerX, 70, 0xAAAAAA);

        // 连接信息
        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                drawCenteredString(pPoseStack, this.font, "设备: " + clientId, centerX, 90, 0xAAAAAA);
            }
        } else {
            // 提示文字
            drawCenteredString(pPoseStack, this.font, "请使用 DGLab App 扫描二维码连接", centerX, this.height / 2 - 20, 0xAAAAAA);
        }

        // 已连接时隐藏二维码按钮
        if (this.refreshQrButton != null) {
            this.refreshQrButton.visible = !isConnected;
        }
        if (this.openQrButton != null) {
            this.openQrButton.visible = !isConnected;
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

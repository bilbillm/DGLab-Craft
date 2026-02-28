package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
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
        this.refreshQrButton = Button.builder(Component.literal("刷新二维码"), button -> {
            String qrUrl = WebSocketServerManager.getInstance().generateQrUrl();
            QRCodeGenerator.generateQRCode(qrUrl);
        }).bounds(centerX - buttonWidth / 2, this.height / 2 + 20, buttonWidth, 20).build();
        this.addRenderableWidget(this.refreshQrButton);

        // 打开二维码按钮
        this.openQrButton = Button.builder(Component.literal("打开二维码图片"), button -> {
            File qrFile = new File(Minecraft.getInstance().gameDirectory, "dglab-qrcode.png");
            if (qrFile.exists()) {
                net.minecraft.Util.getPlatform().openFile(qrFile);
            }
        }).bounds(centerX - buttonWidth / 2, this.height / 2 + 50, buttonWidth, 20).build();
        this.addRenderableWidget(this.openQrButton);

        // 完成按钮
        this.doneButton = Button.builder(Component.literal("完成"), button -> this.onClose())
            .bounds(centerX - buttonWidth / 2, this.height - 40, buttonWidth, 20).build();
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;

        // 标题
        guiGraphics.drawCenteredString(this.font, Component.literal("连接设置"), centerX, 30, 0xFFFFFF);

        // 连接状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        Component statusText;
        int statusColor;
        if (isConnected) {
            statusText = Component.literal("已连接");
            statusColor = 0x00FF00;
        } else {
            statusText = Component.literal("等待连接...");
            statusColor = 0xFFFF00;
        }
        guiGraphics.drawCenteredString(this.font, statusText, centerX, 50, statusColor);

        // 服务器信息
        String serverInfo = "地址: " + server.getLocalIp() + ":" + server.getPort();
        guiGraphics.drawCenteredString(this.font, Component.literal(serverInfo), centerX, 70, 0xAAAAAA);

        // 连接信息
        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                guiGraphics.drawCenteredString(this.font, Component.literal("设备: " + clientId), centerX, 90, 0xAAAAAA);
            }
        } else {
            // 提示文字
            guiGraphics.drawCenteredString(this.font, Component.literal("请使用 DGLab App 扫描二维码连接"), centerX, this.height / 2 - 20, 0xAAAAAA);
        }

        // 已连接时隐藏二维码按钮
        if (this.refreshQrButton != null) {
            this.refreshQrButton.visible = !isConnected;
        }
        if (this.openQrButton != null) {
            this.openQrButton.visible = !isConnected;
        }

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}

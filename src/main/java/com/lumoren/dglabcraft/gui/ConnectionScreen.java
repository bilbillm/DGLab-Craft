package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

/**
 * DGLab Craft 连接设置界面
 * 显示连接状态和二维码
 */
public class ConnectionScreen extends Screen {

    private static final Component MANUAL_IP_HINT = Component.literal("留空则使用自动获取的ip")
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button doneButton;
    private EditBox manualIpInput;
    private boolean manualIpInvalid = false;

    public ConnectionScreen(Screen parent) {
        super(Component.literal("连接设置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 200;

        WebSocketServerManager server = WebSocketServerManager.getInstance();

        this.manualIpInput = new EditBox(this.font, centerX - buttonWidth / 2, this.height / 2 - 10, buttonWidth, 20, Component.literal("手动局域网IP"));
        String currentHost = ModConfig.WS_HOST.get();
        if (currentHost != null && !currentHost.isBlank() && !"localhost".equalsIgnoreCase(currentHost)) {
            this.manualIpInput.setValue(currentHost);
        }
        this.addRenderableWidget(this.manualIpInput);

        if (!server.isConnected()) {
            ensureQrCodeGenerated();
        }

        // 刷新二维码按钮
        this.refreshQrButton = new Button(
            centerX - buttonWidth / 2,
            this.height / 2 + 20,
            buttonWidth,
            20,
            Component.literal("刷新二维码"),
            (button) -> {
                if (commitManualIpInput()) {
                    ensureQrCodeGenerated();
                }
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
        commitManualIpInput();
        this.minecraft.setScreen(this.parent);
    }

    private boolean commitManualIpInput() {
        String value = this.manualIpInput == null ? "" : this.manualIpInput.getValue().trim();
        if (value.isEmpty()) {
            ModConfig.WS_HOST.set("localhost");
            ModConfig.save();
            manualIpInvalid = false;
            return true;
        }
        if (!isValidIpv4(value)) {
            manualIpInvalid = true;
            return false;
        }

        ModConfig.WS_HOST.set(value);
        ModConfig.save();
        manualIpInvalid = false;
        return true;
    }

    private void ensureQrCodeGenerated() {
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        server.generateQrUrl();
    }

    private boolean isValidIpv4(String value) {
        String[] parts = value.split("\\.");
        if (parts.length != 4) return false;
        try {
            for (String part : parts) {
                if (part.isEmpty() || (part.length() > 1 && part.startsWith("0"))) {
                    return false;
                }
                int number = Integer.parseInt(part);
                if (number < 0 || number > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
        String serverInfo = "地址: " + server.resolveConnectionHost() + ":" + server.getPort();
        drawCenteredString(pPoseStack, this.font, serverInfo, centerX, 70, 0xAAAAAA);

        if (!isConnected) {
            drawCenteredString(pPoseStack, this.font, "请使用DGLab APP扫描二维码连接", centerX, 82, 0xAAAAAA);
        }

        if (manualIpInvalid) {
            drawCenteredString(pPoseStack, this.font, "手动 IP 无效，请输入正确的 IPv4 地址", centerX, this.height / 2 + 2, 0xFF5555);
        } else if (this.manualIpInput != null && this.manualIpInput.getValue().isEmpty()) {
            this.font.drawShadow(pPoseStack, MANUAL_IP_HINT, centerX - 95, this.height / 2 - 4, 0x888888);
        }

        // 连接信息
        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                drawCenteredString(pPoseStack, this.font, "设备: " + clientId, centerX, 90, 0xAAAAAA);
            }
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
        return true;
    }
}

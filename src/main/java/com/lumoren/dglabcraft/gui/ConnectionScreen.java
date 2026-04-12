package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
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

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_VERTICAL_SPACING = 4;
    private static final int SECTION_VERTICAL_SPACING = 4;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 30;
    private static final int QR_BUTTON_COUNT = 3;

    private static final Component MANUAL_IP_HINT = Component.literal("留空则使用自动获取的ip")
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button openQrFolderButton;
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
        int manualInputY = getManualInputY();
        int doneButtonY = getDoneButtonY();
        int qrButtonStartY = getQrButtonStartY(doneButtonY);

        WebSocketServerManager server = WebSocketServerManager.getInstance();

        this.manualIpInput = new EditBox(this.font, centerX - buttonWidth / 2, manualInputY, buttonWidth, BUTTON_HEIGHT, Component.literal("手动局域网IP"));
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
            getQrButtonY(0, qrButtonStartY),
            buttonWidth,
            BUTTON_HEIGHT,
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
            getQrButtonY(1, qrButtonStartY),
            buttonWidth,
            BUTTON_HEIGHT,
            Component.literal("打开二维码图片"),
            (button) -> {
                File qrFile = getQrCodeFile();
                if (qrFile.isFile()) {
                    net.minecraft.Util.getPlatform().openFile(qrFile);
                }
            }
        );
        this.addRenderableWidget(this.openQrButton);

        this.openQrFolderButton = new Button(
            centerX - buttonWidth / 2,
            getQrButtonY(2, qrButtonStartY),
            buttonWidth,
            BUTTON_HEIGHT,
            Component.literal("打开二维码文件夹"),
            (button) -> {
                File qrFile = getQrCodeFile();
                File qrFolder = qrFile.getParentFile();
                if (qrFolder != null && qrFolder.isDirectory()) {
                    net.minecraft.Util.getPlatform().openFile(qrFolder);
                }
            }
        );
        this.addRenderableWidget(this.openQrFolderButton);

        // 完成按钮
        this.doneButton = new Button(
            centerX - buttonWidth / 2,
            doneButtonY,
            buttonWidth,
            BUTTON_HEIGHT,
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
        if (this.manualIpInput != null) {
            this.manualIpInput.setSuggestion(MANUAL_IP_HINT.getString());
        }
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

            File qrFile = getQrCodeFile();
            if (qrFile.isFile()) {
                renderQrPath(pPoseStack, centerX, 94, qrFile);
            }
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
            this.openQrButton.visible = !isConnected && hasQrImageFile();
        }
        if (this.openQrFolderButton != null) {
            this.openQrFolderButton.visible = !isConnected && hasQrFolder();
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private File getQrCodeFile() {
        File qrFile = QRCodeGenerator.getQrCodeFile();
        if (qrFile != null) {
            return qrFile;
        }
        return new File(Minecraft.getInstance().gameDirectory, "dglab-qrcode.png");
    }

    private boolean hasQrImageFile() {
        return getQrCodeFile().isFile();
    }

    private boolean hasQrFolder() {
        File qrFolder = getQrCodeFile().getParentFile();
        return qrFolder != null && qrFolder.isDirectory();
    }

    private int getDoneButtonY() {
        return this.height - DONE_BUTTON_BOTTOM_MARGIN;
    }

    private int getManualInputY() {
        return this.height / 2 - 10;
    }

    private int getQrButtonStartY(int doneButtonY) {
        int minY = getManualInputY() + BUTTON_HEIGHT + SECTION_VERTICAL_SPACING;
        int maxY = doneButtonY - SECTION_VERTICAL_SPACING - getQrButtonBlockHeight();
        return Math.max(minY, maxY);
    }

    private int getQrButtonY(int index, int qrButtonStartY) {
        return qrButtonStartY + index * (BUTTON_HEIGHT + BUTTON_VERTICAL_SPACING);
    }

    private int getQrButtonBlockHeight() {
        return QR_BUTTON_COUNT * BUTTON_HEIGHT + (QR_BUTTON_COUNT - 1) * BUTTON_VERTICAL_SPACING;
    }

    private void renderQrPath(PoseStack pPoseStack, int centerX, int startY, File qrFile) {
        drawCenteredString(pPoseStack, this.font, "二维码文件:", centerX, startY, 0xAAAAAA);
        drawCenteredString(pPoseStack, this.font, fitTextToWidth(qrFile.getAbsolutePath(), Math.max(120, this.width - 40)), centerX, startY + 12, 0xAAAAAA);
    }

    private String fitTextToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int targetWidth = maxWidth - this.font.width(ellipsis);
        if (targetWidth <= 0) {
            return ellipsis;
        }

        int left = 0;
        int right = text.length();
        String prefix = "";
        String suffix = "";

        while (left < right && this.font.width(prefix + suffix) < targetWidth) {
            if ((left + (text.length() - right)) % 2 == 0) {
                prefix += text.charAt(left++);
            } else {
                suffix = text.charAt(--right) + suffix;
            }

            while (!suffix.isEmpty() && this.font.width(prefix + suffix) > targetWidth) {
                suffix = suffix.substring(1);
            }
        }

        while (!prefix.isEmpty() && this.font.width(prefix + suffix) > targetWidth) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        return prefix + ellipsis + suffix;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}

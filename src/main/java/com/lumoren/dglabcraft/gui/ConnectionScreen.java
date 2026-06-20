package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
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

    private static final Component MANUAL_IP_HINT = Component.translatable("hint.dglabcraft.manual_ip")
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button openQrFolderButton;
    private Button doneButton;
    private EditBox manualIpInput;
    private boolean manualIpInvalid = false;

    public ConnectionScreen(Screen parent) {
        super(Component.translatable("screen.dglabcraft.connection_settings"));
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

        this.manualIpInput = new EditBox(this.font, centerX - buttonWidth / 2, manualInputY, buttonWidth, BUTTON_HEIGHT, Component.translatable("label.dglabcraft.manual_lan_ip"));
        String currentHost = ModConfig.WS_HOST.get();
        if (currentHost != null && !currentHost.isBlank() && !"localhost".equalsIgnoreCase(currentHost)) {
            this.manualIpInput.setValue(currentHost);
        }
        this.manualIpInput.setHint(MANUAL_IP_HINT);
        this.addRenderableWidget(this.manualIpInput);

        if (!server.isConnected()) {
            ensureQrCodeGenerated();
        }

        // 刷新二维码按钮
        this.refreshQrButton = Button.builder(Component.translatable("button.dglabcraft.refresh_qr"), button -> {
            if (commitManualIpInput()) {
                ensureQrCodeGenerated();
            }
        }).bounds(centerX - buttonWidth / 2, getQrButtonY(0, qrButtonStartY), buttonWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.refreshQrButton);

        // 打开二维码按钮
        this.openQrButton = Button.builder(Component.translatable("button.dglabcraft.open_qr_image"), button -> {
            File qrFile = getQrCodeFile();
            if (qrFile.isFile()) {
                net.minecraft.Util.getPlatform().openFile(qrFile);
            }
        }).bounds(centerX - buttonWidth / 2, getQrButtonY(1, qrButtonStartY), buttonWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.openQrButton);

        this.openQrFolderButton = Button.builder(Component.translatable("button.dglabcraft.open_qr_folder"), button -> {
            File qrFile = getQrCodeFile();
            File qrFolder = qrFile.getParentFile();
            if (qrFolder != null && qrFolder.isDirectory()) {
                net.minecraft.Util.getPlatform().openFile(qrFolder);
            }
        }).bounds(centerX - buttonWidth / 2, getQrButtonY(2, qrButtonStartY), buttonWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.openQrFolderButton);

        // 完成按钮
        this.doneButton = Button.builder(Component.translatable("button.dglabcraft.done"), button -> this.onClose())
            .bounds(centerX - buttonWidth / 2, doneButtonY, buttonWidth, BUTTON_HEIGHT).build();
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
            this.manualIpInput.setHint(MANUAL_IP_HINT);
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
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;

        // 标题
        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.dglabcraft.connection_settings"), centerX, 30, 0xFFFFFF);

        // 连接状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        Component statusText;
        int statusColor;
        if (isConnected) {
            statusText = Component.translatable("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = Component.translatable("status.dglabcraft.waiting_connection");
            statusColor = 0xFFFF00;
        }
        guiGraphics.drawCenteredString(this.font, statusText, centerX, 50, statusColor);

        // 服务器信息
        guiGraphics.drawCenteredString(this.font, Component.translatable("label.dglabcraft.address",
            server.resolveConnectionHost(), server.getPort()), centerX, 70, 0xAAAAAA);

        if (!isConnected) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("message.dglabcraft.scan_qr"), centerX, 82, 0xAAAAAA);

            File qrFile = getQrCodeFile();
            if (qrFile.isFile()) {
                renderQrPath(guiGraphics, centerX, 94, qrFile);
            }
        }

        if (manualIpInvalid) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("error.dglabcraft.invalid_manual_ip"), centerX, this.height / 2 + 2, 0xFF5555);
        }

        // 连接信息
        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                guiGraphics.drawCenteredString(this.font, Component.translatable("label.dglabcraft.device", clientId), centerX, 90, 0xAAAAAA);
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

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
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

    private void renderQrPath(GuiGraphics guiGraphics, int centerX, int startY, File qrFile) {
        Font font = this.font;
        int maxTextWidth = Math.max(120, this.width - 40);

        guiGraphics.drawCenteredString(font, Component.translatable("label.dglabcraft.qr_file"), centerX, startY, 0xAAAAAA);
        guiGraphics.drawCenteredString(font, Component.literal(fitTextToWidth(qrFile.getAbsolutePath(), maxTextWidth)), centerX, startY + 12, 0xAAAAAA);
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

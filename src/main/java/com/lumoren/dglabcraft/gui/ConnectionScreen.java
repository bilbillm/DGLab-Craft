package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderPipelines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * DGLab Craft 连接设置界面
 * 显示连接状态和二维码
 * <p>
 * 未连接时直接在游戏内渲染生成的二维码纹理；连接成功后隐藏。
 * 纹理加载失败时退回原有的扫码提示文字，并保留打开图片/打开目录按钮作为兜底。
 */
public class ConnectionScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("DGLabCraft/ConnectionScreen");
    private static final Component MANUAL_IP_HINT = Component.translatable("hint.dglabcraft.manual_ip")
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    private static final ResourceLocation QR_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("dglabcraft", "qrcode");

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button openQrFolderButton;
    private Button doneButton;
    private EditBox manualIpInput;
    private boolean manualIpInvalid = false;
    private ResourceLocation qrTextureLocation;
    private long qrTextureModifiedAt = -1L;
    private long qrTextureFileLength = -1L;
    private long qrFailedModifiedAt = -1L;
    private long qrFailedFileLength = -1L;
    private int qrTextureWidth = 0;
    private int qrTextureHeight = 0;

    public ConnectionScreen(Screen parent) {
        super(Component.translatable("screen.dglabcraft.connection_settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        ConnectionLayout.Layout layout = ConnectionLayout.calculate(this.width, this.height);
        WebSocketServerManager server = WebSocketServerManager.getInstance();

        ConnectionLayout.Rect input = layout.manualInput();
        this.manualIpInput = new EditBox(this.font, input.x(), input.y(), input.width(), input.height(),
            Component.translatable("label.dglabcraft.manual_lan_ip"));
        String currentHost = DGLabConfig.WS_HOST.get();
        if (currentHost != null && !currentHost.isBlank() && !"localhost".equalsIgnoreCase(currentHost)) {
            this.manualIpInput.setValue(currentHost);
        }
        this.manualIpInput.setHint(MANUAL_IP_HINT);
        this.addRenderableWidget(this.manualIpInput);

        if (!server.isConnected()) {
            ensureQrCodeGenerated();
        }

        List<ConnectionLayout.Rect> buttons = layout.controlButtons();

        // 刷新二维码按钮
        this.refreshQrButton = Button.builder(Component.translatable("button.dglabcraft.refresh_qr"), button -> {
            if (commitManualIpInput()) {
                ensureQrCodeGenerated();
            }
        }).bounds(buttons.get(0).x(), buttons.get(0).y(), buttons.get(0).width(), buttons.get(0).height()).build();
        this.addRenderableWidget(this.refreshQrButton);

        // 打开二维码图片按钮
        this.openQrButton = Button.builder(Component.translatable("button.dglabcraft.open_qr_image"), button -> {
            File qrFile = getQrCodeFile();
            if (qrFile.isFile()) {
                net.minecraft.Util.getPlatform().openFile(qrFile);
            }
        }).bounds(buttons.get(1).x(), buttons.get(1).y(), buttons.get(1).width(), buttons.get(1).height()).build();
        this.addRenderableWidget(this.openQrButton);

        // 打开二维码文件夹按钮
        this.openQrFolderButton = Button.builder(Component.translatable("button.dglabcraft.open_qr_folder"), button -> {
            File qrFile = getQrCodeFile();
            File qrFolder = qrFile.getParentFile();
            if (qrFolder != null && qrFolder.isDirectory()) {
                net.minecraft.Util.getPlatform().openFile(qrFolder);
            }
        }).bounds(buttons.get(2).x(), buttons.get(2).y(), buttons.get(2).width(), buttons.get(2).height()).build();
        this.addRenderableWidget(this.openQrFolderButton);

        // 完成按钮
        ConnectionLayout.Rect done = layout.doneButton();
        this.doneButton = Button.builder(Component.translatable("button.dglabcraft.done"), button -> this.onClose())
            .bounds(done.x(), done.y(), done.width(), done.height()).build();
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void onClose() {
        commitManualIpInput();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed() {
        unloadQrTexture();
        super.removed();
    }

    private boolean commitManualIpInput() {
        String value = this.manualIpInput == null ? "" : this.manualIpInput.getValue().trim();
        if (value.isEmpty()) {
            DGLabConfig.WS_HOST.set("localhost");
            DGLabConfig.save();
            manualIpInvalid = false;
            return true;
        }
        if (!isValidIpv4(value)) {
            manualIpInvalid = true;
            return false;
        }

        DGLabConfig.WS_HOST.set(value);
        DGLabConfig.save();
        manualIpInvalid = false;
        return true;
    }

    private void ensureQrCodeGenerated() {
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        server.generateQrUrl();
        unloadQrTexture();
        this.qrFailedModifiedAt = -1L;
        this.qrFailedFileLength = -1L;
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
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        int centerX = this.width / 2;
        ConnectionLayout.Layout layout = ConnectionLayout.calculate(this.width, this.height);
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        // 已连接时隐藏二维码面板与二维码按钮
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

        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.dglabcraft.connection_settings"),
            centerX, ConnectionLayout.HEADER_TITLE_Y, 0xFFFFFF);

        Component statusText;
        int statusColor;
        if (isConnected) {
            statusText = Component.translatable("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = Component.translatable("status.dglabcraft.waiting_connection");
            statusColor = 0xFFFF00;
        }
        guiGraphics.drawCenteredString(this.font, statusText, centerX, ConnectionLayout.HEADER_STATUS_Y, statusColor);

        guiGraphics.drawCenteredString(this.font, Component.translatable("label.dglabcraft.address",
            server.resolveConnectionHost(), server.getPort()), centerX, ConnectionLayout.HEADER_ADDRESS_Y, 0xAAAAAA);

        if (!isConnected) {
            renderQrPanel(guiGraphics, layout, getQrCodeFile());
        } else {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                guiGraphics.drawCenteredString(this.font, Component.translatable("label.dglabcraft.device", clientId),
                    centerX, ConnectionLayout.CONTENT_TOP + 8, 0xAAAAAA);
            }
        }

        if (this.manualIpInput != null && (layout.showManualLabel() || this.manualIpInvalid)) {
            Component manualLabel = this.manualIpInvalid
                ? Component.translatable("error.dglabcraft.invalid_manual_ip")
                : Component.translatable("label.dglabcraft.manual_lan_ip");
            int manualLabelColor = this.manualIpInvalid ? 0xFF5555 : 0xAAAAAA;
            int labelY = layout.showManualLabel()
                ? layout.manualLabelY()
                : Math.max(0, layout.manualInput().y() - 10);
            guiGraphics.drawCenteredString(this.font, manualLabel,
                layout.manualInput().x() + layout.manualInput().width() / 2, labelY, manualLabelColor);
        }
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

    private void renderQrPanel(GuiGraphics guiGraphics, ConnectionLayout.Layout layout, File qrFile) {
        ConnectionLayout.Rect panel = layout.qrPanel();
        ConnectionLayout.Rect qr = layout.qrImage();
        guiGraphics.fill(panel.x() - 1, panel.y() - 1, panel.right() + 1, panel.bottom() + 1, 0xFF111111);
        guiGraphics.fill(panel.x(), panel.y(), panel.right(), panel.bottom(), 0xFFFFFFFF);

        if (qrFile.isFile() && loadQrTexture(qrFile)) {
            drawQrTexture(guiGraphics, qr);
        } else {
            renderQrFallback(guiGraphics, qr);
            renderQrFileHint(guiGraphics, qr, qrFile);
        }
    }

    private void drawQrTexture(GuiGraphics guiGraphics, ConnectionLayout.Rect qr) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, qrTextureLocation, qr.x(), qr.y(),
            0.0F, 0.0F, qr.width(), qr.height(), qrTextureWidth, qrTextureHeight,
            qrTextureWidth, qrTextureHeight);
    }

    private void renderQrFallback(GuiGraphics guiGraphics, ConnectionLayout.Rect qr) {
        var lines = this.font.split(Component.translatable("message.dglabcraft.scan_qr"),
            Math.max(20, qr.width() - 8));
        int lineHeight = 9;
        int hintHeight = Math.max(lineHeight, qr.height() - 14);
        int textY = qr.y() + Math.max(0, (hintHeight - lines.size() * lineHeight) / 2);
        for (var line : lines) {
            guiGraphics.drawCenteredString(this.font, line, qr.x() + qr.width() / 2, textY, 0x555555);
            textY += lineHeight;
        }
    }

    private void renderQrFileHint(GuiGraphics guiGraphics, ConnectionLayout.Rect qr, File qrFile) {
        if (qr.height() < 44 || !qrFile.isFile()) {
            return;
        }
        String path = fitTextToWidth(qrFile.getAbsolutePath(), Math.max(20, qr.width() - 8));
        guiGraphics.drawCenteredString(this.font, Component.literal(path),
            qr.x() + qr.width() / 2, qr.bottom() - 12, 0x777777);
    }

    private boolean loadQrTexture(File qrFile) {
        long modifiedAt = qrFile.lastModified();
        long fileLength = qrFile.length();
        if (this.qrTextureLocation != null && this.qrTextureModifiedAt == modifiedAt
            && this.qrTextureFileLength == fileLength) {
            return true;
        }
        if (this.qrFailedModifiedAt == modifiedAt && this.qrFailedFileLength == fileLength) {
            return false;
        }

        unloadQrTexture();
        NativeImage image = null;
        try (InputStream input = new FileInputStream(qrFile)) {
            image = NativeImage.read(input);
            this.qrTextureWidth = image.getWidth();
            this.qrTextureHeight = image.getHeight();
            Minecraft.getInstance().getTextureManager().register(QR_TEXTURE_ID,
                new DynamicTexture(() -> "dglabcraft_qrcode", image));
            this.qrTextureLocation = QR_TEXTURE_ID;
            this.qrTextureModifiedAt = modifiedAt;
            this.qrTextureFileLength = fileLength;
            return true;
        } catch (IOException | RuntimeException e) {
            if (this.qrTextureLocation == null && image != null) {
                try {
                    image.close();
                } catch (RuntimeException ignored) {
                    // 尽力释放，加载失败不应影响界面
                }
            }
            this.qrTextureLocation = null;
            this.qrTextureWidth = 0;
            this.qrTextureHeight = 0;
            this.qrFailedModifiedAt = modifiedAt;
            this.qrFailedFileLength = fileLength;
            LOGGER.warn("Unable to load QR code texture from {}", qrFile, e);
            return false;
        }
    }

    private void unloadQrTexture() {
        if (this.qrTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(this.qrTextureLocation);
        }
        this.qrTextureLocation = null;
        this.qrTextureModifiedAt = -1L;
        this.qrTextureFileLength = -1L;
        this.qrTextureWidth = 0;
        this.qrTextureHeight = 0;
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

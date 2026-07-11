package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * DGLab Craft 连接设置界面
 * 显示连接状态和二维码
 */
public class ConnectionScreen extends Screen {
    private static final Component MANUAL_IP_HINT = new TranslatableComponent("hint.dglabcraft.manual_ip")
        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final Screen parent;
    private Button refreshQrButton;
    private Button openQrButton;
    private Button openQrFolderButton;
    private Button doneButton;
    private EditBox manualIpInput;
    private boolean manualIpInvalid = false;
    private ResourceLocation qrTextureLocation;
    private long qrTextureModifiedAt = -1L;
    private int qrTextureWidth = 0;
    private int qrTextureHeight = 0;
    private int helpScrollOffset = 0;
    private int maxHelpScroll = 0;
    private ConnectionLayout.Layout currentLayout;

    public ConnectionScreen(Screen parent) {
        super(new TranslatableComponent("screen.dglabcraft.connection_settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        ConnectionLayout.Layout layout = ConnectionLayout.calculate(this.width, this.height);
        this.currentLayout = layout;
        this.helpScrollOffset = 0;

        WebSocketServerManager server = WebSocketServerManager.getInstance();

        ConnectionLayout.Rect manualInput = layout.manualInput();
        this.manualIpInput = new EditBox(this.font, manualInput.x(), manualInput.y(), manualInput.width(), manualInput.height(), new TranslatableComponent("label.dglabcraft.manual_lan_ip"));
        String currentHost = ModConfig.WS_HOST.get();
        if (currentHost != null && !currentHost.isBlank() && !"localhost".equalsIgnoreCase(currentHost)) {
            this.manualIpInput.setValue(currentHost);
        }
        this.manualIpInput.setSuggestion(MANUAL_IP_HINT.getString());
        this.addRenderableWidget(this.manualIpInput);

        if (!server.isConnected()) {
            ensureQrCodeGenerated();
        }

        // 刷新二维码按钮
        ConnectionLayout.Rect refresh = layout.controlButtons().get(0);
        this.refreshQrButton = new Button(refresh.x(), refresh.y(), refresh.width(), refresh.height(),
            new TranslatableComponent("button.dglabcraft.refresh_qr"), button -> {
            if (commitManualIpInput()) {
                ensureQrCodeGenerated();
            }
        });
        this.addRenderableWidget(this.refreshQrButton);

        // 打开二维码按钮
        ConnectionLayout.Rect open = layout.controlButtons().get(1);
        this.openQrButton = new Button(open.x(), open.y(), open.width(), open.height(),
            new TranslatableComponent("button.dglabcraft.open_qr_image"), button -> {
            File qrFile = getQrCodeFile();
            if (qrFile.isFile()) {
                net.minecraft.Util.getPlatform().openFile(qrFile);
            }
        });
        this.addRenderableWidget(this.openQrButton);

        ConnectionLayout.Rect openFolder = layout.controlButtons().get(2);
        this.openQrFolderButton = new Button(openFolder.x(), openFolder.y(), openFolder.width(), openFolder.height(),
            new TranslatableComponent("button.dglabcraft.open_qr_folder"), button -> {
            File qrFile = getQrCodeFile();
            File qrFolder = qrFile.getParentFile();
            if (qrFolder != null && qrFolder.isDirectory()) {
                net.minecraft.Util.getPlatform().openFile(qrFolder);
            }
        });
        this.addRenderableWidget(this.openQrFolderButton);

        // 完成按钮
        ConnectionLayout.Rect done = layout.doneButton();
        this.doneButton = new Button(done.x(), done.y(), done.width(), done.height(),
            new TranslatableComponent("button.dglabcraft.done"), button -> this.onClose());
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
        unloadQrTexture();
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int centerX = this.width / 2;
        ConnectionLayout.Layout layout = ConnectionLayout.calculate(this.width, this.height);
        this.currentLayout = layout;
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

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

        super.render(poseStack, mouseX, mouseY, partialTick);

        drawCenteredString(poseStack, this.font, new TranslatableComponent("screen.dglabcraft.connection_settings"),
            centerX, ConnectionLayout.HEADER_TITLE_Y, 0xFFFFFF);

        Component statusText;
        int statusColor;
        if (isConnected) {
            statusText = new TranslatableComponent("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = new TranslatableComponent("status.dglabcraft.waiting_connection");
            statusColor = 0xFFFF00;
        }
        drawCenteredString(poseStack, this.font, statusText, centerX, ConnectionLayout.HEADER_STATUS_Y, statusColor);

        drawCenteredString(poseStack, this.font, new TranslatableComponent("label.dglabcraft.address",
            server.resolveConnectionHost(), server.getPort()), centerX, ConnectionLayout.HEADER_ADDRESS_Y, 0xAAAAAA);

        if (!isConnected) {
            File qrFile = getQrCodeFile();
            renderQrPanel(poseStack, layout, qrFile);
            renderConnectionHelp(poseStack, layout);
        } else {
            this.maxHelpScroll = 0;
            this.helpScrollOffset = 0;
        }

        if (isConnected) {
            String clientId = server.getConnectedClientId();
            if (clientId != null) {
                drawCenteredString(poseStack, this.font, new TranslatableComponent("label.dglabcraft.device", clientId), centerX, 90, 0xAAAAAA);
            }
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

    private void renderQrPanel(PoseStack poseStack, ConnectionLayout.Layout layout, File qrFile) {
        ConnectionLayout.Rect panel = layout.qrPanel();
        ConnectionLayout.Rect qr = layout.qrImage();
        fill(poseStack, panel.x() - 2, panel.y() - 2, panel.right() + 2, panel.bottom() + 2, 0xFF111111);
        fill(poseStack, panel.x(), panel.y(), panel.right(), panel.bottom(), 0xFFFFFFFF);

        if (qrFile.isFile() && loadQrTexture(qrFile)) {
            RenderSystem.setShaderTexture(0, qrTextureLocation);
            blit(poseStack, layout.qrImage().x(), layout.qrImage().y(),
                layout.qrImage().width(), layout.qrImage().height(), 0.0F, 0.0F,
                qrTextureWidth, qrTextureHeight, qrTextureWidth, qrTextureHeight);
        } else {
            drawCenteredString(poseStack, this.font, new TranslatableComponent("message.dglabcraft.scan_qr"),
                qr.x() + qr.width() / 2, qr.y() + qr.height() / 2 - 4, 0x555555);
        }
    }

    private void renderConnectionHelp(PoseStack poseStack, ConnectionLayout.Layout layout) {
        ConnectionLayout.Rect controls = layout.controls();
        Component manualLabel = manualIpInvalid
            ? new TranslatableComponent("error.dglabcraft.invalid_manual_ip")
            : new TranslatableComponent("label.dglabcraft.manual_lan_ip");
        int manualLabelColor = manualIpInvalid ? 0xFF5555 : 0xAAAAAA;
        drawCenteredString(poseStack, this.font, manualLabel,
            controls.x() + controls.width() / 2, layout.manualLabelY(), manualLabelColor);

        ConnectionLayout.Rect viewport = layout.helpViewport();
        if (viewport.height() <= 0) {
            this.maxHelpScroll = 0;
            this.helpScrollOffset = 0;
            return;
        }

        List<HelpLine> lines = buildHelpLines(Math.max(40, viewport.width() - 16));
        int contentHeight = lines.stream().mapToInt(HelpLine::height).sum();
        this.maxHelpScroll = DiagnosticLayout.maxScroll(contentHeight, viewport.y(), viewport.bottom());
        this.helpScrollOffset = DiagnosticLayout.clampScroll(this.helpScrollOffset, this.maxHelpScroll);

        int centerX = viewport.x() + viewport.width() / 2;
        int y = viewport.y() - this.helpScrollOffset;
        enableScissor(viewport);
        for (HelpLine line : lines) {
            if (line.text() != null && y + line.height() > viewport.y() && y < viewport.bottom()) {
                drawCenteredString(poseStack, this.font, line.text(), centerX, y, line.color());
            }
            y += line.height();
        }
        RenderSystem.disableScissor();
        renderHelpScrollBar(poseStack, viewport, contentHeight);
    }

    private List<HelpLine> buildHelpLines(int maxWidth) {
        List<HelpLine> lines = new ArrayList<>();
        addWrappedHelpLine(lines, new TranslatableComponent("section.dglabcraft.connection_guide"), maxWidth, 0xFFFF55, 13);
        addWrappedHelpLine(lines, new TranslatableComponent("guide.dglabcraft.connection.step1"), maxWidth, 0xAAAAAA, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("guide.dglabcraft.connection.step2"), maxWidth, 0xAAAAAA, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("guide.dglabcraft.connection.step3"), maxWidth, 0xAAAAAA, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("guide.dglabcraft.connection.step4"), maxWidth, 0xDDDDDD, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("guide.dglabcraft.connection.step5"), maxWidth, 0xFFFF55, 11);
        lines.add(new HelpLine(null, 0, 5));
        addWrappedHelpLine(lines, new TranslatableComponent("section.dglabcraft.connection_troubleshooting"), maxWidth, 0xFF6666, 13);
        addWrappedHelpLine(lines, new TranslatableComponent("troubleshoot.dglabcraft.connection.same_wifi"), maxWidth, 0xFF7777, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("troubleshoot.dglabcraft.connection.firewall"), maxWidth, 0xFF7777, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("troubleshoot.dglabcraft.connection.manual_ip"), maxWidth, 0xFF7777, 11);
        addWrappedHelpLine(lines, new TranslatableComponent("troubleshoot.dglabcraft.connection.port"), maxWidth, 0xFF7777, 11);
        return lines;
    }

    private void addWrappedHelpLine(List<HelpLine> lines, Component text, int maxWidth, int color, int lineHeight) {
        for (FormattedCharSequence wrappedLine : this.font.split(text, maxWidth)) {
            lines.add(new HelpLine(wrappedLine, color, lineHeight));
        }
    }

    private void renderHelpScrollBar(PoseStack poseStack, ConnectionLayout.Rect viewport, int contentHeight) {
        if (this.maxHelpScroll <= 0 || viewport.height() <= 0) {
            return;
        }

        int trackX = viewport.right() - 3;
        int thumbHeight = Math.max(10, viewport.height() * viewport.height() / Math.max(1, contentHeight));
        int thumbY = viewport.y()
            + (viewport.height() - thumbHeight) * this.helpScrollOffset / this.maxHelpScroll;
        fill(poseStack, trackX, viewport.y(), trackX + 2, viewport.bottom(), 0x55333333);
        fill(poseStack, trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    private void enableScissor(ConnectionLayout.Rect viewport) {
        double scale = this.minecraft.getWindow().getGuiScale();
        int x = (int) Math.floor(viewport.x() * scale);
        int y = (int) Math.floor((this.height - viewport.bottom()) * scale);
        int width = (int) Math.ceil(viewport.width() * scale);
        int height = (int) Math.ceil(viewport.height() * scale);
        RenderSystem.enableScissor(x, y, width, height);
    }

    private boolean loadQrTexture(File qrFile) {
        long modifiedAt = qrFile.lastModified();
        if (qrTextureLocation != null && qrTextureModifiedAt == modifiedAt) {
            return true;
        }

        unloadQrTexture();

        try (InputStream input = new FileInputStream(qrFile)) {
            NativeImage image = NativeImage.read(input);
            qrTextureWidth = image.getWidth();
            qrTextureHeight = image.getHeight();
            qrTextureLocation = this.minecraft.getTextureManager().register("dglabcraft_qrcode", new DynamicTexture(image));
            qrTextureModifiedAt = modifiedAt;
            return true;
        } catch (IOException e) {
            qrTextureLocation = null;
            qrTextureModifiedAt = -1L;
            return false;
        }
    }

    private void unloadQrTexture() {
        if (qrTextureLocation != null && this.minecraft != null) {
            this.minecraft.getTextureManager().release(qrTextureLocation);
        }
        qrTextureLocation = null;
        qrTextureModifiedAt = -1L;
        qrTextureWidth = 0;
        qrTextureHeight = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        ConnectionLayout.Layout layout = this.currentLayout != null
            ? this.currentLayout
            : ConnectionLayout.calculate(this.width, this.height);
        if (layout.helpViewport().contains(mouseX, mouseY) && this.maxHelpScroll > 0) {
            this.helpScrollOffset = DiagnosticLayout.scrollByWheel(
                this.helpScrollOffset, scrollY, this.maxHelpScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public void removed() {
        unloadQrTexture();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private record HelpLine(FormattedCharSequence text, int color, int height) {
    }
}

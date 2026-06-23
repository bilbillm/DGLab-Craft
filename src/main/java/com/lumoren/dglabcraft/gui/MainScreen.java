package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.literal("DGLab Craft"));
    }

    private Button settingsButton;
    private Button connectionButton;
    private Button diagnosticButton;
    private Button waveformButton;
    private Button hudToggleButton;
    private Button waveformOverlayButton;
    private Button editOverlaysButton;
    private Button closeButton;

    @Override
    protected void init() {
        super.init();

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        if (!server.isConnected()) {
            server.generateQrUrl();
        }

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;
        int startY = this.height / 2 - 40;

        this.settingsButton = new Button(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight,
            Component.translatable("button.dglabcraft.strength_settings"),
            button -> this.minecraft.setScreen(new DGLabCraftScreen(this)));
        this.addRenderableWidget(this.settingsButton);

        this.connectionButton = new Button(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight,
            Component.translatable("button.dglabcraft.connection_settings"),
            button -> this.minecraft.setScreen(new ConnectionScreen(this)));
        this.addRenderableWidget(this.connectionButton);

        this.diagnosticButton = new Button(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight,
            Component.translatable("button.dglabcraft.diagnostics"),
            button -> this.minecraft.setScreen(new DiagnosticScreen(this)));
        this.addRenderableWidget(this.diagnosticButton);

        this.waveformButton = new Button(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight,
            Component.translatable("button.dglabcraft.waveform_settings_soon"),
            button -> {
            });
        this.addRenderableWidget(this.waveformButton);

        int btnWidth = 98;
        int btnGap = 4;
        int startX = centerX - buttonWidth / 2;
        boolean hudEnabled = ModConfig.HUD_ENABLED.get();
        this.hudToggleButton = new Button(startX, startY + spacing * 4, btnWidth, buttonHeight,
            hudToggleText(hudEnabled),
            button -> {
                boolean newState = !ModConfig.HUD_ENABLED.get();
                ModConfig.HUD_ENABLED.set(newState);
                ModConfig.save();
                button.setMessage(hudToggleText(newState));
            });
        this.addRenderableWidget(this.hudToggleButton);

        boolean waveformEnabled = ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
        this.waveformOverlayButton = new Button(startX + btnWidth + btnGap, startY + spacing * 4, btnWidth, buttonHeight,
            waveformOverlayText(waveformEnabled),
            button -> {
                boolean newState = !ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
                ModConfig.WAVEFORM_OVERLAY_ENABLED.set(newState);
                ModConfig.save();
                button.setMessage(waveformOverlayText(newState));
            });
        this.addRenderableWidget(this.waveformOverlayButton);

        this.editOverlaysButton = new Button(startX, startY + spacing * 5, btnWidth, buttonHeight,
            Component.translatable("button.dglabcraft.edit_overlays"),
            button -> this.minecraft.setScreen(new OverlayEditScreen(this)));
        this.addRenderableWidget(this.editOverlaysButton);

        this.closeButton = new Button(startX + btnWidth + btnGap, startY + spacing * 5, btnWidth, buttonHeight,
            Component.translatable("button.dglabcraft.close_screen"),
            button -> this.onClose());
        this.addRenderableWidget(this.closeButton);
    }

    private Component hudToggleText(boolean enabled) {
        return Component.translatable("button.dglabcraft.hud_toggle",
            Component.translatable(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    private Component waveformOverlayText(boolean enabled) {
        return Component.translatable("button.dglabcraft.waveform_overlay",
            Component.translatable(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int centerX = this.width / 2;
        drawCenteredString(poseStack, this.font, Component.literal("DGLab Craft"), centerX, 30, 0xFFFFFF);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        Component statusText;
        int statusColor;
        if (server.isConnected()) {
            statusText = Component.translatable("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = Component.translatable("status.dglabcraft.waiting_connection");
            statusColor = 0xFFFF00;
        }
        drawCenteredString(poseStack, this.font, statusText, centerX, 50, statusColor);

        String serverInfo = server.resolveConnectionHost() + ":" + server.getPort();
        drawCenteredString(poseStack, this.font, serverInfo, centerX, this.height - 20, 0x888888);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}

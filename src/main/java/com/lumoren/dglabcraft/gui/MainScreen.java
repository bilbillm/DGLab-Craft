package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiGraphics;
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

        this.settingsButton = Button.builder(Component.translatable("button.dglabcraft.strength_settings"), button -> {
            this.minecraft.setScreen(new DGLabCraftScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.settingsButton);

        this.connectionButton = Button.builder(Component.translatable("button.dglabcraft.connection_settings"), button -> {
            this.minecraft.setScreen(new ConnectionScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.connectionButton);

        this.diagnosticButton = Button.builder(Component.translatable("button.dglabcraft.diagnostics"), button -> {
            this.minecraft.setScreen(new DiagnosticScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.diagnosticButton);

        this.waveformButton = Button.builder(Component.translatable("button.dglabcraft.waveform_settings_soon"), button -> {
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.waveformButton);

        int btnWidth = 98;
        int btnGap = 4;
        int startX = centerX - buttonWidth / 2;
        boolean hudEnabled = DGLabConfig.HUD_ENABLED.get();
        this.hudToggleButton = Button.builder(hudToggleText(hudEnabled), button -> {
            boolean newState = !DGLabConfig.HUD_ENABLED.get();
            DGLabConfig.HUD_ENABLED.set(newState);
            DGLabConfig.save();
            button.setMessage(hudToggleText(newState));
        }).bounds(startX, startY + spacing * 4, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.hudToggleButton);

        boolean waveformEnabled = DGLabConfig.WAVEFORM_OVERLAY_ENABLED.get();
        this.waveformOverlayButton = Button.builder(waveformOverlayText(waveformEnabled), button -> {
            boolean newState = !DGLabConfig.WAVEFORM_OVERLAY_ENABLED.get();
            DGLabConfig.WAVEFORM_OVERLAY_ENABLED.set(newState);
            DGLabConfig.save();
            button.setMessage(waveformOverlayText(newState));
        }).bounds(startX + btnWidth + btnGap, startY + spacing * 4, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.waveformOverlayButton);

        this.editOverlaysButton = Button.builder(Component.translatable("button.dglabcraft.edit_overlays"), button -> {
            this.minecraft.setScreen(new OverlayEditScreen(this));
        }).bounds(startX, startY + spacing * 5, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.editOverlaysButton);

        this.closeButton = Button.builder(Component.translatable("button.dglabcraft.close_screen"), button -> this.onClose())
            .bounds(startX + btnWidth + btnGap, startY + spacing * 5, btnWidth, buttonHeight).build();
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
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        int centerX = this.width / 2;
        guiGraphics.drawCenteredString(this.font, Component.literal("DGLab Craft"), centerX, 30, 0xFFFFFF);

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
        guiGraphics.drawCenteredString(this.font, statusText, centerX, 50, statusColor);

        String serverInfo = server.resolveConnectionHost() + ":" + server.getPort();
        guiGraphics.drawCenteredString(this.font, Component.literal(serverInfo), centerX, this.height - 20, 0x888888);
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

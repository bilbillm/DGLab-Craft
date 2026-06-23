package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * DGLab Craft 主界面
 * 显示连接状态和快捷按钮
 */
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

        // 强度设置按钮
        this.settingsButton = Button.builder(Component.translatable("button.dglabcraft.strength_settings"), button -> {
            this.minecraft.setScreen(new DGLabCraftScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.settingsButton);

        // 连接设置按钮
        this.connectionButton = Button.builder(Component.translatable("button.dglabcraft.connection_settings"), button -> {
            this.minecraft.setScreen(new ConnectionScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.connectionButton);

        // 连接诊断按钮
        this.diagnosticButton = Button.builder(Component.translatable("button.dglabcraft.diagnostics"), button -> {
            this.minecraft.setScreen(new DiagnosticScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.diagnosticButton);

        // 波形设置按钮 (敬请期待)
        this.waveformButton = Button.builder(Component.translatable("button.dglabcraft.waveform_settings_soon"), button -> {
            // TODO: 波形设置界面
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.waveformButton);

        // HUD 开关、波形开关、叠加层编辑和关闭界面按钮
        int btnWidth = 98;
        int btnGap = 4;
        int startX = centerX - buttonWidth / 2;
        boolean hudEnabled = ModConfig.HUD_ENABLED.get();
        this.hudToggleButton = Button.builder(hudToggleText(hudEnabled), button -> {
            boolean newState = !ModConfig.HUD_ENABLED.get();
            ModConfig.HUD_ENABLED.set(newState);
            ModConfig.save();
            button.setMessage(hudToggleText(newState));
        }).bounds(startX, startY + spacing * 4, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.hudToggleButton);

        boolean waveformEnabled = ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
        this.waveformOverlayButton = Button.builder(waveformOverlayText(waveformEnabled), button -> {
            boolean newState = !ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
            ModConfig.WAVEFORM_OVERLAY_ENABLED.set(newState);
            ModConfig.save();
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

    /**
     * 获取 HUD 位置对应的中文文本
     */
    private Component getPositionText(int pos) {
        switch (pos) {
            case 0: return Component.translatable("position.dglabcraft.top_left");
            case 1: return Component.translatable("position.dglabcraft.top_right");
            case 2: return Component.translatable("position.dglabcraft.bottom_left");
            case 3: return Component.translatable("position.dglabcraft.bottom_right");
            default: return Component.translatable("position.dglabcraft.unknown");
        }
    }

    private Component hudToggleText(boolean enabled) {
        return Component.translatable("button.dglabcraft.hud_toggle",
            Component.translatable(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    private Component hudPositionText(int pos) {
        return Component.translatable("button.dglabcraft.hud_position", getPositionText(pos));
    }

    private Component waveformOverlayText(boolean enabled) {
        return Component.translatable("button.dglabcraft.waveform_overlay",
            Component.translatable(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;

        // 标题
        guiGraphics.drawCenteredString(this.font, Component.literal("DGLab Craft"), centerX, 30, 0xFFFFFF);

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
        String serverInfo = server.resolveConnectionHost() + ":" + server.getPort();
        guiGraphics.drawCenteredString(this.font, Component.literal(serverInfo), centerX, this.height - 20, 0x888888);

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        // 返回游戏
        this.minecraft.setScreen(null);
    }
}

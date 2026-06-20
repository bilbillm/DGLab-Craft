package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class MainScreen extends Screen {

    public MainScreen() {
        super(new TextComponent("DGLab Craft"));
    }

    private Button settingsButton;
    private Button connectionButton;
    private Button diagnosticButton;
    private Button waveformButton;
    private Button hudToggleButton;
    private Button hudPositionButton;
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
            new TranslatableComponent("button.dglabcraft.strength_settings"),
            button -> this.minecraft.setScreen(new DGLabCraftScreen(this)));
        this.addRenderableWidget(this.settingsButton);

        this.connectionButton = new Button(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.connection_settings"),
            button -> this.minecraft.setScreen(new ConnectionScreen(this)));
        this.addRenderableWidget(this.connectionButton);

        this.diagnosticButton = new Button(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.diagnostics"),
            button -> this.minecraft.setScreen(new DiagnosticScreen(this)));
        this.addRenderableWidget(this.diagnosticButton);

        this.waveformButton = new Button(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.waveform_settings_soon"),
            button -> {
            });
        this.addRenderableWidget(this.waveformButton);

        int btnWidth = 66;
        int btnGap = 1;
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

        int currentPos = ModConfig.HUD_POSITION.get();
        this.hudPositionButton = new Button(startX + btnWidth + btnGap, startY + spacing * 4, btnWidth, buttonHeight,
            hudPositionText(currentPos),
            button -> {
                int newPos = (ModConfig.HUD_POSITION.get() + 1) % 4;
                ModConfig.HUD_POSITION.set(newPos);
                ModConfig.save();
                button.setMessage(hudPositionText(newPos));
            });
        this.addRenderableWidget(this.hudPositionButton);

        this.closeButton = new Button(startX + (btnWidth + btnGap) * 2, startY + spacing * 4, btnWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.close_screen"),
            button -> this.onClose());
        this.addRenderableWidget(this.closeButton);
    }

    private Component getPositionText(int pos) {
        switch (pos) {
            case 0: return new TranslatableComponent("position.dglabcraft.top_left");
            case 1: return new TranslatableComponent("position.dglabcraft.top_right");
            case 2: return new TranslatableComponent("position.dglabcraft.bottom_left");
            case 3: return new TranslatableComponent("position.dglabcraft.bottom_right");
            default: return new TranslatableComponent("position.dglabcraft.unknown");
        }
    }

    private Component hudToggleText(boolean enabled) {
        return new TranslatableComponent("button.dglabcraft.hud_toggle",
            new TranslatableComponent(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    private Component hudPositionText(int pos) {
        return new TranslatableComponent("button.dglabcraft.hud_position", getPositionText(pos));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int centerX = this.width / 2;
        drawCenteredString(poseStack, this.font, new TextComponent("DGLab Craft"), centerX, 30, 0xFFFFFF);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        Component statusText;
        int statusColor;
        if (server.isConnected()) {
            statusText = new TranslatableComponent("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = new TranslatableComponent("status.dglabcraft.waiting_connection");
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

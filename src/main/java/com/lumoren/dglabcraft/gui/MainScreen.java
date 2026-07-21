package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MainScreen extends Screen {

    public MainScreen() {
        super(new StringTextComponent("DGLab Craft"));
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
            new TranslationTextComponent("button.dglabcraft.strength_settings"),
            button -> this.minecraft.setScreen(new DGLabCraftScreen(this)));
        this.addButton(this.settingsButton);

        this.connectionButton = new Button(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight,
            new TranslationTextComponent("button.dglabcraft.connection_settings"),
            button -> this.minecraft.setScreen(new ConnectionScreen(this)));
        this.addButton(this.connectionButton);

        this.diagnosticButton = new Button(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight,
            new TranslationTextComponent("button.dglabcraft.diagnostics"),
            button -> this.minecraft.setScreen(new DiagnosticScreen(this)));
        this.addButton(this.diagnosticButton);

        this.waveformButton = new Button(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight,
            new TranslationTextComponent("button.dglabcraft.waveform_settings_soon"),
            button -> {
            });
        this.addButton(this.waveformButton);

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
        this.addButton(this.hudToggleButton);

        boolean waveformEnabled = ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
        this.waveformOverlayButton = new Button(startX + btnWidth + btnGap, startY + spacing * 4, btnWidth, buttonHeight,
            waveformOverlayText(waveformEnabled),
            button -> {
                boolean newState = !ModConfig.WAVEFORM_OVERLAY_ENABLED.get();
                ModConfig.WAVEFORM_OVERLAY_ENABLED.set(newState);
                ModConfig.save();
                button.setMessage(waveformOverlayText(newState));
            });
        this.addButton(this.waveformOverlayButton);

        this.editOverlaysButton = new Button(startX, startY + spacing * 5, btnWidth, buttonHeight,
            new TranslationTextComponent("button.dglabcraft.edit_overlays"),
            button -> this.minecraft.setScreen(new OverlayEditScreen(this)));
        this.addButton(this.editOverlaysButton);

        this.closeButton = new Button(startX + btnWidth + btnGap, startY + spacing * 5, btnWidth, buttonHeight,
            new TranslationTextComponent("button.dglabcraft.close_screen"),
            button -> this.onClose());
        this.addButton(this.closeButton);
    }

    private ITextComponent hudToggleText(boolean enabled) {
        return new TranslationTextComponent("button.dglabcraft.hud_toggle",
            new TranslationTextComponent(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    private ITextComponent waveformOverlayText(boolean enabled) {
        return new TranslationTextComponent("button.dglabcraft.waveform_overlay",
            new TranslationTextComponent(enabled ? "status.dglabcraft.on" : "status.dglabcraft.off"));
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int centerX = this.width / 2;
        drawCenteredString(poseStack, this.font, new StringTextComponent("DGLab Craft"), centerX, 30, 0xFFFFFF);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        ITextComponent statusText;
        int statusColor;
        if (server.isConnected()) {
            statusText = new TranslationTextComponent("status.dglabcraft.connected");
            statusColor = 0x00FF00;
        } else {
            statusText = new TranslationTextComponent("status.dglabcraft.waiting_connection");
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

package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;


public class MainScreen extends GuiScreen {
    private static final int BTN_SETTINGS = 1;
    private static final int BTN_CONNECTION = 2;
    private static final int BTN_DIAGNOSTIC = 3;
    private static final int BTN_HUD = 4;
    private static final int BTN_WAVEFORM = 5;
    private static final int BTN_EDIT = 6;
    private static final int BTN_CLOSE = 7;

    @Override
    public void initGui() {
        buttonList.clear();
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        if (!server.isConnected()) {
            server.generateQrUrl();
        }
        int centerX = width / 2;
        int buttonWidth = 200;
        int startY = height / 2 - 55;
        buttonList.add(new GuiButton(BTN_SETTINGS, centerX - buttonWidth / 2, startY, buttonWidth, 20, "强度设置"));
        buttonList.add(new GuiButton(BTN_CONNECTION, centerX - buttonWidth / 2, startY + 25, buttonWidth, 20, "连接设置"));
        buttonList.add(new GuiButton(BTN_DIAGNOSTIC, centerX - buttonWidth / 2, startY + 50, buttonWidth, 20, "连接诊断"));
        buttonList.add(new GuiButton(BTN_HUD, centerX - buttonWidth / 2, startY + 75, 98, 20, hudText()));
        buttonList.add(new GuiButton(BTN_WAVEFORM, centerX + 2, startY + 75, 98, 20, waveformText()));
        buttonList.add(new GuiButton(BTN_EDIT, centerX - buttonWidth / 2, startY + 100, 98, 20, "编辑叠加层"));
        buttonList.add(new GuiButton(BTN_CLOSE, centerX + 2, startY + 100, 98, 20, "关闭"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_SETTINGS) {
            mc.displayGuiScreen(new DGLabCraftScreen(this));
        } else if (button.id == BTN_CONNECTION) {
            mc.displayGuiScreen(new ConnectionScreen(this));
        } else if (button.id == BTN_DIAGNOSTIC) {
            mc.displayGuiScreen(new DiagnosticScreen(this));
        } else if (button.id == BTN_HUD) {
            ModConfig.HUD_ENABLED.set(!ModConfig.HUD_ENABLED.get());
            ModConfig.save();
            button.displayString = hudText();
        } else if (button.id == BTN_WAVEFORM) {
            ModConfig.WAVEFORM_OVERLAY_ENABLED.set(!ModConfig.WAVEFORM_OVERLAY_ENABLED.get());
            ModConfig.save();
            button.displayString = waveformText();
        } else if (button.id == BTN_EDIT) {
            mc.displayGuiScreen(new OverlayEditScreen(this));
        } else if (button.id == BTN_CLOSE) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "DGLab Craft", width / 2, 30, 0xFFFFFF);
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        drawCenteredString(fontRendererObj, server.isConnected() ? "已连接" : "正在等待连接", width / 2, 50, server.isConnected() ? 0x55FF88 : 0xFFFF55);
        drawCenteredString(fontRendererObj, server.resolveConnectionHost() + ":" + server.getPort(), width / 2, height - 20, 0x888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private static String hudText() {
        return "HUD: " + (ModConfig.HUD_ENABLED.get() ? "开" : "关");
    }

    private static String waveformText() {
        return "波形: " + (ModConfig.WAVEFORM_OVERLAY_ENABLED.get() ? "开" : "关");
    }
}

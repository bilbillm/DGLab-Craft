package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;


public class OverlayEditScreen extends GuiScreen {
    private static final int BTN_RESET = 1;
    private static final int BTN_DONE = 2;
    private final GuiScreen parent;
    private String activePanel = "";
    private boolean resizing;
    private OverlayLayout.ResizeHandle resizeHandle = OverlayLayout.ResizeHandle.NONE;
    private double grabOffsetX;
    private double grabOffsetY;
    private OverlayLayout.Rect hudRect;
    private OverlayLayout.Rect waveformRect;

    public OverlayEditScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(BTN_RESET, width / 2 - 105, height - 30, 100, 20, "重置"));
        buttonList.add(new GuiButton(BTN_DONE, width / 2 + 5, height - 30, 100, 20, "完成"));
        hudRect = DGLabCraftHUD.hudRect(width, height);
        waveformRect = DGLabCraftHUD.waveformRect(width, height);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_RESET) {
            ModConfig.HUD_X_RATIO.set(-1.0D);
            ModConfig.HUD_Y_RATIO.set(-1.0D);
            ModConfig.HUD_SCALE.set(1.0D);
            ModConfig.WAVEFORM_X_RATIO.set(-1.0D);
            ModConfig.WAVEFORM_Y_RATIO.set(-1.0D);
            ModConfig.WAVEFORM_SCALE.set(1.0D);
            ModConfig.save();
            initGui();
        } else if (button.id == BTN_DONE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (waveformRect.contains(mouseX, mouseY)) {
            beginDrag("waveform", waveformRect, mouseX, mouseY);
        } else if (hudRect.contains(mouseX, mouseY)) {
            beginDrag("hud", hudRect, mouseX, mouseY);
        }
    }

    private void beginDrag(String panel, OverlayLayout.Rect rect, int mouseX, int mouseY) {
        activePanel = panel;
        resizeHandle = OverlayLayout.resizeHandle(rect, mouseX, mouseY, 6);
        resizing = resizeHandle != OverlayLayout.ResizeHandle.NONE;
        grabOffsetX = mouseX - rect.x();
        grabOffsetY = mouseY - rect.y();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (activePanel.length() == 0) {
            return;
        }
        if ("hud".equals(activePanel)) {
            hudRect = resizing
                ? OverlayLayout.resize(hudRect, mouseX, mouseY, resizeHandle, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, width, height)
                : OverlayLayout.drag(hudRect, mouseX, mouseY, grabOffsetX, grabOffsetY, width, height);
        } else {
            waveformRect = resizing
                ? OverlayLayout.resize(waveformRect, mouseX, mouseY, resizeHandle, OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, width, height)
                : OverlayLayout.drag(waveformRect, mouseX, mouseY, grabOffsetX, grabOffsetY, width, height);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if ("hud".equals(activePanel)) {
            saveRect(hudRect, true);
        } else if ("waveform".equals(activePanel)) {
            saveRect(waveformRect, false);
        }
        activePanel = "";
        resizing = false;
    }

    private void saveRect(OverlayLayout.Rect rect, boolean hud) {
        if (hud) {
            ModConfig.HUD_X_RATIO.set(OverlayLayout.ratioFromPixel(rect.x(), rect.width(), width));
            ModConfig.HUD_Y_RATIO.set(OverlayLayout.ratioFromPixel(rect.y(), rect.height(), height));
            ModConfig.HUD_SCALE.set(OverlayLayout.scaleFromRect(rect, OverlayLayout.HUD_WIDTH));
        } else {
            ModConfig.WAVEFORM_X_RATIO.set(OverlayLayout.ratioFromPixel(rect.x(), rect.width(), width));
            ModConfig.WAVEFORM_Y_RATIO.set(OverlayLayout.ratioFromPixel(rect.y(), rect.height(), height));
            ModConfig.WAVEFORM_SCALE.set(OverlayLayout.scaleFromRect(rect, OverlayLayout.WAVEFORM_WIDTH));
        }
        ModConfig.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "拖动叠加层，拖边框调整大小", width / 2, 18, 0xFFFFFF);
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        DGLabCraftHUD.renderHudPanel(fontRendererObj, server, hudRect, true);
        DGLabCraftHUD.renderWaveformPanel(fontRendererObj, server, waveformRect, true);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class OverlayEditScreen extends Screen {
    private static final int RESIZE_BORDER = 8;

    private final Screen parent;
    private DragTarget dragTarget = DragTarget.NONE;
    private OverlayLayout.ResizeHandle resizeHandle = OverlayLayout.ResizeHandle.NONE;
    private OverlayLayout.Rect dragStartRect;
    private double grabOffsetX;
    private double grabOffsetY;

    public OverlayEditScreen(Screen parent) {
        super(Component.translatable("screen.dglabcraft.edit_overlays"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int gap = 6;
        int centerX = this.width / 2;
        int y = this.height - 28;

        this.addRenderableWidget(Button.builder(Component.translatable("button.dglabcraft.reset_overlay_positions"), button -> {
            DGLabConfig.HUD_X_RATIO.set(-1.0D);
            DGLabConfig.HUD_Y_RATIO.set(-1.0D);
            DGLabConfig.HUD_SCALE.set(1.0D);
            DGLabConfig.WAVEFORM_X_RATIO.set(-1.0D);
            DGLabConfig.WAVEFORM_Y_RATIO.set(-1.0D);
            DGLabConfig.WAVEFORM_SCALE.set(1.0D);
            DGLabConfig.save();
        }).bounds(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(Component.translatable("button.dglabcraft.done"), button -> this.onClose())
            .bounds(centerX + gap / 2, y, buttonWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        guiGraphics.centeredText(this.font, Component.translatable("message.dglabcraft.drag_overlay_hint"),
            this.width / 2, 32, 0xCCCCCC);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        DGLabCraftHUD.renderHudPanel(guiGraphics, this.font, server, DGLabCraftHUD.hudRect(this.width, this.height), true);
        DGLabCraftHUD.renderWaveformPanel(guiGraphics, this.font, server, DGLabCraftHUD.waveformRect(this.width, this.height), true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0) {
            OverlayLayout.Rect waveformRect = DGLabCraftHUD.waveformRect(this.width, this.height);
            OverlayLayout.Rect hudRect = DGLabCraftHUD.hudRect(this.width, this.height);
            if (waveformRect.contains(mouseX, mouseY)) {
                beginDrag(DragTarget.WAVEFORM, waveformRect, mouseX, mouseY);
                return true;
            }
            if (hudRect.contains(mouseX, mouseY)) {
                beginDrag(DragTarget.HUD, hudRect, mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && this.dragTarget != DragTarget.NONE) {
            saveDraggedPosition(event.x(), event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.dragTarget != DragTarget.NONE) {
            saveDraggedPosition(event.x(), event.y());
            this.dragTarget = DragTarget.NONE;
            this.resizeHandle = OverlayLayout.ResizeHandle.NONE;
            this.dragStartRect = null;
            DGLabConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void beginDrag(DragTarget target, OverlayLayout.Rect rect, double mouseX, double mouseY) {
        this.dragTarget = target;
        this.dragStartRect = rect;
        this.resizeHandle = OverlayLayout.resizeHandle(rect, mouseX, mouseY, RESIZE_BORDER);
        this.grabOffsetX = mouseX - rect.x();
        this.grabOffsetY = mouseY - rect.y();
    }

    private void saveDraggedPosition(double mouseX, double mouseY) {
        OverlayLayout.Rect current = this.dragStartRect != null ? this.dragStartRect : (this.dragTarget == DragTarget.HUD
            ? DGLabCraftHUD.hudRect(this.width, this.height)
            : DGLabCraftHUD.waveformRect(this.width, this.height));
        int baseWidth = this.dragTarget == DragTarget.HUD ? OverlayLayout.HUD_WIDTH : OverlayLayout.WAVEFORM_WIDTH;
        int baseHeight = this.dragTarget == DragTarget.HUD ? OverlayLayout.HUD_HEIGHT : OverlayLayout.WAVEFORM_HEIGHT;
        OverlayLayout.Rect dragged = this.resizeHandle == OverlayLayout.ResizeHandle.NONE
            ? OverlayLayout.drag(current, mouseX, mouseY, this.grabOffsetX, this.grabOffsetY, this.width, this.height)
            : OverlayLayout.resize(current, mouseX, mouseY, this.resizeHandle, baseWidth, baseHeight, this.width, this.height);
        double xRatio = OverlayLayout.ratioFromPixel(dragged.x(), dragged.width(), this.width);
        double yRatio = OverlayLayout.ratioFromPixel(dragged.y(), dragged.height(), this.height);
        double scale = OverlayLayout.scaleFromRect(dragged, baseWidth);

        if (this.dragTarget == DragTarget.HUD) {
            DGLabConfig.HUD_X_RATIO.set(xRatio);
            DGLabConfig.HUD_Y_RATIO.set(yRatio);
            DGLabConfig.HUD_SCALE.set(scale);
        } else if (this.dragTarget == DragTarget.WAVEFORM) {
            DGLabConfig.WAVEFORM_X_RATIO.set(xRatio);
            DGLabConfig.WAVEFORM_Y_RATIO.set(yRatio);
            DGLabConfig.WAVEFORM_SCALE.set(scale);
        }
    }

    private enum DragTarget {
        NONE,
        HUD,
        WAVEFORM
    }
}

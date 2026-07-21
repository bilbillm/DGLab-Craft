package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class OverlayEditScreen extends Screen {
    private static final int RESIZE_BORDER = 8;

    private final Screen parent;
    private DragTarget dragTarget = DragTarget.NONE;
    private OverlayLayout.ResizeHandle resizeHandle = OverlayLayout.ResizeHandle.NONE;
    private OverlayLayout.Rect dragStartRect;
    private double grabOffsetX;
    private double grabOffsetY;

    public OverlayEditScreen(Screen parent) {
        super(new TranslatableComponent("screen.dglabcraft.edit_overlays"));
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

        this.addButton(new Button(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.reset_overlay_positions"),
            button -> {
                ModConfig.HUD_X_RATIO.set(-1.0D);
                ModConfig.HUD_Y_RATIO.set(-1.0D);
                ModConfig.HUD_SCALE.set(1.0D);
                ModConfig.WAVEFORM_X_RATIO.set(-1.0D);
                ModConfig.WAVEFORM_Y_RATIO.set(-1.0D);
                ModConfig.WAVEFORM_SCALE.set(1.0D);
                ModConfig.save();
            }));

        this.addButton(new Button(centerX + gap / 2, y, buttonWidth, buttonHeight,
            new TranslatableComponent("button.dglabcraft.done"),
            button -> this.onClose()));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        GuiComponent.fill(poseStack, 0, 0, this.width, this.height, 0x66000000);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, new TranslatableComponent("message.dglabcraft.drag_overlay_hint"),
            this.width / 2, 32, 0xCCCCCC);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        DGLabCraftHUD.renderHudPanel(poseStack, this.font, server, DGLabCraftHUD.hudRect(this.width, this.height), true);
        DGLabCraftHUD.renderWaveformPanel(poseStack, this.font, server, DGLabCraftHUD.waveformRect(this.width, this.height), true);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.dragTarget != DragTarget.NONE) {
            saveDraggedPosition(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.dragTarget != DragTarget.NONE) {
            saveDraggedPosition(mouseX, mouseY);
            this.dragTarget = DragTarget.NONE;
            this.resizeHandle = OverlayLayout.ResizeHandle.NONE;
            this.dragStartRect = null;
            ModConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
            ModConfig.HUD_X_RATIO.set(xRatio);
            ModConfig.HUD_Y_RATIO.set(yRatio);
            ModConfig.HUD_SCALE.set(scale);
        } else if (this.dragTarget == DragTarget.WAVEFORM) {
            ModConfig.WAVEFORM_X_RATIO.set(xRatio);
            ModConfig.WAVEFORM_Y_RATIO.set(yRatio);
            ModConfig.WAVEFORM_SCALE.set(scale);
        }
    }

    private enum DragTarget {
        NONE,
        HUD,
        WAVEFORM
    }
}

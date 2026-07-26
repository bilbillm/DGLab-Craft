package com.lumoren.dglabcraft.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class Slider extends GuiButton {
    public interface ValueConsumer {
        void accept(double value);
    }

    private final double minValue;
    private final double maxValue;
    private final double stepSize;
    private final String prefix;
    private final String suffix;
    private final ValueConsumer onValueChangeComplete;
    private double value;
    private boolean dragging;

    public Slider(int id, int x, int y, int width, int height, String prefix,
                  double minValue, double maxValue, double currentValue,
                  double stepSize, String suffix, ValueConsumer onValueChangeComplete) {
        super(id, x, y, width, height, "");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
        this.prefix = prefix;
        this.suffix = suffix;
        this.onValueChangeComplete = onValueChangeComplete;
        setActualValue(currentValue);
    }

    public double getValue() {
        double raw = minValue + (maxValue - minValue) * value;
        if (stepSize > 0.0D) {
            raw = Math.round(raw / stepSize) * stepSize;
        }
        return Math.max(minValue, Math.min(maxValue, raw));
    }

    public void setActualValue(double actualValue) {
        if (maxValue <= minValue) {
            this.value = 0.0D;
        } else {
            this.value = (actualValue - minValue) / (maxValue - minValue);
        }
        this.value = Math.max(0.0D, Math.min(1.0D, this.value));
        updateDisplayString();
    }

    @Override
    public int getHoverState(boolean mouseOver) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            if (dragging) {
                value = (mouseX - (xPosition + 4)) / (double) (width - 8);
                value = Math.max(0.0D, Math.min(1.0D, value));
                updateDisplayString();
            }
            mc.getTextureManager().bindTexture(buttonTextures);
            int handleX = xPosition + (int) (value * (width - 8));
            drawTexturedModalRect(handleX, yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(handleX + 4, yPosition, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            value = (mouseX - (xPosition + 4)) / (double) (width - 8);
            value = Math.max(0.0D, Math.min(1.0D, value));
            updateDisplayString();
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (dragging) {
            commitCurrentValue();
        }
        dragging = false;
    }

    public void commitCurrentValue() {
        if (onValueChangeComplete != null) {
            onValueChangeComplete.accept(getValue());
        }
    }

    private void updateDisplayString() {
        double actualValue = getValue();
        String valueString = stepSize < 1.0D ? String.format("%.1f", actualValue) : String.valueOf((int) Math.round(actualValue));
        this.displayString = prefix + valueString + suffix;
    }
}

package com.lumoren.dglabcraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.narration.NarrationElementOutput;

/**
 * 自定义滑动条组件 - 原版风格
 */
public class Slider extends net.minecraft.client.gui.components.AbstractWidget {
    private final float minValue;
    private final float maxValue;
    private float currentValue;
    private final String prefix;
    private final OnSliderChange callback;
    private boolean isDragging = false;

    public interface OnSliderChange {
        void onChange(float value);
    }

    public Slider(int x, int y, int width, String prefix, float minValue, float maxValue, float currentValue, OnSliderChange callback) {
        super(x, y, width, 20, Component.literal(prefix));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.prefix = prefix;
        this.callback = callback;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX >= this.x && mouseX < this.x + this.width &&
            mouseY >= this.y && mouseY < this.y + this.height) {
            updateValue(mouseX);
            isDragging = true;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        isDragging = false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (isDragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(double mouseX) {
        float percent = (float) ((mouseX - this.x) / this.width);
        percent = Math.max(0, Math.min(1, percent));
        currentValue = minValue + (maxValue - minValue) * percent;
        if (callback != null) {
            callback.onChange(currentValue);
        }
    }

    public void setValue(float value) {
        this.currentValue = value;
    }

    public float getValue() {
        return currentValue;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        // 不需要旁白功能
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = mouseX >= this.x && mouseY >= this.y &&
                         mouseX < this.x + this.width && mouseY < this.y + this.height;

        this.isHovered = hovered;

        // 绘制背景 - 使用更接近原版的颜色
        int bgColor = hovered ? 0xFF3D3D3D : 0xFF252525;
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, bgColor);

        // 绘制边框
        int borderColor = hovered ? 0xFFFFAA00 : 0xFF707070;
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + 1, borderColor);
        fill(pPoseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, borderColor);
        fill(pPoseStack, this.x, this.y, this.x + 1, this.y + this.height, borderColor);
        fill(pPoseStack, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, borderColor);

        // 绘制滑块轨道
        int trackY = this.y + this.height / 2 - 2;
        int trackHeight = 4;
        fill(pPoseStack, this.x + 4, trackY, this.x + this.width - 4, trackY + trackHeight, 0xFF505050);

        // 绘制滑块
        float percent = (currentValue - minValue) / (maxValue - minValue);
        int sliderWidth = 8;
        int sliderX = (int) (this.x + 4 + (this.width - 8) * percent);

        // 滑块颜色 - 悬停时更亮
        int sliderColor = hovered ? 0xFF55AAFF : 0xFF00AAFF;
        fill(pPoseStack, sliderX - sliderWidth/2, this.y + 2, sliderX + sliderWidth/2, this.y + this.height - 2, sliderColor);

        // 绘制标签和数值
        String valueStr;
        if (minValue < 1.0f) {
            // 浮点数范围（如 0.1-3.0），显示一位小数
            valueStr = String.format("%.1f", currentValue);
        } else if (maxValue <= 100 && minValue >= 0 && (maxValue - minValue) <= 100) {
            valueStr = String.valueOf(Math.round(currentValue));
        } else {
            valueStr = String.format("%.1f", currentValue);
        }
        String displayText = prefix + valueStr;

        // 绘制文字（居中）
        int textWidth = mc.font.width(displayText);
        int textX = this.x + (this.width - textWidth) / 2;
        mc.font.draw(pPoseStack, displayText, textX, this.y + 5, 0xFFFFFF);
    }
}

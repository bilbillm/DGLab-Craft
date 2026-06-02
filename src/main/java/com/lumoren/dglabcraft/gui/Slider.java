package com.lumoren.dglabcraft.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * 自定义滑动条组件 - 使用 Minecraft 原版 AbstractSliderButton
 * 严格对齐游戏设置中的 FOV 滑块样式
 */
public class Slider extends AbstractSliderButton {

    private final double minValue;
    private final double maxValue;
    private final double stepSize;
    private final String suffix;
    private final String prefixText;  // 保存原始前缀文本
    private final java.util.function.Consumer<Double> onValueChangeComplete;  // 释放鼠标时回调（保存配置）

    /**
     * 构造函数
     *
     * @param x                       组件 x 坐标
     * @param y                       组件 y 坐标
     * @param width                   组件宽度
     * @param height                  组件高度（通常为 20）
     * @param prefix                  前缀文本（如 "火焰伤害倍率: "）
     * @param minValue                最小值
     * @param maxValue                最大值
     * @param currentValue            当前值
     * @param stepSize                步长（例如 0.1 或 1.0）
     * @param suffix                  后缀文本（如 "x" 或 "%"）
     * @param onValueChangeComplete   释放鼠标时的回调（用于保存配置）
     */
    public Slider(int x, int y, int width, int height, Component prefix,
                  double minValue, double maxValue, double currentValue,
                  double stepSize, String suffix, java.util.function.Consumer<Double> onValueChangeComplete) {
        super(x, y, width, height, prefix, mapToInternalStatic(minValue, maxValue, currentValue));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
        this.suffix = suffix;
        this.prefixText = prefix.getString();  // 保存原始前缀文本
        this.onValueChangeComplete = onValueChangeComplete;

        this.updateMessage();
    }

    /**
     * 将外部实际值映射到内部 0.0-1.0 区间（静态方法）
     */
    private static double mapToInternalStatic(double minValue, double maxValue, double actualValue) {
        if (maxValue <= minValue) {
            return 0.0;
        }
        return (actualValue - minValue) / (maxValue - minValue);
    }

    /**
     * 将外部实际值映射到内部 0.0-1.0 区间（实例方法）
     */
    private double mapToInternal(double actualValue) {
        return mapToInternalStatic(minValue, maxValue, actualValue);
    }

    /**
     * 将内部 0.0-1.0 区间值映射回外部实际值
     */
    private double mapToExternal(double internalValue) {
        if (maxValue <= minValue) {
            return minValue;
        }
        double rawValue = minValue + (maxValue - minValue) * internalValue;

        // 应用步长约束
        if (stepSize > 0) {
            rawValue = Math.round(rawValue / stepSize) * stepSize;
        }

        // 限制在范围内
        return Math.max(minValue, Math.min(maxValue, rawValue));
    }

    /**
     * 获取当前的实际值
     */
    public double getValue() {
        return mapToExternal(this.value);
    }

    /**
     * 设置当前值
     */
    public void setValue(double value) {
        this.value = mapToInternal(value);
        this.updateMessage();
    }

    /**
     * 更新滑块显示的文本
     * 格式: [前缀]: [当前值][后缀]
     */
    @Override
    protected void updateMessage() {
        double actualValue = this.getValue();

        // 处理数值显示精度
        String valueStr;
        if (stepSize < 1.0) {
            // 浮点数范围（如 0.1-3.0），显示一位小数
            valueStr = String.format("%.1f", actualValue);
        } else {
            // 整数范围，不显示小数位
            valueStr = String.valueOf((int) Math.round(actualValue));
        }

        // 使用保存的原始前缀文本构建显示文本
        Component fullMessage = new net.minecraft.network.chat.TextComponent(prefixText + valueStr + suffix);
        this.setMessage(fullMessage);
    }

    /**
     * 当滑块被拖动时调用
     * 仅更新显示文本，不保存配置
     */
    @Override
    protected void applyValue() {
        // 拖动时只更新显示文本，不触发回调
        // 配置保存在 onRelease 中处理
    }

    /**
     * 当滑块释放时调用
     * 触发回调保存配置
     */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        commitCurrentValue();
    }

    public void commitCurrentValue() {
        double actualValue = this.getValue();
        if (onValueChangeComplete != null) {
            onValueChangeComplete.accept(actualValue);
        }
    }
}

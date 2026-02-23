package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * DGLab Craft 强度设置界面
 * 使用自定义 Slider 实现
 */
public class DGLabCraftScreen extends Screen {

    private final Screen parent;

    public DGLabCraftScreen(Screen parent) {
        super(Component.literal("DGLab 联动设置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int leftX = centerX - 155;
        int rightX = centerX + 5;
        int startY = 70;
        int spacing = 24;

        // ===== 左列滑块 (4个) =====

        // 1. 全局强度上限
        Slider baseMaxSlider = new Slider(
            leftX, startY, 150, "全局强度上限: ",
            0, 100, ModConfig.BASE_MAX_INTENSITY.get(), value -> {
                ModConfig.BASE_MAX_INTENSITY.set((int) value);
            }
        );
        this.addRenderableWidget(baseMaxSlider);

        // 2. 火焰/岩浆伤害
        Slider fireSlider = new Slider(
            leftX, startY + spacing, 150, "火焰伤害: ",
            0, 2.0f, ModConfig.FIRE_INTENSITY.get().floatValue(), value -> {
                ModConfig.FIRE_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(fireSlider);

        // 3. 跌落伤害
        Slider fallSlider = new Slider(
            leftX, startY + spacing * 2, 150, "跌落伤害: ",
            0, 2.0f, ModConfig.FALL_INTENSITY.get().floatValue(), value -> {
                ModConfig.FALL_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(fallSlider);

        // 4. 溺水伤害
        Slider drownSlider = new Slider(
            leftX, startY + spacing * 3, 150, "溺水伤害: ",
            0, 2.0f, ModConfig.DROWN_INTENSITY.get().floatValue(), value -> {
                ModConfig.DROWN_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(drownSlider);

        // ===== 右列滑块 (4个) =====

        // 5. 中毒伤害
        Slider poisonSlider = new Slider(
            rightX, startY, 150, "中毒伤害: ",
            0, 2.0f, ModConfig.POISON_INTENSITY.get().floatValue(), value -> {
                ModConfig.POISON_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(poisonSlider);

        // 6. 凋零伤害
        Slider witherSlider = new Slider(
            rightX, startY + spacing, 150, "凋零伤害: ",
            0, 2.0f, ModConfig.WITHER_INTENSITY.get().floatValue(), value -> {
                ModConfig.WITHER_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(witherSlider);

        // 7. 心跳阈值
        Slider heartbeatThresholdSlider = new Slider(
            rightX, startY + spacing * 2, 150, "心跳阈值: ",
            0, 10, ModConfig.HEARTBEAT_THRESHOLD.get().floatValue(), value -> {
                ModConfig.HEARTBEAT_THRESHOLD.set((double) value);
            }
        );
        this.addRenderableWidget(heartbeatThresholdSlider);

        // 8. 心跳强度
        Slider heartbeatIntensitySlider = new Slider(
            rightX, startY + spacing * 3, 150, "心跳强度: ",
            0, 2.0f, ModConfig.HEARTBEAT_INTENSITY.get().floatValue(), value -> {
                ModConfig.HEARTBEAT_INTENSITY.set((double) value);
            }
        );
        this.addRenderableWidget(heartbeatIntensitySlider);

        // ===== 底部完成按钮 =====
        Button doneButton = new Button(
            centerX - 100, this.height - 30, 200, 20,
            Component.literal("完成"),
            (button) -> {
                this.onClose();
            }
        );
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        int centerX = this.width / 2;

        // 标题
        drawCenteredString(pPoseStack, this.font, "DGLab 联动设置", centerX, 20, 0xFFFFFF);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}

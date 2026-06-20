package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DGLab Craft 设置界面 - 使用原版 ContainerObjectSelectionList
 */
public class DGLabCraftScreen extends Screen {

    private final Screen parent;
    private SettingsList list;
    private Button doneButton;
    private Button resetButton;
    // 实时更新标签
    private SettingsList.LabelEntry appStrengthLabel;
    private SettingsList.LabelEntry effectiveStrengthLabel;
    private SettingsList.LabelEntry heartbeatThresholdLabel;
    // 上一次的值，用于检测变化
    private int lastAppStrengthA = -1;
    private int lastAppStrengthB = -1;
    private int lastPercentage = -1;
    private int lastHeartbeatThreshold = -1;
    private boolean lastConnected = false;

    public DGLabCraftScreen(Screen parent) {
        super(Component.translatable("screen.dglabcraft.strength_settings"));
        this.parent = parent;
    }

    private static Component t(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static Component setting(String key) {
        return t(key).copy().append(": ");
    }

    private static Component onOff(boolean value) {
        return t(value ? "status.dglabcraft.on" : "status.dglabcraft.off");
    }

    private static Component syncButtonText(boolean value) {
        return t("button.dglabcraft.sync_channels", onOff(value));
    }

    private static Component appStrengthDisconnectedText() {
        return t("label.dglabcraft.app_strength.disconnected");
    }

    private static Component effectiveStrengthDisconnectedText() {
        return t("label.dglabcraft.effective_strength.disconnected");
    }

    private static Component appStrengthText(int strengthA, int strengthB) {
        return t("label.dglabcraft.app_strength", strengthA, strengthB);
    }

    private static Component effectiveStrengthText(int strengthA, int strengthB) {
        return t("label.dglabcraft.effective_strength", strengthA, strengthB);
    }

    private static Component heartbeatTriggerText(int health) {
        return t("label.dglabcraft.heartbeat_trigger", health);
    }

    @Override
    protected void init() {
        super.init();

        // 创建滚动列表
        this.list = new SettingsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.addWidget(this.list);

        // 构建设置项
        buildSettings();

        // 底部按钮
        int buttonWidth = 100;
        int buttonHeight = 20;
        int bottomY = this.height - 30;
        int centerX = this.width / 2;

        // 完成按钮 - 居中偏左
        this.doneButton = Button.builder(t("button.dglabcraft.done"), button -> this.onClose())
            .bounds(centerX - buttonWidth - 5, bottomY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(doneButton);

        // 重置按钮 - 居中偏右
        this.resetButton = Button.builder(t("button.dglabcraft.reset_defaults"), button -> resetToDefaults())
            .bounds(centerX + 5, bottomY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(resetButton);
    }

    /**
     * 构建所有设置项
     */
    private void buildSettings() {
        // ===== 通用与心跳设置 =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.general_heartbeat")));

        // 手机APP强度上限（只读显示）
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        boolean isConnected = ws.isConnected();
        if (!isConnected) {
            this.appStrengthLabel = new SettingsList.LabelEntry(appStrengthDisconnectedText());
            this.effectiveStrengthLabel = new SettingsList.LabelEntry(effectiveStrengthDisconnectedText());
        } else {
            int appMaxStrengthA = ws.getAppAMaxStrength();
            int appMaxStrengthB = ws.getAppBMaxStrength();
            this.appStrengthLabel = new SettingsList.LabelEntry(appStrengthText(appMaxStrengthA, appMaxStrengthB));

            // 全局强度上限百分比 + 实际强度上限
            int percentage = DGLabConfig.MAX_INTENSITY_PERCENTAGE.get().intValue();
            int effectiveMaxA = DGLabConfig.getEffectiveMaxIntensity(appMaxStrengthA);
            int effectiveMaxB = DGLabConfig.getEffectiveMaxIntensity(appMaxStrengthB);
            this.effectiveStrengthLabel = new SettingsList.LabelEntry(effectiveStrengthText(effectiveMaxA, effectiveMaxB));
        }
        this.list.addEntry(this.appStrengthLabel);
        this.list.addEntry(this.effectiveStrengthLabel);

        // 记录初始状态
        this.lastConnected = isConnected;
        this.lastPercentage = DGLabConfig.MAX_INTENSITY_PERCENTAGE.get().intValue();
        if (isConnected) {
            this.lastAppStrengthA = ws.getAppAMaxStrength();
            this.lastAppStrengthB = ws.getAppBMaxStrength();
        }

        // 全局强度上限百分比滑块
        int percentage = DGLabConfig.MAX_INTENSITY_PERCENTAGE.get().intValue();
        Slider percentageSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.max_intensity_percentage"),
            0, 100, percentage, 1.0, "%", value -> {
                DGLabConfig.MAX_INTENSITY_PERCENTAGE.set((double) value);
                DGLabConfig.save();
            }
        );

        // A/B通道同步
        Button syncButton = Button.builder(
            syncButtonText(DGLabConfig.SYNC_CHANNELS.get()),
            (button) -> {
                boolean newValue = !DGLabConfig.SYNC_CHANNELS.get();
                DGLabConfig.SYNC_CHANNELS.set(newValue);
                DGLabConfig.save();
                button.setMessage(syncButtonText(newValue));
            }
        ).bounds(0, 0, 190, 20).build();

        this.list.addEntry(new SettingsList.RowEntry(percentageSlider, syncButton));

        // 心跳阈值 + 心跳倍率
        Slider thresholdSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.heartbeat_threshold"),
            0, 100, DGLabConfig.HEARTBEAT_THRESHOLD.get(), 1.0, "%", value -> {
                DGLabConfig.HEARTBEAT_THRESHOLD.set(value);
                DGLabConfig.save();
            }
        );

        // 心跳触发阈值显示
        float maxHealth = HeartbeatHandler.getCurrentMaxHealth();
        int thresholdPercent = DGLabConfig.HEARTBEAT_THRESHOLD.get().intValue();
        int triggerHealth = (int) Math.ceil(maxHealth * thresholdPercent / 100.0);
        this.heartbeatThresholdLabel = new SettingsList.LabelEntry(heartbeatTriggerText(triggerHealth));

        Slider heartbeatSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.heartbeat_multiplier"),
            0, 5.0, DGLabConfig.HEARTBEAT_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.HEARTBEAT_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(thresholdSlider, heartbeatSlider));
        this.list.addEntry(this.heartbeatThresholdLabel);

        // ===== 锐器与穿刺倍率 (fast_pinch) =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.fast_pinch")));

        // 仙人掌 + 甜浆果丛
        Slider cactusSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.cactus"),
            0, 5.0, DGLabConfig.CACTUS_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.CACTUS_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider sweetberrySlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.sweetberry_bush"),
            0, 5.0, DGLabConfig.SWEETBERRY_BUSH_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.SWEETBERRY_BUSH_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(cactusSlider, sweetberrySlider));

        // 弓箭 + 三叉戟
        Slider arrowSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.arrow"),
            0, 5.0, DGLabConfig.ARROW_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.ARROW_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider tridentSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.trident"),
            0, 5.0, DGLabConfig.TRIDENT_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.TRIDENT_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(arrowSlider, tridentSlider));

        // 钟乳石 + (空)
        Slider stalagmiteSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.stalagmite"),
            0, 5.0, DGLabConfig.STALAGMITE_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.STALAGMITE_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(stalagmiteSlider, null));

        // ===== 钝器与撞击倍率 (beat) =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.beat")));

        // 跌落 + 生物攻击
        Slider fallSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.fall"),
            0, 5.0, DGLabConfig.FALL_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.FALL_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider mobAttackSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.mob_attack"),
            0, 5.0, DGLabConfig.MOB_ATTACK_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.MOB_ATTACK_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(fallSlider, mobAttackSlider));

        // 玩家攻击 + 撞墙
        Slider playerAttackSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.player_attack"),
            0, 5.0, DGLabConfig.PLAYER_ATTACK_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.PLAYER_ATTACK_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider flyIntoWallSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.fly_into_wall"),
            0, 5.0, DGLabConfig.FLY_INTO_WALL_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.FLY_INTO_WALL_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(playerAttackSlider, flyIntoWallSlider));

        // 爆炸 + 烟花
        Slider explosionSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.explosion"),
            0, 5.0, DGLabConfig.EXPLOSION_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.EXPLOSION_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider fireworksSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.fireworks"),
            0, 5.0, DGLabConfig.FIREWORKS_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.FIREWORKS_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(explosionSlider, fireworksSlider));

        // ===== 高温与灼烧倍率 (burn) =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.burn")));

        // 着火 + 火中
        Slider onFireSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.on_fire"),
            0, 5.0, DGLabConfig.ON_FIRE_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.ON_FIRE_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider inFireSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.in_fire"),
            0, 5.0, DGLabConfig.IN_FIRE_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.IN_FIRE_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(onFireSlider, inFireSlider));

        // 岩浆 + 烫脚
        Slider lavaSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.lava"),
            0, 5.0, DGLabConfig.LAVA_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.LAVA_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider hotFloorSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.hot_floor"),
            0, 5.0, DGLabConfig.HOT_FLOOR_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.HOT_FLOOR_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(lavaSlider, hotFloorSlider));

        // ===== 挤压与砸击倍率 (compress) =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.compress")));

        // 墙内窒息 + 实体挤压
        Slider inWallSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.in_wall"),
            0, 5.0, DGLabConfig.IN_WALL_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.IN_WALL_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider crammingSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.cramming"),
            0, 5.0, DGLabConfig.CRAMMING_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.CRAMMING_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(inWallSlider, crammingSlider));

        // 坠落方块 + 铁砧
        Slider fallingBlockSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.falling_block"),
            0, 5.0, DGLabConfig.FALLING_BLOCK_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.FALLING_BLOCK_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider anvilSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.anvil"),
            0, 5.0, DGLabConfig.ANVIL_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.ANVIL_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(fallingBlockSlider, anvilSlider));

        // ===== 异常状态与缺氧倍率 (drown & tide) =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.drown_tide")));

        // 溺水 + 细雪冰冻
        Slider drownSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.drown"),
            0, 5.0, DGLabConfig.DROWN_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.DROWN_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider freezeSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.freeze"),
            0, 5.0, DGLabConfig.FREEZE_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.FREEZE_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(drownSlider, freezeSlider));

        // 魔法 + 凋零
        Slider magicSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.magic"),
            0, 5.0, DGLabConfig.MAGIC_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.MAGIC_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider witherSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.wither"),
            0, 5.0, DGLabConfig.WITHER_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.WITHER_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(magicSlider, witherSlider));

        // 龙息 + 饥饿
        Slider dragonBreathSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.dragon_breath"),
            0, 5.0, DGLabConfig.DRAGON_BREATH_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.DRAGON_BREATH_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider starveSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.starve"),
            0, 5.0, DGLabConfig.STARVE_MULTIPLIER.get(), 0.1, "x", value -> {
                DGLabConfig.STARVE_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(dragonBreathSlider, starveSlider));

        // ===== 环境维度反馈倍率 =====
        this.list.addEntry(new SettingsList.HeaderEntry(t("header.dglabcraft.environment")));

        // 下界 + 末地
        Slider netherSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.nether"),
            0, 100, DGLabConfig.NETHER_MULTIPLIER.get(), 1, "%", value -> {
                DGLabConfig.NETHER_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        Slider endSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.end"),
            0, 100, DGLabConfig.END_MULTIPLIER.get(), 1, "%", value -> {
                DGLabConfig.END_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(netherSlider, endSlider));

        // 传送门 + (空)
        Slider portalSlider = new Slider(
            0, 0, 190, 20, setting("setting.dglabcraft.portal"),
            0, 100, DGLabConfig.PORTAL_MULTIPLIER.get(), 1, "%", value -> {
                DGLabConfig.PORTAL_MULTIPLIER.set(value);
                DGLabConfig.save();
            }
        );

        this.list.addEntry(new SettingsList.RowEntry(portalSlider, null));
    }

    /**
     * 重置为默认值
     */
    private void resetToDefaults() {
        // 全局
        DGLabConfig.MAX_INTENSITY_PERCENTAGE.set(100.0);
        DGLabConfig.SYNC_CHANNELS.set(true);

        // 心跳
        DGLabConfig.HEARTBEAT_THRESHOLD.set(30.0);
        DGLabConfig.HEARTBEAT_MULTIPLIER.set(1.0);

        // 锐器与穿刺
        DGLabConfig.CACTUS_MULTIPLIER.set(1.0);
        DGLabConfig.SWEETBERRY_BUSH_MULTIPLIER.set(1.0);
        DGLabConfig.ARROW_MULTIPLIER.set(1.0);
        DGLabConfig.TRIDENT_MULTIPLIER.set(1.0);
        DGLabConfig.STALAGMITE_MULTIPLIER.set(1.0);

        // 钝器与撞击
        DGLabConfig.FALL_MULTIPLIER.set(1.0);
        DGLabConfig.MOB_ATTACK_MULTIPLIER.set(1.0);
        DGLabConfig.PLAYER_ATTACK_MULTIPLIER.set(1.0);
        DGLabConfig.FLY_INTO_WALL_MULTIPLIER.set(1.0);
        DGLabConfig.EXPLOSION_MULTIPLIER.set(1.0);
        DGLabConfig.FIREWORKS_MULTIPLIER.set(1.0);

        // 高温与灼烧
        DGLabConfig.ON_FIRE_MULTIPLIER.set(1.0);
        DGLabConfig.IN_FIRE_MULTIPLIER.set(1.0);
        DGLabConfig.LAVA_MULTIPLIER.set(1.0);
        DGLabConfig.HOT_FLOOR_MULTIPLIER.set(1.0);

        // 挤压与窒息
        DGLabConfig.IN_WALL_MULTIPLIER.set(1.0);
        DGLabConfig.CRAMMING_MULTIPLIER.set(1.0);
        DGLabConfig.FALLING_BLOCK_MULTIPLIER.set(1.0);
        DGLabConfig.ANVIL_MULTIPLIER.set(1.0);

        // 环境与缺氧
        DGLabConfig.DROWN_MULTIPLIER.set(1.0);
        DGLabConfig.FREEZE_MULTIPLIER.set(1.0);

        // 魔法与毒素
        DGLabConfig.MAGIC_MULTIPLIER.set(1.0);
        DGLabConfig.WITHER_MULTIPLIER.set(1.0);
        DGLabConfig.DRAGON_BREATH_MULTIPLIER.set(1.0);
        DGLabConfig.STARVE_MULTIPLIER.set(1.0);

        // 环境维度
        DGLabConfig.NETHER_MULTIPLIER.set(20.0);
        DGLabConfig.END_MULTIPLIER.set(20.0);
        DGLabConfig.PORTAL_MULTIPLIER.set(20.0);

        // Buff
        DGLabConfig.BUFF_MULTIPLIER.set(1.0);

        DGLabConfig.save();

        // 重新初始化界面以反映新值
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        // 检测强度上限变化并更新显示
        updateStrengthLabels();

        // 渲染列表（原版泥土背景、阴影、滚动条由列表自动处理）
        this.list.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        // 渲染标题
        guiGraphics.drawCenteredString(this.font, t("screen.dglabcraft.strength_settings"), this.width / 2, 20, 0xFFFFFF);

        // 渲染底部按钮
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    /**
     * 更新强度上限标签（实时检测变化）
     */
    private void updateStrengthLabels() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        boolean isConnected = ws.isConnected();

        // 检测连接状态变化
        if (isConnected != this.lastConnected) {
            this.lastConnected = isConnected;
            if (isConnected) {
                this.lastAppStrengthA = ws.getAppAMaxStrength();
                this.lastAppStrengthB = ws.getAppBMaxStrength();
            }
            // 重建整个界面以反映连接状态变化
            this.rebuildWidgets();
            return;
        }

        // 检测强度上限变化
        if (isConnected) {
            int currentAppStrengthA = ws.getAppAMaxStrength();
            int currentAppStrengthB = ws.getAppBMaxStrength();

            if (currentAppStrengthA != this.lastAppStrengthA || currentAppStrengthB != this.lastAppStrengthB) {
                this.lastAppStrengthA = currentAppStrengthA;
                this.lastAppStrengthB = currentAppStrengthB;

                // 更新标签文本
                this.appStrengthLabel.setText(appStrengthText(currentAppStrengthA, currentAppStrengthB));

                int effectiveMaxA = DGLabConfig.getEffectiveMaxIntensity(currentAppStrengthA);
                int effectiveMaxB = DGLabConfig.getEffectiveMaxIntensity(currentAppStrengthB);
                this.effectiveStrengthLabel.setText(effectiveStrengthText(effectiveMaxA, effectiveMaxB));
            }
        }

        // 检测百分比变化
        int currentPercentage = DGLabConfig.MAX_INTENSITY_PERCENTAGE.get().intValue();
        if (currentPercentage != this.lastPercentage) {
            this.lastPercentage = currentPercentage;
            if (isConnected) {
                int appStrengthA = ws.getAppAMaxStrength();
                int appStrengthB = ws.getAppBMaxStrength();
                int effectiveMaxA = DGLabConfig.getEffectiveMaxIntensity(appStrengthA);
                int effectiveMaxB = DGLabConfig.getEffectiveMaxIntensity(appStrengthB);
                this.effectiveStrengthLabel.setText(effectiveStrengthText(effectiveMaxA, effectiveMaxB));
            }
        }

        // 检测心跳阈值变化
        int currentThresholdPercent = DGLabConfig.HEARTBEAT_THRESHOLD.get().intValue();
        if (currentThresholdPercent != this.lastHeartbeatThreshold) {
            this.lastHeartbeatThreshold = currentThresholdPercent;
            // 更新心跳触发阈值显示
            float maxHealth = HeartbeatHandler.getCurrentMaxHealth();
            int triggerHealth = (int) Math.ceil(maxHealth * currentThresholdPercent / 100.0);
            this.heartbeatThresholdLabel.setText(heartbeatTriggerText(triggerHealth));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        commitAllSliders();
        this.minecraft.setScreen(this.parent);
    }

    private void commitAllSliders() {
        if (this.list == null) {
            return;
        }

        for (SettingsList.Entry entry : this.list.children()) {
            for (GuiEventListener child : entry.children()) {
                if (child instanceof Slider slider) {
                    slider.commitCurrentValue();
                }
            }
        }
    }

    // ========== 内部类：滚动列表 ==========

    /**
     * 设置列表 - 继承自 ContainerObjectSelectionList
     */
    static class SettingsList extends ContainerObjectSelectionList<SettingsList.Entry> {

        public SettingsList(net.minecraft.client.Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        protected int getScrollbarPosition() {
            return this.width - 10;
        }

        @Override
        public int getRowWidth() {
            return 400;
        }

        @Override
        protected boolean isSelectedItem(int pIndex) {
            return false;
        }

        /**
         * 公开的添加条目方法
         */
        public int addEntry(Entry entry) {
            return super.addEntry(entry);
        }

        // ========== SettingsList 的内部类 ==========

        /**
         * 设置项基类
         */
        abstract static class Entry extends ContainerObjectSelectionList.Entry<SettingsList.Entry> {
            protected final net.minecraft.client.Minecraft minecraft;

            protected Entry() {
                this.minecraft = net.minecraft.client.Minecraft.getInstance();
            }
        }

        /**
         * 标题条目 - 用于显示分类标题
         */
        static class HeaderEntry extends Entry {
            private final Component title;

            public HeaderEntry(Component title) {
                super();
                this.title = title;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                // 渲染深色背景条
                guiGraphics.fill(left + 10, top, left + entryWidth - 10, top + entryHeight, 0x4D000000);

                // 渲染居中标题文本（带阴影）
                int textWidth = this.minecraft.font.width(this.title);
                guiGraphics.drawString(this.minecraft.font, this.title, left + (entryWidth - textWidth) / 2, top + 7, 0xFFFFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }

        /**
         * 标签条目 - 用于显示只读文本（支持动态更新）
         */
        static class LabelEntry extends Entry {
            private Component text;

            public LabelEntry(Component text) {
                super();
                this.text = text;
            }

            public void setText(Component text) {
                this.text = text;
            }

            public Component getText() {
                return this.text;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                // 渲染标签文本
                guiGraphics.drawString(
                    net.minecraft.client.Minecraft.getInstance().font,
                    text,
                    left + 15,
                    top + 5,
                    0xFFFFFF);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }
        }

        /**
         * 行条目 - 用于显示设置项，支持双列两个 Widget
         */
        static class RowEntry extends Entry {
            private final AbstractWidget leftWidget;
            private final AbstractWidget rightWidget;
            private final List<AbstractWidget> widgets;

            public RowEntry(AbstractWidget left, AbstractWidget right) {
                super();
                this.leftWidget = left;
                this.rightWidget = right;
                this.widgets = new ArrayList<>();
                if (leftWidget != null) widgets.add(leftWidget);
                if (rightWidget != null) widgets.add(rightWidget);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                int gap = 15;
                int widgetWidth = (entryWidth - gap - 20) / 2;

                // 渲染左侧 Widget
                if (leftWidget != null) {
                    leftWidget.setX(left + 10);
                    leftWidget.setY(top + 2);
                    leftWidget.setWidth(widgetWidth);
                    leftWidget.render(guiGraphics, mouseX, mouseY, partialTick);
                }

                // 渲染右侧 Widget
                if (rightWidget != null) {
                    rightWidget.setX(left + 10 + widgetWidth + gap);
                    rightWidget.setY(top + 2);
                    rightWidget.setWidth(widgetWidth);
                    rightWidget.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return widgets;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return widgets;
            }
        }
    }
}

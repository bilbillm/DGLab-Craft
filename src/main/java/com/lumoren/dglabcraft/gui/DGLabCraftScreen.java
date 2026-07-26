package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class DGLabCraftScreen extends GuiScreen {
    private static final int BTN_SYNC = 1000;
    private static final int BTN_RESET = 1001;
    private static final int BTN_DONE = 1002;
    private final GuiScreen parent;
    private final List<Slider> sliders = new ArrayList<Slider>();
    private GuiButton draggedButton;
    private int scroll;

    public DGLabCraftScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        sliders.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(BTN_SYNC, centerX - 100, 48, 200, 20, syncText()));

        int y = 82;
        addSlider("全局强度上限: ", 0, 100, ModConfig.MAX_INTENSITY_PERCENTAGE.get(), 1, "%", y, new Slider.ValueConsumer() {
            public void accept(double value) { ModConfig.MAX_INTENSITY_PERCENTAGE.set(value); ModConfig.save(); }
        }); y += 24;
        addSlider("心跳阈值: ", 0, 100, ModConfig.HEARTBEAT_THRESHOLD.get(), 1, "%", y, new Slider.ValueConsumer() {
            public void accept(double value) { ModConfig.HEARTBEAT_THRESHOLD.set(value); ModConfig.save(); }
        }); y += 24;
        addSlider("心跳倍率: ", 0, 5, ModConfig.HEARTBEAT_MULTIPLIER.get(), 0.1, "x", y, new Slider.ValueConsumer() {
            public void accept(double value) { ModConfig.HEARTBEAT_MULTIPLIER.set(value); ModConfig.save(); }
        }); y += 30;

        addSlider("仙人掌: ", 0, 5, ModConfig.CACTUS_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.CACTUS_MULTIPLIER)); y += 24;
        addSlider("箭矢: ", 0, 5, ModConfig.ARROW_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.ARROW_MULTIPLIER)); y += 24;
        addSlider("跌落: ", 0, 5, ModConfig.FALL_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.FALL_MULTIPLIER)); y += 24;
        addSlider("生物攻击: ", 0, 5, ModConfig.MOB_ATTACK_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.MOB_ATTACK_MULTIPLIER)); y += 24;
        addSlider("玩家攻击: ", 0, 5, ModConfig.PLAYER_ATTACK_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.PLAYER_ATTACK_MULTIPLIER)); y += 24;
        addSlider("爆炸: ", 0, 5, ModConfig.EXPLOSION_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.EXPLOSION_MULTIPLIER)); y += 24;
        addSlider("着火: ", 0, 5, ModConfig.ON_FIRE_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.ON_FIRE_MULTIPLIER)); y += 24;
        addSlider("火中: ", 0, 5, ModConfig.IN_FIRE_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.IN_FIRE_MULTIPLIER)); y += 24;
        addSlider("岩浆: ", 0, 5, ModConfig.LAVA_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.LAVA_MULTIPLIER)); y += 24;
        addSlider("窒息: ", 0, 5, ModConfig.IN_WALL_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.IN_WALL_MULTIPLIER)); y += 24;
        addSlider("溺水: ", 0, 5, ModConfig.DROWN_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.DROWN_MULTIPLIER)); y += 24;
        addSlider("魔法: ", 0, 5, ModConfig.MAGIC_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.MAGIC_MULTIPLIER)); y += 24;
        addSlider("凋零: ", 0, 5, ModConfig.WITHER_MULTIPLIER.get(), 0.1, "x", y, set(ModConfig.WITHER_MULTIPLIER)); y += 30;
        addSlider("下界环境: ", 0, 100, ModConfig.NETHER_MULTIPLIER.get(), 1, "%", y, set(ModConfig.NETHER_MULTIPLIER)); y += 24;
        addSlider("末地环境: ", 0, 100, ModConfig.END_MULTIPLIER.get(), 1, "%", y, set(ModConfig.END_MULTIPLIER)); y += 24;
        addSlider("传送门: ", 0, 100, ModConfig.PORTAL_MULTIPLIER.get(), 1, "%", y, set(ModConfig.PORTAL_MULTIPLIER));

        buttonList.add(new GuiButton(BTN_RESET, centerX - 105, height - 30, 100, 20, "重置"));
        buttonList.add(new GuiButton(BTN_DONE, centerX + 5, height - 30, 100, 20, "完成"));
    }

    private Slider.ValueConsumer set(final ModConfig.Value<Double> configValue) {
        return new Slider.ValueConsumer() {
            public void accept(double value) {
                configValue.set(value);
                ModConfig.save();
            }
        };
    }

    private void addSlider(String prefix, double min, double max, double current, double step, String suffix, int y, Slider.ValueConsumer consumer) {
        Slider slider = new Slider(2000 + sliders.size(), width / 2 - 150, y, 300, 20, prefix, min, max, current, step, suffix, consumer);
        sliders.add(slider);
        buttonList.add(slider);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_SYNC) {
            ModConfig.SYNC_CHANNELS.set(!ModConfig.SYNC_CHANNELS.get());
            ModConfig.save();
            button.displayString = syncText();
        } else if (button.id == BTN_RESET) {
            resetToDefaults();
            initGui();
        } else if (button.id == BTN_DONE) {
            commitAllSliders();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? -18 : 18;
            scroll = Math.max(0, scroll);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "强度设置", width / 2, 18, 0xFFFFFF);
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        String strength = ws.isConnected()
            ? "App 上限 A=" + ws.getAppAMaxStrength() + " B=" + ws.getAppBMaxStrength()
            : "App 未连接";
        drawCenteredString(fontRendererObj, strength + " | 心跳触发: " + heartbeatTriggerHealth(), width / 2, 34, 0xCCCCCC);

        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button instanceof Slider) {
                button.yPosition -= scroll;
                boolean oldVisible = button.visible;
                button.visible = button.yPosition >= 70 && button.yPosition <= height - 42;
                button.drawButton(mc, mouseX, mouseY);
                button.visible = oldVisible;
                button.yPosition += scroll;
            } else {
                button.drawButton(mc, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button instanceof Slider) {
                button.yPosition -= scroll;
                if (button.mousePressed(mc, mouseX, mouseY)) {
                    draggedButton = button;
                    button.playPressSound(mc.getSoundHandler());
                    actionPerformed(button);
                }
                button.yPosition += scroll;
            } else if (button.mousePressed(mc, mouseX, mouseY)) {
                draggedButton = button;
                button.playPressSound(mc.getSoundHandler());
                actionPerformed(button);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggedButton != null) {
            draggedButton.yPosition -= draggedButton instanceof Slider ? scroll : 0;
            draggedButton.mouseReleased(mouseX, mouseY);
            draggedButton.yPosition += draggedButton instanceof Slider ? scroll : 0;
            draggedButton = null;
        }
    }

    private int heartbeatTriggerHealth() {
        return (int) Math.ceil(HeartbeatHandler.getCurrentMaxHealth() * ModConfig.HEARTBEAT_THRESHOLD.get() / 100.0D);
    }

    private String syncText() {
        return "A/B 同步: " + (ModConfig.SYNC_CHANNELS.get() ? "开" : "关");
    }

    private void commitAllSliders() {
        for (Slider slider : sliders) {
            slider.commitCurrentValue();
        }
    }

    private void resetToDefaults() {
        ModConfig.MAX_INTENSITY_PERCENTAGE.set(100.0D);
        ModConfig.SYNC_CHANNELS.set(true);
        ModConfig.HEARTBEAT_THRESHOLD.set(30.0D);
        ModConfig.HEARTBEAT_MULTIPLIER.set(1.0D);
        ModConfig.CACTUS_MULTIPLIER.set(1.0D);
        ModConfig.ARROW_MULTIPLIER.set(1.0D);
        ModConfig.FALL_MULTIPLIER.set(1.0D);
        ModConfig.MOB_ATTACK_MULTIPLIER.set(1.0D);
        ModConfig.PLAYER_ATTACK_MULTIPLIER.set(1.0D);
        ModConfig.EXPLOSION_MULTIPLIER.set(1.0D);
        ModConfig.ON_FIRE_MULTIPLIER.set(1.0D);
        ModConfig.IN_FIRE_MULTIPLIER.set(1.0D);
        ModConfig.LAVA_MULTIPLIER.set(1.0D);
        ModConfig.IN_WALL_MULTIPLIER.set(1.0D);
        ModConfig.DROWN_MULTIPLIER.set(1.0D);
        ModConfig.MAGIC_MULTIPLIER.set(1.0D);
        ModConfig.WITHER_MULTIPLIER.set(1.0D);
        ModConfig.NETHER_MULTIPLIER.set(20.0D);
        ModConfig.END_MULTIPLIER.set(20.0D);
        ModConfig.PORTAL_MULTIPLIER.set(20.0D);
        ModConfig.save();
    }
}

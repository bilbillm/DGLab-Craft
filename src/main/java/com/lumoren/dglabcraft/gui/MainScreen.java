package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * DGLab Craft 主界面
 * 显示连接状态和快捷按钮
 */
public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.literal("DGLab Craft"));
    }

    private Button settingsButton;
    private Button connectionButton;
    private Button waveformButton;
    private Button hudToggleButton;
    private Button hudPositionButton;
    private Button closeButton;

    @Override
    protected void init() {
        super.init();

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        if (!server.isConnected()) {
            server.generateQrUrl();
        }

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;
        int startY = this.height / 2 - 40;

        // 强度设置按钮
        this.settingsButton = Button.builder(Component.literal("强度设置"), button -> {
            this.minecraft.setScreen(new DGLabCraftScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.settingsButton);

        // 连接设置按钮
        this.connectionButton = Button.builder(Component.literal("连接设置"), button -> {
            this.minecraft.setScreen(new ConnectionScreen(this));
        }).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.connectionButton);

        // 波形设置按钮 (敬请期待)
        this.waveformButton = Button.builder(Component.literal("波形设置 (敬请期待)"), button -> {
            // TODO: 波形设置界面
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.waveformButton);

        // HUD 开关按钮 + HUD 位置切换按钮 + 关闭界面按钮（三列并排，总宽度200与上方按钮对齐）
        int btnWidth = 66;  // 66*3 + 1*2 = 200
        int btnGap = 1;     // 间距
        int startX = centerX - buttonWidth / 2;
        boolean hudEnabled = DGLabConfig.HUD_ENABLED.get();
        String hudText = hudEnabled ? "HUD: 开启" : "HUD: 关闭";
        this.hudToggleButton = Button.builder(Component.literal(hudText), button -> {
            boolean newState = !DGLabConfig.HUD_ENABLED.get();
            DGLabConfig.HUD_ENABLED.set(newState);
            DGLabConfig.save();
            button.setMessage(Component.literal(newState ? "HUD: 开启" : "HUD: 关闭"));
        }).bounds(startX, startY + spacing * 3, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.hudToggleButton);

        // HUD 位置切换按钮
        int currentPos = DGLabConfig.HUD_POSITION.get();
        String posText = "位置: " + getPositionText(currentPos);
        this.hudPositionButton = Button.builder(Component.literal(posText), button -> {
            int newPos = (DGLabConfig.HUD_POSITION.get() + 1) % 4;
            DGLabConfig.HUD_POSITION.set(newPos);
            DGLabConfig.save();
            button.setMessage(Component.literal("位置: " + getPositionText(newPos)));
        }).bounds(startX + btnWidth + btnGap, startY + spacing * 3, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.hudPositionButton);

        // 关闭界面按钮
        this.closeButton = Button.builder(Component.literal("关闭界面"), button -> this.onClose())
            .bounds(startX + (btnWidth + btnGap) * 2, startY + spacing * 3, btnWidth, buttonHeight).build();
        this.addRenderableWidget(this.closeButton);
    }

    /**
     * 获取 HUD 位置对应的中文文本
     */
    private String getPositionText(int pos) {
        switch (pos) {
            case 0: return "左上";
            case 1: return "右上";
            case 2: return "左下";
            case 3: return "右下";
            default: return "未知";
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // 1. 先让父类把毛玻璃背景和所有的按钮都画好
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        // 2. 然后在最顶层，画我们极其清晰的文字！
        int centerX = this.width / 2;

        // 标题
        guiGraphics.drawCenteredString(this.font, Component.literal("DGLab Craft"), centerX, 30, 0xFFFFFF);

        // 连接状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        Component statusText;
        int statusColor;
        if (isConnected) {
            statusText = Component.literal("已连接");
            statusColor = 0x00FF00;
        } else {
            statusText = Component.literal("等待连接...");
            statusColor = 0xFFFF00;
        }
        guiGraphics.drawCenteredString(this.font, statusText, centerX, 50, statusColor);

        // 服务器信息
        String serverInfo = server.resolveConnectionHost() + ":" + server.getPort();
        guiGraphics.drawCenteredString(this.font, Component.literal(serverInfo), centerX, this.height - 20, 0x888888);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        // 返回游戏
        this.minecraft.setScreen(null);
    }
}

package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
        this.settingsButton = new Button(
            centerX - buttonWidth / 2,
            startY,
            buttonWidth,
            buttonHeight,
            Component.literal("强度设置"),
            (button) -> {
                this.minecraft.setScreen(new DGLabCraftScreen(this));
            }
        );
        this.addRenderableWidget(this.settingsButton);

        // 连接设置按钮
        this.connectionButton = new Button(
            centerX - buttonWidth / 2,
            startY + spacing,
            buttonWidth,
            buttonHeight,
            Component.literal("连接设置"),
            (button) -> {
                this.minecraft.setScreen(new ConnectionScreen(this));
            }
        );
        this.addRenderableWidget(this.connectionButton);

        // 波形设置按钮 (敬请期待)
        this.waveformButton = new Button(
            centerX - buttonWidth / 2,
            startY + spacing * 2,
            buttonWidth,
            buttonHeight,
            Component.literal("波形设置 (敬请期待)"),
            (button) -> {
                // TODO: 波形设置界面
            }
        );
        this.addRenderableWidget(this.waveformButton);

        // HUD 开关按钮 + HUD 位置切换按钮 + 关闭界面按钮（三列并排，总宽度200与上方按钮对齐）
        int btnWidth = 66;  // 66*3 + 1*2 = 200
        int btnGap = 1;     // 间距
        int startX = centerX - buttonWidth / 2;
        boolean hudEnabled = ModConfig.HUD_ENABLED.get();
        String hudText = hudEnabled ? "HUD: 开启" : "HUD: 关闭";
        this.hudToggleButton = new Button(
            startX,
            startY + spacing * 3,
            btnWidth,
            buttonHeight,
            Component.literal(hudText),
            (button) -> {
                boolean newState = !ModConfig.HUD_ENABLED.get();
                ModConfig.HUD_ENABLED.set(newState);
                ModConfig.save();
                button.setMessage(Component.literal(newState ? "HUD: 开启" : "HUD: 关闭"));
            }
        );
        this.addRenderableWidget(this.hudToggleButton);

        // HUD 位置切换按钮
        int currentPos = ModConfig.HUD_POSITION.get();
        String posText = "位置: " + getPositionText(currentPos);
        this.hudPositionButton = new Button(
            startX + btnWidth + btnGap,
            startY + spacing * 3,
            btnWidth,
            buttonHeight,
            Component.literal(posText),
            (button) -> {
                int newPos = (ModConfig.HUD_POSITION.get() + 1) % 4;
                ModConfig.HUD_POSITION.set(newPos);
                ModConfig.save();
                button.setMessage(Component.literal("位置: " + getPositionText(newPos)));
            }
        );
        this.addRenderableWidget(this.hudPositionButton);

        // 关闭界面按钮
        this.closeButton = new Button(
            startX + (btnWidth + btnGap) * 2,
            startY + spacing * 3,
            btnWidth,
            buttonHeight,
            Component.literal("关闭界面"),
            (button) -> this.onClose()
        );
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        int centerX = this.width / 2;

        // 标题
        drawCenteredString(pPoseStack, this.font, "DGLab Craft", centerX, 30, 0xFFFFFF);

        // 连接状态
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        boolean isConnected = server.isConnected();

        String statusText;
        int statusColor;
        if (isConnected) {
            statusText = "已连接";
            statusColor = 0x00FF00;
        } else {
            statusText = "等待连接...";
            statusColor = 0xFFFF00;
        }
        drawCenteredString(pPoseStack, this.font, statusText, centerX, 50, statusColor);

        // 服务器信息
        String serverInfo = server.resolveConnectionHost() + ":" + server.getPort();
        drawCenteredString(pPoseStack, this.font, serverInfo, centerX, this.height - 20, 0x888888);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
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

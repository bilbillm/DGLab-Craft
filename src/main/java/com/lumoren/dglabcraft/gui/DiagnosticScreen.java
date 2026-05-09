package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * DGLab 诊断页面
 */
public class DiagnosticScreen extends Screen {
    private static final int BUTTON_WIDTH = 98;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int CONTENT_WIDTH = 320;

    private final Screen parent;
    private Button regenerateQrButton;
    private Button silenceButton;
    private Button doneButton;

    public DiagnosticScreen(Screen parent) {
        super(Component.literal("连接诊断"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int totalButtonWidth = BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int startX = centerX - totalButtonWidth / 2;
        int buttonY = this.height - 30;

        this.regenerateQrButton = Button.builder(Component.literal("重新生成二维码"), button -> {
            WebSocketServerManager.getInstance().generateQrUrl();
        }).bounds(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.regenerateQrButton);

        this.silenceButton = Button.builder(Component.literal("立即停止反馈"), button -> {
            WebSocketServerManager.getInstance().safeSilenceAll();
        }).bounds(startX + BUTTON_WIDTH + BUTTON_GAP, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.silenceButton);

        this.doneButton = Button.builder(Component.literal("完成"), button -> this.onClose())
            .bounds(startX + (BUTTON_WIDTH + BUTTON_GAP) * 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        int centerX = this.width / 2;
        int contentX = centerX - CONTENT_WIDTH / 2;
        int y = 20;

        guiGraphics.drawCenteredString(this.font, Component.literal("连接诊断"), centerX, y, 0xFFFFFF);
        y += 18;
        guiGraphics.drawCenteredString(this.font, Component.literal("这里会告诉你现在连没连上、两边通道在忙什么，以及下一步该做什么"), centerX, y, 0xAAAAAA);
        y += 26;

        y = drawSection(guiGraphics, contentX, y, "连接状态", buildConnectionLines(server));
        y = drawSection(guiGraphics, contentX, y, "A 通道状态", buildChannelLines(server, "A"));
        y = drawSection(guiGraphics, contentX, y, "B 通道状态", buildChannelLines(server, "B"));
        y = drawSection(guiGraphics, contentX, y, "当前效果概况", buildRuntimeLines(server));
        drawSection(guiGraphics, contentX, y, "详细信息", buildDetailLines(server));

        this.regenerateQrButton.active = server.isRunning() && !server.isConnected();
        this.silenceButton.active = server.isConnected() && server.hasLiveOutput();

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private List<Component> buildConnectionLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();

        if (server.isConnected()) {
            lines.add(Component.literal("手机已经连上，可以正常发反馈。"));
            lines.add(Component.literal("如果感觉没反应，可以先看下面的 A/B 通道状态。"));
        } else if (server.isWaitingForAppBind()) {
            lines.add(Component.literal("手机已经连过来，但还没完成确认。"));
            lines.add(Component.literal("请在 DGLab APP 里继续确认，或者重新扫一次二维码。"));
        } else if (server.isRunning()) {
            lines.add(Component.literal("还没连上手机。"));
            lines.add(Component.literal("请打开 DGLab APP 扫码连接，确认手机和电脑在同一局域网。"));
        } else {
            lines.add(Component.literal("本地连接服务还没启动。"));
            lines.add(Component.literal("先回到连接设置页检查端口，再重新打开本页面看看。"));
        }

        return lines;
    }

    private List<Component> buildChannelLines(WebSocketServerManager server, String channel) {
        List<Component> lines = new ArrayList<>();
        boolean active = server.isChannelRuntimeActive(channel);
        int intensity = server.getChannelRuntimeIntensity(channel);
        String detail = toPlainDetail(server.getChannelRuntimeSource(channel), server.getChannelRuntimeDetail(channel));

        if (!server.isConnected()) {
            lines.add(Component.literal("现在没有连上手机，这个通道不会发出反馈。"));
            return lines;
        }

        if (active) {
            lines.add(Component.literal(channel + " 通道正在工作。"));
            lines.add(Component.literal("大概强度: " + intensity + "%；原因: " + detail + "。"));
            lines.add(Component.literal("如果这不是你想要的反馈，可以点下面的“立即停止反馈”。"));
        } else {
            lines.add(Component.literal(channel + " 通道当前空闲。"));
            lines.add(Component.literal("等游戏里再次触发事件时，这里会显示新的状态。"));
        }

        return lines;
    }

    private List<Component> buildRuntimeLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();

        if (!server.isConnected()) {
            lines.add(Component.literal("现在没有可用的运行中效果。"));
            lines.add(Component.literal("连上手机后，这里会显示当前是哪类事件在驱动反馈。"));
            return lines;
        }

        if (server.isSyncRuntimeActive()) {
            lines.add(Component.literal("A/B 两边正在一起工作。"));
            lines.add(Component.literal("当前来源: " + toPlainDetail(server.getSyncRuntimeSource(), server.getSyncRuntimeDetail()) + "。"));
            lines.add(Component.literal("预计还会持续约 " + formatSeconds(server.getSyncRuntimeRemainingMillis()) + "。"));
            return lines;
        }

        if (server.hasActiveEffects()) {
            lines.add(Component.literal("现在至少有一个通道正在处理反馈。"));
            lines.add(Component.literal("如果你想确认是哪一边在工作，请看上面的 A/B 状态。"));
        } else {
            lines.add(Component.literal("最近没有正在持续的反馈。"));
            lines.add(Component.literal("这不代表功能坏了，只是此刻没有事件在触发。"));
            lines.add(Component.literal("如果设备还在继续震，点下面的“立即停止反馈”会更稳妥。"));
        }

        return lines;
    }

    private List<Component> buildDetailLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("地址: " + server.resolveConnectionHost() + ":" + server.getPort()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("连接服务: " + (server.isRunning() ? "已启动" : "未启动")).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("底层连接: " + (server.hasClientSocketConnection() ? "已建立" : "未建立")).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("绑定状态: " + (server.isConnected() ? "已完成" : (server.isWaitingForAppBind() ? "等待确认" : "未完成"))).withStyle(ChatFormatting.GRAY));

        if (server.getConnectedClientId() != null) {
            lines.add(Component.literal("设备 clientId: " + server.getConnectedClientId()).withStyle(ChatFormatting.GRAY));
        }
        if (server.getTargetId() != null) {
            lines.add(Component.literal("targetId: " + server.getTargetId()).withStyle(ChatFormatting.GRAY));
        }

        lines.add(Component.literal("A: status=" + server.getChannelAStatus() + ", intensity=" + (int) server.getChannelAIntensity() + "%"
            + formatWaveformDetail(server, "A")).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("B: status=" + server.getChannelBStatus() + ", intensity=" + (int) server.getChannelBIntensity() + "%"
            + formatWaveformDetail(server, "B")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private String formatWaveformDetail(WebSocketServerManager server, String channel) {
        String waveform = server.getChannelRuntimeWaveform(channel);
        long remainingMillis = server.getChannelRuntimeRemainingMillis(channel);
        if (waveform == null || waveform.isBlank()) {
            return "";
        }
        return ", waveform=" + waveform + ", remaining=" + formatSeconds(remainingMillis);
    }

    private int drawSection(GuiGraphics guiGraphics, int x, int y, String title, List<Component> lines) {
        guiGraphics.drawString(this.font, title, x, y, 0xFFFFFF, false);
        y += 12;

        for (Component line : lines) {
            List<FormattedCharSequence> wrapped = this.font.split(line, CONTENT_WIDTH);
            for (FormattedCharSequence textLine : wrapped) {
                guiGraphics.drawString(this.font, textLine, x, y, 0xD0D0D0, false);
                y += 10;
            }
        }

        return y + 8;
    }

    private String toPlainDetail(WebSocketServerManager.EffectSource source, String detail) {
        return switch (source) {
            case DAMAGE -> switch (normalize(detail)) {
                case "lava" -> "岩浆伤害";
                case "onfire" -> "着火";
                case "infire" -> "站在火里";
                case "hotfloor" -> "烫脚";
                case "drown" -> "溺水";
                case "freeze" -> "冰冻";
                case "fall" -> "摔落";
                case "mob_attack" -> "被生物打到";
                case "player_attack" -> "被玩家打到";
                case "explosion", "explosion.player" -> "爆炸";
                default -> "受伤反馈";
            };
            case HEARTBEAT -> "低血量心跳提醒";
            case ENVIRONMENT -> switch (normalize(detail)) {
                case "nether" -> "下界环境反馈";
                case "end" -> "末地环境反馈";
                case "portal" -> "传送门环境反馈";
                case "powder_snow" -> "细雪环境反馈";
                default -> "环境反馈";
            };
            case NONE -> "暂无";
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String formatSeconds(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        return seconds + " 秒";
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}

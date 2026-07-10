package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.IssueReportBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends GuiScreen {
    private static final int BTN_COPY = 1;
    private static final int BTN_SILENCE = 2;
    private static final int BTN_DONE = 3;
    private final GuiScreen parent;
    private int scroll;

    public DiagnosticScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(BTN_COPY, centerX - 155, height - 30, 100, 20, "复制反馈"));
        buttonList.add(new GuiButton(BTN_SILENCE, centerX - 50, height - 30, 100, 20, "停止输出"));
        buttonList.add(new GuiButton(BTN_DONE, centerX + 55, height - 30, 100, 20, "完成"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BTN_COPY) {
            GuiScreen.setClipboardString(buildIssueReport());
        } else if (button.id == BTN_SILENCE) {
            WebSocketServerManager.getInstance().safeSilenceAll();
        } else if (button.id == BTN_DONE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? -12 : 12;
            scroll = Math.max(0, scroll);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "连接诊断", width / 2, 18, 0xFFFFFF);
        List<String> lines = buildLines();
        int y = 40 - scroll;
        for (String line : lines) {
            if (y > 28 && y < height - 38) {
                fontRenderer.drawString(line, 24, y, 0xDDDDDD);
            }
            y += 12;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private List<String> buildLines() {
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        List<String> lines = new ArrayList<String>();
        lines.add("Minecraft: 1.12.2");
        lines.add("Forge: 14.23.5.2847");
        lines.add("Java: " + System.getProperty("java.version"));
        lines.add("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        lines.add("WebSocket: " + server.resolveConnectionHost() + ":" + server.getPort());
        lines.add("服务状态: " + (server.isRunning() ? "已启动" : "未启动"));
        lines.add("Socket: " + (server.hasClientSocketConnection() ? "已连接" : "未连接"));
        lines.add("绑定: " + (server.isConnected() ? "已完成" : "未完成"));
        lines.add("ClientId: " + IssueReportBuilder.redactId(server.getConnectedClientId()));
        lines.add("TargetId: " + IssueReportBuilder.redactId(server.getTargetId()));
        lines.add("A: " + (int) server.getChannelAIntensity() + "% " + server.getChannelAStatus());
        lines.add("B: " + (int) server.getChannelBIntensity() + "% " + server.getChannelBStatus());
        lines.add("HUD: " + ModConfig.HUD_ENABLED.get() + ", Waveform: " + ModConfig.WAVEFORM_OVERLAY_ENABLED.get());
        lines.add("Sync: " + ModConfig.SYNC_CHANNELS.get());
        return lines;
    }

    private String buildIssueReport() {
        StringBuilder builder = new StringBuilder();
        for (String line : buildLines()) {
            builder.append("- ").append(line).append('\n');
        }
        return builder.toString();
    }
}

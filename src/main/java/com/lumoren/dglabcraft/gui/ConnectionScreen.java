package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ConnectionScreen extends GuiScreen {
    private static final int BTN_REFRESH = 1;
    private static final int BTN_OPEN_QR = 2;
    private static final int BTN_RESET_IP = 3;
    private static final int BTN_DONE = 4;
    private final GuiScreen parent;
    private GuiTextField hostField;

    public ConnectionScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        String currentHost = ModConfig.WS_HOST.get();
        hostField = new GuiTextField(10, fontRenderer, centerX - 100, 92, 200, 20);
        hostField.setMaxStringLength(128);
        hostField.setText(currentHost == null ? "" : currentHost);
        buttonList.add(new GuiButton(BTN_REFRESH, centerX - 100, 120, 200, 20, "刷新二维码"));
        buttonList.add(new GuiButton(BTN_OPEN_QR, centerX - 100, 145, 200, 20, "打开二维码图片"));
        buttonList.add(new GuiButton(BTN_RESET_IP, centerX - 100, 170, 200, 20, "自动选择 IP"));
        buttonList.add(new GuiButton(BTN_DONE, centerX - 100, height - 30, 200, 20, "完成"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BTN_REFRESH) {
            saveHost();
            WebSocketServerManager.getInstance().generateQrUrl();
        } else if (button.id == BTN_OPEN_QR) {
            File qr = QRCodeGenerator.getQrCodeFile();
            if (qr != null && qr.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(qr);
            }
        } else if (button.id == BTN_RESET_IP) {
            hostField.setText("localhost");
            saveHost();
        } else if (button.id == BTN_DONE) {
            saveHost();
            mc.displayGuiScreen(parent);
        }
    }

    private void saveHost() {
        String value = hostField.getText() == null ? "" : hostField.getText().trim();
        ModConfig.WS_HOST.set(value.isEmpty() ? "localhost" : value);
        ModConfig.save();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (hostField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        hostField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        hostField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        drawCenteredString(fontRenderer, "连接设置", width / 2, 30, 0xFFFFFF);
        drawCenteredString(fontRenderer, server.isConnected() ? "已连接" : "正在等待连接", width / 2, 52, server.isConnected() ? 0x55FF88 : 0xFFFF55);
        drawCenteredString(fontRenderer, "地址: " + server.resolveConnectionHost() + ":" + server.getPort(), width / 2, 70, 0xCCCCCC);
        fontRenderer.drawString("手动局域网 IP:", width / 2 - 100, 82, 0xAAAAAA);
        hostField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

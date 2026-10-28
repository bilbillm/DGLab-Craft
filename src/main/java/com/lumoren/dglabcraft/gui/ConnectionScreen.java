package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.RelayConnection;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.QRCodeGenerator;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** First-use pairing; detailed connection tools stay in the options screen. */
public class ConnectionScreen extends Screen {
    private final Screen parent;
    private ResourceLocation texture;
    private String displayedCode = "";
    private Button retry;
    private QuickConnectLayout.Layout layout;

    public ConnectionScreen(Screen parent) {
        super(Component.translatable("connect.dglabcraft.title")); this.parent = parent;
    }
    @Override protected void init() {
        layout = QuickConnectLayout.calculate(width, height);
        WebSocketServerManager.getInstance().start();
        retry = addRenderableWidget(Button.builder(Component.translatable("connect.dglabcraft.retry"), b -> {
            WebSocketServerManager server = WebSocketServerManager.getInstance();
            com.lumoren.dglabcraft.config.ModConfig.WS_ENABLED.set(true);
            com.lumoren.dglabcraft.config.ModConfig.save();
            server.stop(); server.start(); releaseCode();
        }).bounds(width / 2 - 60, layout.qrY() + layout.qrSize() / 2 - 10, 120, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("connect.dglabcraft.options"),
            b -> minecraft.setScreen(new ConnectionOptionsScreen(this)))
            .bounds(width / 2 - 132, layout.buttonY(), 128, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.dglabcraft.done"), b -> onClose())
            .bounds(width / 2 + 4, layout.buttonY(), 128, 20).build());
        tick();
    }
    @Override public void tick() {
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        RelayConnection.State state = server.connectionState();
        retry.visible = state == RelayConnection.State.RETRYING || state == RelayConnection.State.IDLE;
        String current = server.pairingUrl();
        if (current.equals(displayedCode)) return;
        releaseCode();
        if (current.isEmpty()) return;
        try {
            var matrix = QRCodeGenerator.createMatrix(current);
            NativeImage image = new NativeImage(matrix.getWidth(), matrix.getHeight(), true);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    image.setPixelRGBA(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            DynamicTexture dynamic = new DynamicTexture(image); dynamic.setFilter(false, false);
            texture = minecraft.getTextureManager().register("dglabcraft_pairing", dynamic);
            displayedCode = current;
        } catch (com.google.zxing.WriterException | RuntimeException error) {
            displayedCode = ""; retry.visible = true;
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        RelayConnection.State state = server.connectionState();
        boolean connected = state == RelayConnection.State.CONNECTED;
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("connect.dglabcraft.state." + state.name().toLowerCase(java.util.Locale.ROOT)),
            width / 2, 30, connected ? 0x67E8A6 : 0x86D9FF);
        int x = (width - layout.qrSize()) / 2, y = layout.qrY(), size = layout.qrSize();
        boolean validCode = texture != null && displayedCode.equals(server.pairingUrl());
        if (!connected && validCode) {
            graphics.fill(x - 5, y - 5, x + size + 5, y + size + 5, 0xFFFFFFFF);
            graphics.blit(texture, x, y, size, size, 0, 0, 400, 400, 400, 400);
        } else if (connected) {
            graphics.fill(x, y, x + size, y + size, 0xA51B4332);
            graphics.drawCenteredString(font, Component.translatable("connect.dglabcraft.ready"), width / 2, y + size / 2 - 12, 0xA7F3D0);
            graphics.drawCenteredString(font, Component.translatable("connect.dglabcraft.play"), width / 2, y + size / 2 + 6, 0xFFFFFF);
        } else if (!retry.visible) {
            graphics.drawCenteredString(font, Component.translatable("connect.dglabcraft.preparing"), width / 2, y + size / 2, 0xAAAAAA);
        }
        Component hint = Component.translatable(connected ? "connect.dglabcraft.device_hint"
            : server.usesRelay() ? "connect.dglabcraft.scan_hint" : "connect.dglabcraft.lan_hint");
        int lineY = y + size + 10;
        for (var line : font.split(hint, Math.min(360, width - 24))) {
            if (lineY + 9 > layout.buttonY() - 3) break;
            graphics.drawCenteredString(font, line, width / 2, lineY, 0xC7CDD6); lineY += 10;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    private void releaseCode() {
        if (texture != null && minecraft != null) minecraft.getTextureManager().release(texture);
        texture = null; displayedCode = "";
    }
    @Override public void removed() { releaseCode(); super.removed(); }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return true; }
}

package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class ConnectionOptionsScreen extends Screen {
    private final Screen parent;
    ConnectionOptionsScreen(Screen parent) { super(Component.translatable("connect.dglabcraft.options")); this.parent = parent; }
    @Override protected void init() {
        int x = width / 2 - 110, y = Math.max(70, height / 2 - 28);
        addRenderableWidget(Button.builder(Component.translatable("connect.dglabcraft.relay"), b -> select(true)).bounds(x, y, 220, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("connect.dglabcraft.lan"), b -> select(false)).bounds(x, y + 25, 220, 20).build());
        Button lanTools = addRenderableWidget(Button.builder(Component.translatable("connect.dglabcraft.lan_tools"),
            b -> minecraft.setScreen(new AdvancedConnectionScreen(this))).bounds(x, y + 50, 108, 20).build());
        lanTools.active = !WebSocketServerManager.getInstance().usesRelay();
        addRenderableWidget(Button.builder(Component.translatable("button.dglabcraft.diagnostics"),
            b -> minecraft.setScreen(new DiagnosticScreen(this))).bounds(x + 112, y + 50, 108, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.dglabcraft.done"), b -> onClose()).bounds(x, height - 28, 220, 20).build());
    }
    private void select(boolean relay) {
        WebSocketServerManager.getInstance().useRelay(relay);
        minecraft.setScreen(parent);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        int y = 36;
        for (var line : font.split(Component.translatable("connect.dglabcraft.route_hint"), Math.min(360, width - 24))) {
            graphics.drawCenteredString(font, line, width / 2, y, 0xAAAAAA); y += 10;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}

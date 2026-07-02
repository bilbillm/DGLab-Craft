package com.lumoren.dglabcraft.gui;

import net.minecraft.client.gui.GuiGraphics;

final class DGLabScreenBackground {
    private static final int BASE = 0xFF17181D;
    private static final int TOP_BAND = 0xFF101116;
    private static final int BOTTOM_BAND = 0xFF101116;
    private static final int CENTER_PANEL = 0xFF1E2028;

    private DGLabScreenBackground() {
    }

    static void render(GuiGraphics guiGraphics, int width, int height) {
        guiGraphics.fill(0, 0, width, height, BASE);
        guiGraphics.fill(0, 0, width, 64, TOP_BAND);
        guiGraphics.fill(0, Math.max(0, height - 48), width, height, BOTTOM_BAND);

        int panelLeft = Math.max(8, width / 2 - 230);
        int panelRight = Math.min(width - 8, width / 2 + 230);
        if (panelRight > panelLeft) {
            guiGraphics.fill(panelLeft, 64, panelRight, Math.max(64, height - 48), CENTER_PANEL);
        }
    }

    static int listHeaderColor() {
        return 0xFF272A34;
    }
}

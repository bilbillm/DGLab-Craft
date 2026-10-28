package com.lumoren.dglabcraft.gui;

final class QuickConnectLayout {
    record Layout(int qrY, int qrSize, int buttonY) { }
    static Layout calculate(int width, int height) {
        return new Layout(53, Math.max(48, Math.min(176, Math.min(width - 36, height - 116))), height - 28);
    }
}

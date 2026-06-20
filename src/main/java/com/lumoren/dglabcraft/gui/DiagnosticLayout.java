package com.lumoren.dglabcraft.gui;

final class DiagnosticLayout {
    static final int BUTTON_AREA_HEIGHT = 62;
    static final int SCROLL_STEP = 24;

    private DiagnosticLayout() {
    }

    static int viewportBottom(int screenHeight) {
        return Math.max(0, screenHeight - BUTTON_AREA_HEIGHT);
    }

    static int maxScroll(int contentHeight, int viewportTop, int viewportBottom) {
        return Math.max(0, contentHeight - Math.max(0, viewportBottom - viewportTop));
    }

    static int clampScroll(int scrollOffset, int maxScroll) {
        if (scrollOffset < 0) {
            return 0;
        }
        return Math.min(scrollOffset, maxScroll);
    }

    static int scrollByWheel(int scrollOffset, double delta, int maxScroll) {
        return clampScroll(scrollOffset - (int) Math.round(delta * SCROLL_STEP), maxScroll);
    }

    static boolean isLineFullyVisible(int lineY, int lineHeight, int viewportTop, int viewportBottom) {
        return lineY >= viewportTop && lineY + lineHeight <= viewportBottom;
    }
}

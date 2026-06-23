package com.lumoren.dglabcraft.gui;

public final class OverlayLayout {
    public static final int HUD_WIDTH = 154;
    public static final int HUD_HEIGHT = 54;
    public static final int WAVEFORM_WIDTH = 192;
    public static final int WAVEFORM_HEIGHT = 86;
    private static final int MARGIN = 5;

    private OverlayLayout() {
    }

    public static Rect rectFromRatio(double xRatio, double yRatio, int width, int height, int screenWidth, int screenHeight) {
        double safeX = sanitizeRatio(xRatio);
        double safeY = sanitizeRatio(yRatio);
        int x = (int) Math.round(safeX * Math.max(0, screenWidth - width));
        int y = (int) Math.round(safeY * Math.max(0, screenHeight - height));
        return clampRect(new Rect(x, y, width, height), screenWidth, screenHeight);
    }

    public static Rect defaultHudRect(int hudPosition, int screenWidth, int screenHeight) {
        return rectFromRatio(defaultXRatio(hudPosition), defaultYRatio(hudPosition),
            HUD_WIDTH, HUD_HEIGHT, screenWidth, screenHeight);
    }

    public static Rect defaultWaveformRect(int screenWidth, int screenHeight) {
        return rectFromRatio(0.5D, 0.12D, WAVEFORM_WIDTH, WAVEFORM_HEIGHT, screenWidth, screenHeight);
    }

    public static double ratioFromPixel(int pixel, int elementSize, int screenSize) {
        int available = Math.max(0, screenSize - elementSize);
        if (available == 0) {
            return 0.0D;
        }
        return clampRatio((double) pixel / (double) available);
    }

    public static Rect drag(Rect start, double mouseX, double mouseY, double grabOffsetX, double grabOffsetY,
                            int screenWidth, int screenHeight) {
        int x = (int) Math.round(mouseX - grabOffsetX);
        int y = (int) Math.round(mouseY - grabOffsetY);
        return clampRect(new Rect(x, y, start.width(), start.height()), screenWidth, screenHeight);
    }

    public static Rect clampRect(Rect rect, int screenWidth, int screenHeight) {
        int maxX = Math.max(MARGIN, screenWidth - rect.width() - MARGIN);
        int maxY = Math.max(MARGIN, screenHeight - rect.height() - MARGIN);
        int x = clamp(rect.x(), MARGIN, maxX);
        int y = clamp(rect.y(), MARGIN, maxY);
        return new Rect(x, y, rect.width(), rect.height());
    }

    public static double clampRatio(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    public static boolean hasSavedRatio(double xRatio, double yRatio) {
        return xRatio >= 0.0D && yRatio >= 0.0D;
    }

    private static double defaultXRatio(int hudPosition) {
        return switch (hudPosition) {
            case 1, 3 -> 1.0D;
            default -> 0.0D;
        };
    }

    private static double defaultYRatio(int hudPosition) {
        return switch (hudPosition) {
            case 2, 3 -> 1.0D;
            default -> 0.0D;
        };
    }

    private static double sanitizeRatio(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0D;
        }
        return clampRatio(value);
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}

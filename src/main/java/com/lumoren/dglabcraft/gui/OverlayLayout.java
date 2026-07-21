package com.lumoren.dglabcraft.gui;

public final class OverlayLayout {
    public static final int HUD_WIDTH = 88;
    public static final int HUD_HEIGHT = 64;
    public static final int WAVEFORM_WIDTH = 192;
    public static final int WAVEFORM_HEIGHT = 86;
    private static final int MARGIN = 5;
    private static final int DEFAULT_OVERLAY_GAP = 3;
    private static final double MIN_SCALE = 0.375D;
    private static final double MAX_SCALE = 4.0D;

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
        return clampRect(new Rect(MARGIN + HUD_WIDTH + DEFAULT_OVERLAY_GAP, MARGIN, WAVEFORM_WIDTH, WAVEFORM_HEIGHT),
            screenWidth, screenHeight);
    }

    public static Rect scaledRectFromRatio(double xRatio, double yRatio, int baseWidth, int baseHeight,
                                           double scale, int screenWidth, int screenHeight) {
        double safeScale = clampScale(scale, baseWidth, baseHeight, screenWidth, screenHeight);
        return rectFromRatio(xRatio, yRatio, scaledSize(baseWidth, safeScale), scaledSize(baseHeight, safeScale),
            screenWidth, screenHeight);
    }

    public static Rect resize(Rect start, double mouseX, double mouseY, ResizeHandle handle,
                              int baseWidth, int baseHeight, int screenWidth, int screenHeight) {
        if (handle == null || handle == ResizeHandle.NONE) {
            return start;
        }

        double rawScale = scaleForResize(start, mouseX, mouseY, handle, baseWidth, baseHeight);
        double scale = clampScale(rawScale, baseWidth, baseHeight, screenWidth, screenHeight);
        int width = scaledSize(baseWidth, scale);
        int height = scaledSize(baseHeight, scale);
        int x = handle.left() ? start.x() + start.width() - width : start.x();
        int y = handle.top() ? start.y() + start.height() - height : start.y();
        return clampRect(new Rect(x, y, width, height), screenWidth, screenHeight);
    }

    public static ResizeHandle resizeHandle(Rect rect, double mouseX, double mouseY, int borderSize) {
        if (!rect.contains(mouseX, mouseY)) {
            return ResizeHandle.NONE;
        }

        boolean left = mouseX <= rect.x() + borderSize;
        boolean right = mouseX >= rect.x() + rect.width() - borderSize;
        boolean top = mouseY <= rect.y() + borderSize;
        boolean bottom = mouseY >= rect.y() + rect.height() - borderSize;
        if (!left && !right && !top && !bottom) {
            return ResizeHandle.NONE;
        }
        return new ResizeHandle(left, right, top, bottom);
    }

    public static double scaleFromRect(Rect rect, int baseWidth) {
        if (baseWidth <= 0) {
            return 1.0D;
        }
        return Math.max(MIN_SCALE, rect.width() / (double) baseWidth);
    }

    public static double clampScale(double scale, int baseWidth, int baseHeight, int screenWidth, int screenHeight) {
        double safe = sanitizeFinite(scale, 1.0D);
        double maxByWidth = (Math.max(1, screenWidth - MARGIN * 2)) / (double) baseWidth;
        double maxByHeight = (Math.max(1, screenHeight - MARGIN * 2)) / (double) baseHeight;
        double max = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(maxByWidth, maxByHeight)));
        return Math.max(MIN_SCALE, Math.min(max, safe));
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
        return hudPosition == 1 || hudPosition == 3 ? 1.0D : 0.0D;
    }

    private static double defaultYRatio(int hudPosition) {
        return hudPosition == 2 || hudPosition == 3 ? 1.0D : 0.0D;
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

    private static int scaledSize(int baseSize, double scale) {
        return Math.max(1, (int) Math.round(baseSize * scale));
    }

    private static double scaleForResize(Rect start, double mouseX, double mouseY, ResizeHandle handle,
                                         int baseWidth, int baseHeight) {
        double horizontalScale = start.width() / (double) baseWidth;
        double verticalScale = start.height() / (double) baseHeight;

        if (handle.left()) {
            horizontalScale = (start.x() + start.width() - mouseX) / baseWidth;
        } else if (handle.right()) {
            horizontalScale = (mouseX - start.x()) / baseWidth;
        }

        if (handle.top()) {
            verticalScale = (start.y() + start.height() - mouseY) / baseHeight;
        } else if (handle.bottom()) {
            verticalScale = (mouseY - start.y()) / baseHeight;
        }

        if ((handle.left() || handle.right()) && (handle.top() || handle.bottom())) {
            return Math.max(horizontalScale, verticalScale);
        }
        if (handle.left() || handle.right()) {
            return horizontalScale;
        }
        return verticalScale;
    }

    private static double sanitizeFinite(double value, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return value;
    }

    public static final class Rect {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int x() { return x; }
        public int y() { return y; }
        public int width() { return width; }
        public int height() { return height; }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Rect)) return false;
            Rect rect = (Rect) other;
            return x == rect.x && y == rect.y && width == rect.width && height == rect.height;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }

        @Override
        public String toString() {
            return "Rect[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
        }

        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public static final class ResizeHandle {
        public static final ResizeHandle NONE = new ResizeHandle(false, false, false, false);
        private final boolean left;
        private final boolean right;
        private final boolean top;
        private final boolean bottom;

        public ResizeHandle(boolean left, boolean right, boolean top, boolean bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public boolean left() { return left; }
        public boolean right() { return right; }
        public boolean top() { return top; }
        public boolean bottom() { return bottom; }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ResizeHandle)) return false;
            ResizeHandle handle = (ResizeHandle) other;
            return left == handle.left && right == handle.right && top == handle.top && bottom == handle.bottom;
        }

        @Override
        public int hashCode() {
            int result = left ? 1 : 0;
            result = 31 * result + (right ? 1 : 0);
            result = 31 * result + (top ? 1 : 0);
            result = 31 * result + (bottom ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ResizeHandle[left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + "]";
        }
    }
}

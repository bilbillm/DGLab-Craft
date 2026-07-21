package com.lumoren.dglabcraft.gui;

import java.util.Arrays;
import java.util.List;

final class ConnectionLayout {
    static final int BUTTON_HEIGHT = 20;
    static final int HEADER_TITLE_Y = 16;
    static final int HEADER_STATUS_Y = 32;
    static final int HEADER_ADDRESS_Y = 48;

    private static final int PAGE_MARGIN = 12;
    private static final int CONTENT_TOP = 64;
    private static final int COLUMN_GAP = 14;
    private static final int CONTROL_GAP = 4;
    private static final int QR_PADDING = 6;
    private static final int MIN_QR_SIZE = 64;
    private static final int MAX_QR_SIZE = 176;
    private static final int HELP_GAP = 8;
    private static final int DONE_BOTTOM_MARGIN = 30;
    private static final int DONE_GAP = 8;

    private ConnectionLayout() {
    }

    static Layout calculate(int screenWidth, int screenHeight) {
        int width = Math.max(1, screenWidth);
        int height = Math.max(1, screenHeight);
        int pageMargin = Math.min(PAGE_MARGIN, Math.max(0, (width - 1) / 2));
        int availableWidth = Math.max(1, width - pageMargin * 2);

        int doneWidth = Math.min(200, Math.max(80, availableWidth));
        int doneY = Math.max(0, height - DONE_BOTTOM_MARGIN);
        Rect doneButton = new Rect((width - doneWidth) / 2, doneY, doneWidth, BUTTON_HEIGHT);
        int helpBottom = Math.max(CONTENT_TOP, doneY - DONE_GAP);
        int availableHeight = Math.max(0, helpBottom - CONTENT_TOP);
        boolean compactControls = availableHeight < 210;

        int minQrOuter = MIN_QR_SIZE + QR_PADDING * 2;
        int targetControlsWidth = clamp(width / 2 - 20, compactControls ? 140 : 160, 220);
        int controlsWidth = Math.min(targetControlsWidth,
            Math.max(80, availableWidth - COLUMN_GAP - minQrOuter));
        int horizontalQrSize = Math.max(1,
            availableWidth - COLUMN_GAP - controlsWidth - QR_PADDING * 2);

        int controlsHeight = compactControls ? 79 : 105;
        int targetHelpHeight = clamp(height - 210, 80, 140);
        int maxMainHeight = Math.max(0, availableHeight - HELP_GAP - targetHelpHeight);
        int verticalQrSize = compactControls
            ? MIN_QR_SIZE
            : Math.max(MIN_QR_SIZE, maxMainHeight - QR_PADDING * 2);
        int qrSize = Math.max(1, Math.min(MAX_QR_SIZE, Math.min(horizontalQrSize, verticalQrSize)));
        int qrPanelSize = qrSize + QR_PADDING * 2;
        int mainHeight = Math.max(controlsHeight, qrPanelSize);

        int groupWidth = qrPanelSize + COLUMN_GAP + controlsWidth;
        int groupX = Math.max(0, (width - groupWidth) / 2);
        int controlsX = groupX + qrPanelSize + COLUMN_GAP;
        int qrPanelY = CONTENT_TOP + Math.max(0, (mainHeight - qrPanelSize) / 2);
        Rect qrPanel = new Rect(groupX, qrPanelY, qrPanelSize, qrPanelSize);
        Rect qrImage = new Rect(groupX + QR_PADDING, qrPanelY + QR_PADDING, qrSize, qrSize);

        ControlLayout controlLayout = calculateControls(controlsX, controlsWidth, compactControls);
        int helpTop = CONTENT_TOP + mainHeight + HELP_GAP;
        Rect helpViewport = new Rect(pageMargin, helpTop, availableWidth,
            Math.max(0, helpBottom - helpTop));

        return new Layout(
            qrPanel,
            qrImage,
            controlLayout.controls(),
            controlLayout.manualInput(),
            controlLayout.manualLabelY(),
            controlLayout.buttons(),
            helpViewport,
            doneButton,
            compactControls
        );
    }

    private static ControlLayout calculateControls(int x, int width, boolean compact) {
        if (compact) {
            int inputY = CONTENT_TOP + 11;
            Rect manualInput = new Rect(x, inputY, width, BUTTON_HEIGHT);
            int buttonsY = manualInput.bottom() + CONTROL_GAP;
            int halfWidth = Math.max(1, (width - CONTROL_GAP) / 2);
            List<Rect> buttons = Arrays.asList(
                new Rect(x, buttonsY, halfWidth, BUTTON_HEIGHT),
                new Rect(x + halfWidth + CONTROL_GAP, buttonsY,
                    Math.max(1, width - halfWidth - CONTROL_GAP), BUTTON_HEIGHT),
                new Rect(x, buttonsY + BUTTON_HEIGHT + CONTROL_GAP, width, BUTTON_HEIGHT)
            );
            Rect controls = new Rect(x, CONTENT_TOP, width, buttons.get(2).bottom() - CONTENT_TOP);
            return new ControlLayout(controls, manualInput, CONTENT_TOP, buttons);
        }

        List<Rect> buttons = Arrays.asList(
            new Rect(x, CONTENT_TOP, width, BUTTON_HEIGHT),
            new Rect(x, CONTENT_TOP + BUTTON_HEIGHT + CONTROL_GAP, width, BUTTON_HEIGHT),
            new Rect(x, CONTENT_TOP + (BUTTON_HEIGHT + CONTROL_GAP) * 2, width, BUTTON_HEIGHT)
        );
        int manualLabelY = buttons.get(2).bottom() + 6;
        Rect manualInput = new Rect(x, manualLabelY + 11, width, BUTTON_HEIGHT);
        Rect controls = new Rect(x, CONTENT_TOP, width, manualInput.bottom() - CONTENT_TOP);
        return new ControlLayout(controls, manualInput, manualLabelY, buttons);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class Layout {
        private final Rect qrPanel;
        private final Rect qrImage;
        private final Rect controls;
        private final Rect manualInput;
        private final int manualLabelY;
        private final List<Rect> controlButtons;
        private final Rect helpViewport;
        private final Rect doneButton;
        private final boolean compactControls;

        Layout(Rect qrPanel, Rect qrImage, Rect controls, Rect manualInput, int manualLabelY,
               List<Rect> controlButtons, Rect helpViewport, Rect doneButton, boolean compactControls) {
            this.qrPanel = qrPanel;
            this.qrImage = qrImage;
            this.controls = controls;
            this.manualInput = manualInput;
            this.manualLabelY = manualLabelY;
            this.controlButtons = controlButtons;
            this.helpViewport = helpViewport;
            this.doneButton = doneButton;
            this.compactControls = compactControls;
        }

        Rect qrPanel() { return qrPanel; }
        Rect qrImage() { return qrImage; }
        Rect controls() { return controls; }
        Rect manualInput() { return manualInput; }
        int manualLabelY() { return manualLabelY; }
        List<Rect> controlButtons() { return controlButtons; }
        Rect helpViewport() { return helpViewport; }
        Rect doneButton() { return doneButton; }
        boolean compactControls() { return compactControls; }
    }

    static final class Rect {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        int x() { return x; }
        int y() { return y; }
        int width() { return width; }
        int height() { return height; }

        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }

        boolean intersects(Rect other) {
            return x < other.right() && right() > other.x
                && y < other.bottom() && bottom() > other.y;
        }

        boolean contains(Rect other) {
            return other.x >= x && other.y >= y
                && other.right() <= right() && other.bottom() <= bottom();
        }

        boolean contains(double pointX, double pointY) {
            return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
        }
    }

    private static final class ControlLayout {
        private final Rect controls;
        private final Rect manualInput;
        private final int manualLabelY;
        private final List<Rect> buttons;

        private ControlLayout(Rect controls, Rect manualInput, int manualLabelY, List<Rect> buttons) {
            this.controls = controls;
            this.manualInput = manualInput;
            this.manualLabelY = manualLabelY;
            this.buttons = buttons;
        }

        Rect controls() { return controls; }
        Rect manualInput() { return manualInput; }
        int manualLabelY() { return manualLabelY; }
        List<Rect> buttons() { return buttons; }
    }
}

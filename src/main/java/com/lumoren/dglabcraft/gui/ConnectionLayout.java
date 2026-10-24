package com.lumoren.dglabcraft.gui;

import java.util.List;

/**
 * Shared layout for the connection screen.
 * <p>
 * The QR panel sits on the left and the manual-IP controls sit on the right, so
 * the generated QR code can be rendered at the largest size that still fits the
 * current GUI dimensions. The layout also has a compact variant for small GUI
 * scales where the original vertical arrangement would overlap the Done button.
 */
final class ConnectionLayout {
    static final int BUTTON_HEIGHT = 20;
    static final int HEADER_TITLE_Y = 16;
    static final int HEADER_STATUS_Y = 32;
    static final int HEADER_ADDRESS_Y = 48;
    static final int CONTENT_TOP = 64;

    private static final int PAGE_MARGIN = 12;
    private static final int COLUMN_GAP = 14;
    private static final int CONTROL_GAP = 4;
    private static final int QR_PADDING = 6;
    private static final int MIN_QR_SIZE = 64;
    private static final int MAX_QR_SIZE = 176;
    private static final int DONE_BOTTOM_MARGIN = 30;
    private static final int DONE_GAP = 8;
    private static final int MANUAL_LABEL_GAP = 6;
    private static final int MANUAL_LABEL_HEIGHT = 11;

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

        int contentBottom = Math.max(CONTENT_TOP, doneY - DONE_GAP);
        int availableHeight = Math.max(0, contentBottom - CONTENT_TOP);

        boolean compact = availableHeight < getControlsHeight();
        boolean tiny = availableHeight < getTinyControlsHeight();
        int controlGap = compact ? 0 : CONTROL_GAP;
        int manualGap = compact ? 0 : MANUAL_LABEL_GAP;
        int manualHeight = tiny ? 0 : MANUAL_LABEL_HEIGHT;
        int controlsHeight = BUTTON_HEIGHT * 3 + controlGap * 2 + manualGap + manualHeight + BUTTON_HEIGHT;

        int minQrOuter = MIN_QR_SIZE + QR_PADDING * 2;
        int targetControlsWidth = clamp(availableWidth * 2 / 5, 140, 200);
        int controlsWidth = Math.min(targetControlsWidth,
            Math.max(80, availableWidth - COLUMN_GAP - minQrOuter));
        int horizontalQrSize = Math.max(1,
            availableWidth - COLUMN_GAP - controlsWidth - QR_PADDING * 2);

        int verticalQrSize = Math.max(MIN_QR_SIZE, availableHeight - QR_PADDING * 2);
        int qrSize = Math.max(1, Math.min(MAX_QR_SIZE, Math.min(horizontalQrSize, verticalQrSize)));
        int qrPanelSize = qrSize + QR_PADDING * 2;
        int mainHeight = Math.max(controlsHeight, qrPanelSize);

        int groupWidth = qrPanelSize + COLUMN_GAP + controlsWidth;
        int groupX = Math.max(pageMargin, (width - groupWidth) / 2);
        int controlsX = groupX + qrPanelSize + COLUMN_GAP;
        int qrPanelY = CONTENT_TOP + Math.max(0, (mainHeight - qrPanelSize) / 2);
        Rect qrPanel = new Rect(groupX, qrPanelY, qrPanelSize, qrPanelSize);
        Rect qrImage = new Rect(groupX + QR_PADDING, qrPanelY + QR_PADDING, qrSize, qrSize);

        Rect firstButton = new Rect(controlsX, CONTENT_TOP, controlsWidth, BUTTON_HEIGHT);
        Rect secondButton = new Rect(controlsX, firstButton.bottom() + controlGap,
            controlsWidth, BUTTON_HEIGHT);
        Rect thirdButton = new Rect(controlsX, secondButton.bottom() + controlGap,
            controlsWidth, BUTTON_HEIGHT);
        List<Rect> buttons = List.of(firstButton, secondButton, thirdButton);

        int manualLabelY = thirdButton.bottom() + manualGap;
        Rect manualInput = new Rect(controlsX, manualLabelY + manualHeight, controlsWidth, BUTTON_HEIGHT);
        Rect controls = new Rect(controlsX, CONTENT_TOP, controlsWidth,
            manualInput.bottom() - CONTENT_TOP);

        return new Layout(qrPanel, qrImage, controls, manualInput, manualLabelY, manualHeight > 0,
            buttons, doneButton, compact);
    }

    private static int getControlsHeight() {
        return BUTTON_HEIGHT * 3 + CONTROL_GAP * 2 + MANUAL_LABEL_GAP + MANUAL_LABEL_HEIGHT + BUTTON_HEIGHT;
    }

    private static int getTinyControlsHeight() {
        return BUTTON_HEIGHT * 3 + MANUAL_LABEL_HEIGHT + BUTTON_HEIGHT;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    record Layout(
        Rect qrPanel,
        Rect qrImage,
        Rect controls,
        Rect manualInput,
        int manualLabelY,
        boolean showManualLabel,
        List<Rect> controlButtons,
        Rect doneButton,
        boolean compactControls
    ) {
    }

    record Rect(int x, int y, int width, int height) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }
    }
}

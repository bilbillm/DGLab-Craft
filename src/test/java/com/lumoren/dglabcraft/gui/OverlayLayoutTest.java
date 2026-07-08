package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverlayLayoutTest {
    @Test
    void migratesLegacyHudCornersToDefaultRects() {
        assertEquals(new OverlayLayout.Rect(5, 5, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(0, 320, 180));
        assertEquals(new OverlayLayout.Rect(227, 5, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(1, 320, 180));
        assertEquals(new OverlayLayout.Rect(5, 111, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(2, 320, 180));
        assertEquals(new OverlayLayout.Rect(227, 111, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(3, 320, 180));
    }

    @Test
    void defaultWaveformStartsNextToDefaultHud() {
        assertEquals(new OverlayLayout.Rect(96, 5, OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT),
            OverlayLayout.defaultWaveformRect(640, 360));
    }

    @Test
    void clampsRectsInsideScreenWithMargin() {
        OverlayLayout.Rect rect = OverlayLayout.clampRect(new OverlayLayout.Rect(999, -20, 40, 20), 100, 80);

        assertEquals(new OverlayLayout.Rect(55, 5, 40, 20), rect);
    }

    @Test
    void dragUsesMousePositionMinusGrabOffsetAndClamps() {
        OverlayLayout.Rect start = new OverlayLayout.Rect(20, 20, 40, 20);
        OverlayLayout.Rect dragged = OverlayLayout.drag(start, 80, 50, 10, 5, 100, 80);

        assertEquals(new OverlayLayout.Rect(55, 45, 40, 20), dragged);
    }

    @Test
    void ratioConversionUsesAvailableScreenSpace() {
        assertEquals(0.5D, OverlayLayout.ratioFromPixel(50, 100, 200), 0.0001D);
        assertEquals(0.0D, OverlayLayout.ratioFromPixel(0, 200, 200), 0.0001D);
        assertEquals(1.0D, OverlayLayout.ratioFromPixel(999, 100, 200), 0.0001D);
    }

    @Test
    void savedRatioRequiresBothCoordinates() {
        assertFalse(OverlayLayout.hasSavedRatio(-1.0D, 0.2D));
        assertFalse(OverlayLayout.hasSavedRatio(0.2D, -1.0D));
        assertTrue(OverlayLayout.hasSavedRatio(0.2D, 0.3D));
    }

    @Test
    void scaledRectKeepsAspectRatio() {
        OverlayLayout.Rect rect = OverlayLayout.scaledRectFromRatio(0.0D, 0.0D,
            OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, 1.5D, 640, 360);

        assertEquals(288, rect.width());
        assertEquals(129, rect.height());
    }

    @Test
    void resizeRightEdgeKeepsAspectRatio() {
        OverlayLayout.Rect start = new OverlayLayout.Rect(20, 20, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT);
        OverlayLayout.ResizeHandle handle = new OverlayLayout.ResizeHandle(false, true, false, false);

        OverlayLayout.Rect resized = OverlayLayout.resize(start, 20 + OverlayLayout.HUD_WIDTH * 2.0D, 40,
            handle, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, 640, 360);

        assertEquals(176, resized.width());
        assertEquals(128, resized.height());
        assertEquals(20, resized.x());
        assertEquals(20, resized.y());
    }

    @Test
    void resizeLeftEdgeAnchorsRightSide() {
        OverlayLayout.Rect start = new OverlayLayout.Rect(300, 20, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT);
        OverlayLayout.ResizeHandle handle = new OverlayLayout.ResizeHandle(true, false, false, false);

        OverlayLayout.Rect resized = OverlayLayout.resize(start, 300 - OverlayLayout.HUD_WIDTH, 40,
            handle, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, 640, 360);

        assertEquals(176, resized.width());
        assertEquals(128, resized.height());
        assertEquals(300 + OverlayLayout.HUD_WIDTH - 176, resized.x());
    }

    @Test
    void resizeHandleDetectsPanelBorderOnly() {
        OverlayLayout.Rect rect = new OverlayLayout.Rect(20, 30, 100, 60);

        assertEquals(OverlayLayout.ResizeHandle.NONE, OverlayLayout.resizeHandle(rect, 60, 60, 8));
        assertEquals(new OverlayLayout.ResizeHandle(true, false, true, false), OverlayLayout.resizeHandle(rect, 22, 32, 8));
        assertEquals(new OverlayLayout.ResizeHandle(false, true, false, true), OverlayLayout.resizeHandle(rect, 118, 88, 8));
    }

    @Test
    void scaleFromRectUsesBaseWidth() {
        assertEquals(1.5D, OverlayLayout.scaleFromRect(new OverlayLayout.Rect(0, 0, 132, 96), OverlayLayout.HUD_WIDTH), 0.0001D);
    }

    @Test
    void scaleClampsToCompactMinimum() {
        assertEquals(0.375D, OverlayLayout.clampScale(0.1D, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, 640, 360), 0.0001D);
    }
}

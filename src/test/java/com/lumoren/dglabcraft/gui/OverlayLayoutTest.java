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
        assertEquals(new OverlayLayout.Rect(161, 5, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(1, 320, 180));
        assertEquals(new OverlayLayout.Rect(5, 121, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(2, 320, 180));
        assertEquals(new OverlayLayout.Rect(161, 121, OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT),
            OverlayLayout.defaultHudRect(3, 320, 180));
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
}

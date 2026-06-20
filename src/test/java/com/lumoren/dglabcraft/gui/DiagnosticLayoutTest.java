package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticLayoutTest {
    @Test
    void maxScrollIsZeroWhenContentFitsViewport() {
        assertEquals(0, DiagnosticLayout.maxScroll(80, 40, 140));
    }

    @Test
    void maxScrollUsesContentOverflowOnly() {
        assertEquals(60, DiagnosticLayout.maxScroll(160, 40, 140));
    }

    @Test
    void scrollWheelClampsToViewportBounds() {
        assertEquals(24, DiagnosticLayout.scrollByWheel(0, -1.0, 60));
        assertEquals(0, DiagnosticLayout.scrollByWheel(10, 1.0, 60));
        assertEquals(60, DiagnosticLayout.scrollByWheel(50, -1.0, 60));
    }

    @Test
    void viewportBottomReservesFixedButtonArea() {
        assertEquals(138, DiagnosticLayout.viewportBottom(200));
    }

    @Test
    void lineVisibilityDoesNotEnterButtonArea() {
        assertTrue(DiagnosticLayout.isLineFullyVisible(120, 10, 40, 138));
        assertFalse(DiagnosticLayout.isLineFullyVisible(132, 10, 40, 138));
        assertFalse(DiagnosticLayout.isLineFullyVisible(30, 10, 40, 138));
    }
}

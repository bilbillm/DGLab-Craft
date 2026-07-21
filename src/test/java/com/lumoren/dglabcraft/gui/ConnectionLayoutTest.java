package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionLayoutTest {
    @Test
    void wideScreenKeepsQrControlsHelpAndDoneInSeparateRegions() {
        assertRegionsDoNotOverlap(ConnectionLayout.calculate(854, 480));
    }

    @Test
    void highGuiScaleStillLeavesUsableHelpViewport() {
        ConnectionLayout.Layout layout = ConnectionLayout.calculate(512, 300);

        assertTrue(layout.compactControls());
        assertRegionsDoNotOverlap(layout);
        assertEquals(64, layout.qrImage().width());
        assertEquals(64, layout.controls().y());
        assertTrue(layout.helpViewport().height() >= 105);
    }

    @Test
    void headerUsesTighterEvenLineSpacing() {
        assertEquals(16, ConnectionLayout.HEADER_STATUS_Y - ConnectionLayout.HEADER_TITLE_Y);
        assertEquals(16, ConnectionLayout.HEADER_ADDRESS_Y - ConnectionLayout.HEADER_STATUS_Y);
    }

    @Test
    void compactScreenUsesCompactControlsWithoutCoveringDoneButton() {
        ConnectionLayout.Layout layout = ConnectionLayout.calculate(427, 240);

        assertTrue(layout.compactControls());
        assertRegionsDoNotOverlap(layout);
        assertTrue(layout.helpViewport().height() >= 20);
    }

    private void assertRegionsDoNotOverlap(ConnectionLayout.Layout layout) {
        assertFalse(layout.qrPanel().intersects(layout.controls()));
        assertTrue(layout.qrPanel().bottom() <= layout.helpViewport().y());
        assertTrue(layout.controls().bottom() <= layout.helpViewport().y());
        assertTrue(layout.helpViewport().bottom() <= layout.doneButton().y());

        List<ConnectionLayout.Rect> widgets = new ArrayList<>(layout.controlButtons());
        widgets.add(layout.manualInput());
        for (int i = 0; i < widgets.size(); i++) {
            assertTrue(layout.controls().contains(widgets.get(i)));
            for (int j = i + 1; j < widgets.size(); j++) {
                assertFalse(widgets.get(i).intersects(widgets.get(j)));
            }
        }
    }
}


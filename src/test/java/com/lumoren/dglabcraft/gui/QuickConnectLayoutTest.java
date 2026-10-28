package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuickConnectLayoutTest {
    @Test void codeAndActionsFitAtAllSupportedGuiScales() {
        for (int[] size : new int[][]{{320,240},{427,240},{480,270},{640,360},{960,540},{1920,1080}}) {
            var layout = QuickConnectLayout.calculate(size[0], size[1]);
            assertTrue(layout.qrSize() >= 120, "QR must stay scannable at minimum GUI size");
            assertTrue(layout.qrY() + layout.qrSize() + 20 < layout.buttonY());
            assertTrue(layout.buttonY() + 20 <= size[1]);
            assertTrue(layout.qrSize() + 10 <= size[0]);
        }
    }
}

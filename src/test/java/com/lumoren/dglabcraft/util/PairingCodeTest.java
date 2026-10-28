package com.lumoren.dglabcraft.util;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PairingCodeTest {
    @Test void pairingCodeRoundTripsWithoutAFileOrExternalImageViewer() throws Exception {
        String url = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#wss%3A%2F%2Fws.dungeon-lab.cn%2F01234567-abcd-abcd-abcd-012345678901";
        var matrix = QRCodeGenerator.createMatrix(url);
        int[] pixels = new int[matrix.getWidth() * matrix.getHeight()];
        for (int y = 0; y < matrix.getHeight(); y++) for (int x = 0; x < matrix.getWidth(); x++) {
            pixels[y * matrix.getWidth() + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
        }
        var source = new RGBLuminanceSource(matrix.getWidth(), matrix.getHeight(), pixels);
        assertEquals(url, new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source))).getText());
    }
}

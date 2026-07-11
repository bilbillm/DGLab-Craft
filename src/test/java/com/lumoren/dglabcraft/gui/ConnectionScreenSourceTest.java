package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionScreenSourceTest {
    private static final Path SOURCE = Path.of("src/main/java/com/lumoren/dglabcraft/gui/ConnectionScreen.java");

    @Test
    void qrTextureBlitScalesTheEntireSourceImage() throws IOException {
        String source = Files.readString(SOURCE).replaceAll("\\s+", " ");

        assertTrue(source.contains(
            "guiGraphics.blit(qrTextureLocation, layout.qrImage().x(), layout.qrImage().y(), "
                + "layout.qrImage().width(), layout.qrImage().height(), 0.0F, 0.0F, "
                + "qrTextureWidth, qrTextureHeight, qrTextureWidth, qrTextureHeight);"
        ));
    }
}

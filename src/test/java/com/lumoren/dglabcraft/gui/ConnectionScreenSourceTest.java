package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionScreenSourceTest {
    private static final Path SOURCE = Paths.get("src/main/java/com/lumoren/dglabcraft/gui/ConnectionScreen.java");

    @Test
    void qrTextureBlitScalesTheEntireSourceImage() throws IOException {
        String source = new String(Files.readAllBytes(SOURCE), StandardCharsets.UTF_8).replaceAll("\\s+", " ");

        assertTrue(source.contains(
            "Minecraft.getInstance().getTextureManager().bind(qrTextureLocation); blit(poseStack, layout.qrImage().x(), layout.qrImage().y(), "
                + "layout.qrImage().width(), layout.qrImage().height(), 0.0F, 0.0F, "
                + "qrTextureWidth, qrTextureHeight, qrTextureWidth, qrTextureHeight);"
        ));
    }
}

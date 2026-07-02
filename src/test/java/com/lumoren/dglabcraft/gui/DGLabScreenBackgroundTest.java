package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DGLabScreenBackgroundTest {
    private static final Path GUI_DIR = Path.of("src/main/java/com/lumoren/dglabcraft/gui");
    private static final List<String> SCREENS = List.of(
        "MainScreen.java",
        "DGLabCraftScreen.java",
        "ConnectionScreen.java",
        "DiagnosticScreen.java",
        "OverlayEditScreen.java"
    );

    @Test
    void playerFacingScreensUseOpaqueModBackgroundInsteadOfVanillaBlurredBackground() throws IOException {
        for (String screen : SCREENS) {
            String source = Files.readString(GUI_DIR.resolve(screen));

            assertFalse(source.contains("renderBackground("), screen + " must not call vanilla blurred background");
            assertTrue(source.contains("DGLabScreenBackground.render("), screen + " must render the opaque mod background");
        }
    }
}

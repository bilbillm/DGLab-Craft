package com.lumoren.dglabcraft.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DGLabRenderOrderTest {
    private static final Path GUI_DIR = Path.of("src/main/java/com/lumoren/dglabcraft/gui");

    @Test
    void plainMainScreenTextRendersAfterSuperRender() throws IOException {
        assertDrawsAfterSuper("MainScreen.java", "centeredText");
    }

    @Test
    void connectionTextRendersAfterWidgetRender() throws IOException {
        assertDrawsAfterSuper("ConnectionScreen.java", "centeredText");
    }

    @Test
    void diagnosticsTextRendersAfterWidgetRender() throws IOException {
        assertDrawsAfterSuper("DiagnosticScreen.java", "centeredText");
        assertDrawsAfterSuper("DiagnosticScreen.java", "guiGraphics.text(");
    }

    @Test
    void overlayEditorTextAndPanelsRenderAfterButtons() throws IOException {
        assertDrawsAfterSuper("OverlayEditScreen.java", "DGLabCraftHUD.renderHudPanel");
        assertDrawsAfterSuper("OverlayEditScreen.java", "DGLabCraftHUD.renderWaveformPanel");
    }

    @Test
    void settingsListUsesSuperRenderPipeline() throws IOException {
        String source = Files.readString(GUI_DIR.resolve("DGLabCraftScreen.java"));

        assertTrue(source.contains("this.addRenderableWidget(this.list);"));
        assertFalse(source.contains("this.list.render(guiGraphics"));
        assertFalse(source.contains("this.list.extractRenderState(guiGraphics"));
        assertDrawsAfterSuper("DGLabCraftScreen.java", "centeredText");
    }

    private void assertDrawsAfterSuper(String file, String marker) throws IOException {
        String source = Files.readString(GUI_DIR.resolve(file));
        int superRender = source.indexOf("super.extractRenderState(guiGraphics");
        int markerIndex = source.indexOf(marker);

        assertTrue(superRender >= 0, file + " must call super.render");
        assertTrue(markerIndex > superRender, file + " must render " + marker + " after super.render");
    }
}

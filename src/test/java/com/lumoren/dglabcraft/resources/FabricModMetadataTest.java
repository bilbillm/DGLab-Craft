package com.lumoren.dglabcraft.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FabricModMetadataTest {
    private static final Path MOD_JSON = Path.of("src/main/resources/fabric.mod.json");

    @Test
    void dependsOnFabricApiModuleId() throws IOException {
        JsonObject depends = JsonParser.parseString(Files.readString(MOD_JSON))
            .getAsJsonObject()
            .getAsJsonObject("depends");

        assertEquals("*", depends.get("fabric").getAsString());
        assertFalse(depends.has("fabric-api"));
    }
}

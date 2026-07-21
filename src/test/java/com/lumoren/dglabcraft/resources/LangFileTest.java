package com.lumoren.dglabcraft.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangFileTest {
    private static final Path LANG_DIR = Paths.get("src/main/resources/assets/dglabcraft/lang");
    private static final String[] LOCALES = {"zh_cn", "ja_jp", "de_de", "ru_ru", "fr_fr"};

    @Test
    void localeKeysMatchEnglishBaseline() throws IOException {
        Set<String> englishKeys = keys("en_us");

        for (String locale : LOCALES) {
            assertEquals(englishKeys, keys(locale), locale + " keys must match en_us.json");
        }
    }

    private Set<String> keys(String locale) throws IOException {
        String json = new String(Files.readAllBytes(LANG_DIR.resolve(locale + ".json")), StandardCharsets.UTF_8);
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        TreeSet<String> keys = new TreeSet<>();
        object.entrySet().forEach(entry -> keys.add(entry.getKey()));
        return keys;
    }
}

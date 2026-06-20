package com.lumoren.dglabcraft.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangFileTest {
    private static final Path LANG_DIR = Path.of("src/main/resources/assets/dglabcraft/lang");
    private static final String[] LOCALES = {"zh_cn", "ja_jp", "de_de", "ru_ru", "fr_fr"};

    @Test
    void localeKeysMatchEnglishBaseline() throws IOException {
        Set<String> englishKeys = keys("en_us");

        for (String locale : LOCALES) {
            assertEquals(englishKeys, keys(locale), locale + " keys must match en_us.json");
        }
    }

    private Set<String> keys(String locale) throws IOException {
        String json = Files.readString(LANG_DIR.resolve(locale + ".json"));
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return new TreeSet<>(object.keySet());
    }
}

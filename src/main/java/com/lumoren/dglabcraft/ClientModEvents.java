package com.lumoren.dglabcraft;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class ClientModEvents {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("dglabcraft", "main"));

    private static final KeyMapping OPEN_SETTINGS_KEY_INSTANCE = new KeyMapping(
        "key.dglabcraft.open_settings",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        CATEGORY
    );

    public static final Supplier<KeyMapping> OPEN_SETTINGS_KEY = () -> OPEN_SETTINGS_KEY_INSTANCE;

    private static boolean registered;

    private ClientModEvents() {
    }

    public static void register() {
        if (!registered) {
            KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS_KEY_INSTANCE);
            registered = true;
        }
    }
}

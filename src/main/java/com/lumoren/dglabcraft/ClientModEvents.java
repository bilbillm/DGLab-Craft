package com.lumoren.dglabcraft;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class ClientModEvents {
    public static final KeyBinding OPEN_SETTINGS_KEY = new KeyBinding(
        "key.dglabcraft.open_settings",
        Keyboard.KEY_K,
        "key.categories.dglabcraft"
    );

    private ClientModEvents() {
    }

    public static void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(OPEN_SETTINGS_KEY);
    }
}

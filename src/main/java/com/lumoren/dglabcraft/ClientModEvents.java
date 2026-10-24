package com.lumoren.dglabcraft;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class ClientModEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("DGLabCraft-Keybinding");

    private static final KeyMapping OPEN_SETTINGS_KEY_INSTANCE = new KeyMapping(
        "key.dglabcraft.open_settings",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        "key.categories.dglabcraft"
    );

    public static final Supplier<KeyMapping> OPEN_SETTINGS_KEY = () -> OPEN_SETTINGS_KEY_INSTANCE;

    private static boolean registered;
    private static boolean conflictChecked;
    private static boolean openedLogged;

    private ClientModEvents() {
    }

    public static void register() {
        if (!registered) {
            KeyBindingHelper.registerKeyBinding(OPEN_SETTINGS_KEY_INSTANCE);
            registered = true;
            LOGGER.info("Registered key binding '{}' with default key {}",
                OPEN_SETTINGS_KEY_INSTANCE.getName(), OPEN_SETTINGS_KEY_INSTANCE.getDefaultKey().getName());
        }
    }

    /**
     * 首次进入游戏时检查快捷键是否被别的模组占用。
     * “按 K 没反应”多数是快捷键冲突或按键被改过，这里写日志并在游戏内提示一次。
     */
    public static void checkKeyConflicts(Minecraft minecraft) {
        if (conflictChecked || minecraft.options == null || minecraft.player == null) {
            return;
        }
        conflictChecked = true;

        String ownLabel = OPEN_SETTINGS_KEY_INSTANCE.getTranslatedKeyMessage().getString();
        String unboundLabel = new TranslatableComponent("key.keyboard.unknown").getString();
        if (ownLabel.isBlank() || ownLabel.equals(unboundLabel)) {
            LOGGER.warn("DGLab Craft key binding is not bound to any key; open the settings from Options -> Controls");
            return;
        }

        for (KeyMapping other : minecraft.options.keyMappings) {
            if (other == OPEN_SETTINGS_KEY_INSTANCE) {
                continue;
            }
            if (!ownLabel.equals(other.getTranslatedKeyMessage().getString())) {
                continue;
            }

            LOGGER.warn("Key binding conflict: '{}' and '{}' both use {}",
                OPEN_SETTINGS_KEY_INSTANCE.getName(), other.getName(), ownLabel);

            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    new TranslatableComponent("message.dglabcraft.key_conflict", other.getName()), false);
            }
            return;
        }
    }

    /** 快捷键成功打开过一次界面时写一条日志，方便排查“按 K 没反应”。 */
    public static void logOpenedOnce() {
        if (openedLogged) {
            return;
        }
        openedLogged = true;
        LOGGER.info("Opened DGLab Craft settings from key binding");
    }
}

package com.lumoren.dglabcraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
final class KeyBindingDiagnostics {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("DGLabCraft-Keybinding");
    private static boolean registeredLogged;
    private static Object checkedPlayer;
    static void check(Minecraft minecraft, KeyBinding own) {
        if (!registeredLogged) { LOG.info("Registered key binding '" + own.getKeyDescription() + "'"); registeredLogged = true; }
        if (minecraft.player == null || minecraft.gameSettings == null || checkedPlayer == minecraft.player) return;
        checkedPlayer = minecraft.player;
        if (own.getKeyCode() == 0) { LOG.warning("DGLab Craft key is unbound; check Options -> Controls"); return; }
        String label = String.valueOf(own.getKeyCode());
        for (KeyBinding other : minecraft.gameSettings.keyBindings) {
            if (other == own) continue;
            if (own.getKeyCode() == other.getKeyCode()) {
                LOG.warning("Key binding conflict: '" + own.getKeyDescription() + "' and '" + other.getKeyDescription() + "'");
                minecraft.player.sendMessage(new net.minecraft.util.text.TextComponentTranslation("message.dglabcraft.key_conflict", other.getKeyDescription()));
                return;
            }
        }
    }
}

package com.lumoren.dglabcraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
final class KeyBindingDiagnostics {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("DGLabCraft-Keybinding");
    private static boolean registeredLogged;
    private static Object checkedPlayer;
    static void check(Minecraft minecraft, KeyMapping own) {
        if (!registeredLogged) { LOG.info("Registered key binding '" + own.getName() + "'"); registeredLogged = true; }
        if (minecraft.player == null || minecraft.options == null || checkedPlayer == minecraft.player) return;
        checkedPlayer = minecraft.player;
        if (own.isUnbound()) { LOG.warning("DGLab Craft key is unbound; check Options -> Controls"); return; }
        String label = own.getTranslatedKeyMessage().getString();
        for (KeyMapping other : minecraft.options.keyMappings) {
            if (other == own) continue;
            if (label.equals(other.getTranslatedKeyMessage().getString())) {
                LOG.warning("Key binding conflict: '" + own.getName() + "' and '" + other.getName() + "'");
                minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.dglabcraft.key_conflict", other.getName()), false);
                return;
            }
        }
    }
}

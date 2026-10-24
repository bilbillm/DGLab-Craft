package com.lumoren.dglabcraft.events;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
public class FadeManager {
    private static boolean hadWorld, idleResetSent;
    private static int tick;
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        if (mc.thePlayer == null || mc.theWorld == null) {
            if (hadWorld) ws.safeSilenceAll();
            hadWorld = false; idleResetSent = false; tick = 0; return;
        }
        hadWorld = true;
        if (++tick % 10 != 0) return;
        if (ws.hasActiveEffects()) idleResetSent = false;
        else if (!idleResetSent) { ws.safeSilenceAll(); idleResetSent = true; }
    }
    public static void updateDamage(int a, int b) { idleResetSent = false; }
    public static void updateHeartbeat(int a, int b) { idleResetSent = false; }
    public static void updateEnvironment(int a, int b) { idleResetSent = false; }
    public static void stopHeartbeat() { }
    public static void stopEnvironment() { }
    public static void stopDamage() { }
}

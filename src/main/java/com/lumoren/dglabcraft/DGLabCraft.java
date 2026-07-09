package com.lumoren.dglabcraft;

import com.lumoren.dglabcraft.events.DamageHandler;
import com.lumoren.dglabcraft.events.EnvironmentHandler;
import com.lumoren.dglabcraft.events.FadeManager;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.gui.DGLabCraftHUD;
import com.lumoren.dglabcraft.gui.MainScreen;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

public class DGLabCraft implements ClientModInitializer {
    public static final String MODID = "dglabcraft";
    public static final DamageHandler DAMAGE_HANDLER = new DamageHandler();

    private final EnvironmentHandler environmentHandler = new EnvironmentHandler();
    private final HeartbeatHandler heartbeatHandler = new HeartbeatHandler();

    @Override
    public void onInitializeClient() {
        ClientModEvents.register();

        WebSocketServerManager.getInstance().start();
        Runtime.getRuntime().addShutdownHook(new Thread(
            () -> WebSocketServerManager.getInstance().stop(),
            "DGLabCraft-Shutdown"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleOpenSettings(client);
            DAMAGE_HANDLER.onClientTick(client);
            environmentHandler.onClientTick(client);
            heartbeatHandler.onClientTick(client);
            FadeManager.onClientTick(client);
        });

        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> DGLabCraftHUD.render(guiGraphics));
    }

    private void handleOpenSettings(Minecraft minecraft) {
        while (ClientModEvents.OPEN_SETTINGS_KEY.get().consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new MainScreen());
            }
        }
    }
}

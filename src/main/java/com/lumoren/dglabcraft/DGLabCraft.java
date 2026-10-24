package com.lumoren.dglabcraft;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.events.DamageHandler;
import com.lumoren.dglabcraft.events.EnvironmentHandler;
import com.lumoren.dglabcraft.events.FadeManager;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.events.StatusEffectHandler;
import com.lumoren.dglabcraft.gui.DGLabCraftHUD;
import com.lumoren.dglabcraft.gui.MainScreen;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = DGLabCraft.MODID, name = DGLabCraft.NAME, version = DGLabCraft.VERSION, acceptableRemoteVersions = "*")
public class DGLabCraft {
    public static final String MODID = "dglabcraft";
    public static final String NAME = "DGLab Craft";
    public static final String VERSION = "1.1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        ModConfig.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        ClientModEvents.registerKeyBindings();
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new StatusEffectHandler());
        FMLCommonHandler.instance().bus().register(new EnvironmentHandler());
        FMLCommonHandler.instance().bus().register(new HeartbeatHandler());
        FMLCommonHandler.instance().bus().register(new FadeManager());
        MinecraftForge.EVENT_BUS.register(new DamageHandler());
        MinecraftForge.EVENT_BUS.register(new DGLabCraftHUD());
        WebSocketServerManager.getInstance().start();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (ClientModEvents.OPEN_SETTINGS_KEY.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new MainScreen());
            }
        }
    }
}

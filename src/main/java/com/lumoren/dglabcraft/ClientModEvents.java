package com.lumoren.dglabcraft;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DGLabCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    // 使用 Lazy 延迟加载 KeyMapping，确保在被请求时已实例化
    public static final Lazy<KeyMapping> OPEN_SETTINGS_KEY = Lazy.of(() -> new KeyMapping(
            "key.dglabcraft.open_settings",
            GLFW.GLFW_KEY_K,
            "key.categories.dglabcraft"
    ));

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 直接在此处注册，不会有 null 的问题
        event.enqueueWork(() -> ClientRegistry.registerKeyBinding(OPEN_SETTINGS_KEY.get()));
    }
}

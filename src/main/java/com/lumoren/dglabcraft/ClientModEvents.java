package com.lumoren.dglabcraft;

import com.google.common.base.Suppliers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@EventBusSubscriber(modid = DGLabCraft.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("dglabcraft", "main"));

    // 使用 Supplier 延迟加载 KeyMapping，确保在被请求时已实例化
    public static final Supplier<KeyMapping> OPEN_SETTINGS_KEY = Suppliers.memoize(() -> new KeyMapping(
            "key.dglabcraft.open_settings",
            GLFW.GLFW_KEY_K,
            CATEGORY
    ));

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        // 直接在此处注册，不会有 null 的问题
        event.register(OPEN_SETTINGS_KEY.get());
    }
}

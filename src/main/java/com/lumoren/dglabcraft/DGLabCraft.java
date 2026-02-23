package com.lumoren.dglabcraft;

import com.lumoren.dglabcraft.events.DamageHandler;
import com.lumoren.dglabcraft.events.EnvironmentHandler;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.events.StatusEffectHandler;
import com.lumoren.dglabcraft.gui.DGLabCraftScreen;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(DGLabCraft.MODID)
public class DGLabCraft
{
    public static final String MODID = "dglabcraft";

    // 客户端按键绑定
    public static KeyMapping OPEN_SETTINGS_KEY;

    public DGLabCraft(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册通用设置
        modEventBus.addListener(this::commonSetup);

        // 注册客户端设置
        modEventBus.addListener(this::clientSetup);

        // 注册 Forge 配置
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
            com.lumoren.dglabcraft.config.ModConfig.SPEC);

        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DamageHandler());
        MinecraftForge.EVENT_BUS.register(new StatusEffectHandler());
        MinecraftForge.EVENT_BUS.register(new EnvironmentHandler());
        MinecraftForge.EVENT_BUS.register(new HeartbeatHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 启动 WebSocket 服务器
        WebSocketServerManager.getInstance().start();
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        // 注册按键映射
        OPEN_SETTINGS_KEY = new KeyMapping(
            "key.dglabcraft.open_settings",
            GLFW.GLFW_KEY_K,
            "key.categories.dglabcraft"
        );
    }

    /**
     * 注册按键映射
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        if (OPEN_SETTINGS_KEY != null) {
            event.register(OPEN_SETTINGS_KEY);
        }
    }

    /**
     * 输入处理 - 每 tick 检查按键
     */
    @SubscribeEvent
    public void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (OPEN_SETTINGS_KEY != null && OPEN_SETTINGS_KEY.isDown()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new DGLabCraftScreen(null));
            }
        }
    }
}

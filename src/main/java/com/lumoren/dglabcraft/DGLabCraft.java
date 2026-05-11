package com.lumoren.dglabcraft;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.events.DamageHandler;
import com.lumoren.dglabcraft.events.EnvironmentHandler;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.gui.MainScreen;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;

@Mod(DGLabCraft.MODID)
public class DGLabCraft
{
    public static final String MODID = "dglabcraft";

    public DGLabCraft(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册通用设置
        modEventBus.addListener(this::commonSetup);

        // 注册 Forge 配置
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
            com.lumoren.dglabcraft.config.ModConfig.SPEC);

        // 注册事件总线 (注意: StatusEffectHandler 已废弃，药水效果由 DamageHandler 处理)
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DamageHandler());
        MinecraftForge.EVENT_BUS.register(new EnvironmentHandler());
        MinecraftForge.EVENT_BUS.register(new HeartbeatHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 启动 WebSocket 服务器
        WebSocketServerManager.getInstance().start();

        // 注册 JVM 关闭钩子 — 确保游戏退出时清理 WebSocket 连接
        Runtime.getRuntime().addShutdownHook(new Thread(
            () -> WebSocketServerManager.getInstance().stop(),
            "DGLabCraft-Shutdown"));

        // 波形管理器使用懒加载，首次使用时自动初始化
    }

    /**
     * 输入处理 - 每 tick 检查按键
     */
    @SubscribeEvent
    public void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (ClientModEvents.OPEN_SETTINGS_KEY.get().consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new MainScreen());
            }
        }
    }
}

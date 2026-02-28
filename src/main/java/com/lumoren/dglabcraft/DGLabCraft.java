package com.lumoren.dglabcraft;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.events.DamageHandler;
import com.lumoren.dglabcraft.events.EnvironmentHandler;
import com.lumoren.dglabcraft.events.FadeManager;
import com.lumoren.dglabcraft.events.HeartbeatHandler;
import com.lumoren.dglabcraft.events.StatusEffectHandler;
import com.lumoren.dglabcraft.gui.MainScreen;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(DGLabCraft.MODID)
public class DGLabCraft
{
    public static final String MODID = "dglabcraft";

    public DGLabCraft(IEventBus modEventBus, ModContainer modContainer)
    {
        // 注册通用设置
        modEventBus.addListener(this::commonSetup);

        // 注册 NeoForge 配置
        modContainer.registerConfig(ModConfig.Type.COMMON, DGLabConfig.SPEC);

        // 注册事件总线
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new DamageHandler());
        NeoForge.EVENT_BUS.register(new StatusEffectHandler());
        NeoForge.EVENT_BUS.register(new EnvironmentHandler());
        NeoForge.EVENT_BUS.register(new HeartbeatHandler());
        NeoForge.EVENT_BUS.register(FadeManager.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 启动 WebSocket 服务器
        WebSocketServerManager.getInstance().start();

        // 波形管理器使用懒加载，首次使用时自动初始化
    }

    /**
     * 输入处理 - 每 tick 检查按键
     */
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (ClientModEvents.OPEN_SETTINGS_KEY.get().consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new MainScreen());
            }
        }
    }
}

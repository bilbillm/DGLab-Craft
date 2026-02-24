package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 环境反馈处理
 * 参考 DG_LAB 算法实现
 * 处理生物群系、天气、特殊方块等环境因素
 */
public class EnvironmentHandler {

    private int tickCounter = 0;
    private boolean wasInSnow = false;
    private boolean wasInNether = false;
    private boolean wasInCold = false;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        Player player = (Player) event.getEntity();
        handleClientEnvironment(player);
    }

    private void handleClientEnvironment(Player player) {
        tickCounter++;
        // 每 10 tick 检查一次 (减少频繁调用)
        if (tickCounter % 10 != 0) return;

        int maxIntensity = ModConfig.BASE_MAX_INTENSITY.get();

        // 检查维度 - 通过获取 level 的 dimension 类型
        DimensionType dimType = player.level.dimensionType();
        // 下界 dimension type 的 registry name 包含 "nether"
        boolean isNetherDimension = dimType != null &&
            dimType.toString().toLowerCase().contains("nether");

        // 1. 下界环境反馈
        if (isNetherDimension) {
            if (!wasInNether) {
                wasInNether = true;
                wasInCold = false;
            }
            int intensity = (int)(10.0 * ModConfig.NETHER_INTENSITY.get());
            intensity = Math.min(intensity, maxIntensity);
            // 低频沉闷的波形
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 0);
        } else {
            wasInNether = false;
        }

        // 2. 寒冷环境检测 (通过雪/冰方块判断)
        if (!isNetherDimension) {
            // 检查附近是否有雪或冰
            boolean hasSnowNearby = hasSnowBlockNearby(player);
            if (hasSnowNearby) {
                if (!wasInCold) {
                    wasInCold = true;
                }
                int intensity = (int)(8.0 * ModConfig.COLD_INTENSITY.get());
                intensity = Math.min(intensity, maxIntensity);
                // 高频细碎的麻木感
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
            } else {
                wasInCold = false;
            }
        }

        // 3. 检查玩家脚下的方块
        checkPlayerFootBlock(player, maxIntensity);
    }

    /**
     * 检查附近是否有雪/冰方块
     */
    private boolean hasSnowBlockNearby(Player player) {
        BlockPos pos = player.blockPosition();
        // 检查脚下和周围的方块
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = player.level.getBlockState(pos.offset(x, y, z)).getBlock();
                    if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK ||
                        block == Blocks.ICE || block == Blocks.PACKED_ICE ||
                        block == Blocks.FROSTED_ICE || block == Blocks.POWDER_SNOW) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查玩家脚下的方块
     */
    private void checkPlayerFootBlock(Player player, int maxIntensity) {
        Block feetBlock = player.level.getBlockState(player.blockPosition().below()).getBlock();

        // 细雪 - 高频麻木感
        if (feetBlock == Blocks.POWDER_SNOW) {
            int intensity = (int)(10.0 * ModConfig.COLD_INTENSITY.get());
            intensity = Math.min(intensity, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
            wasInSnow = true;
        } else {
            if (wasInSnow) {
                // 离开细雪时发送停止信号
                WebSocketServerManager.getInstance().stopStimulus("A");
                wasInSnow = false;
            }
        }

        // 仙人掌 - 持续刺痛
        if (feetBlock == Blocks.CACTUS) {
            int intensity = Math.min(15, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
        }

        // 甜蜜泥浆 - 轻微粘稠感
        if (feetBlock == Blocks.HONEY_BLOCK) {
            int intensity = Math.min(5, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 0);
        }

        // 粘液块 - 弹跳感
        if (feetBlock == Blocks.SLIME_BLOCK) {
            int intensity = Math.min(8, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("A", "square", intensity, 0);
        }

        // 下界传送门方块 - 空间扭曲感
        if (feetBlock == Blocks.NETHER_PORTAL) {
            int intensity = Math.min(10, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
        }

        // 灵魂沙/灵魂土 - 低沉压迫感
        if (feetBlock == Blocks.SOUL_SAND || feetBlock == Blocks.SOUL_SOIL) {
            int intensity = (int)(10.0 * ModConfig.NETHER_INTENSITY.get());
            intensity = Math.min(intensity, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 0);
        }

        // 终界传送门方块 - 空间传送感
        if (feetBlock == Blocks.END_PORTAL || feetBlock == Blocks.END_GATEWAY) {
            int intensity = Math.min(12, maxIntensity);
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 0);
        }
    }
}

package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 环境反馈处理
 * 处理生物群系、天气、特殊方块等环境因素
 */
public class EnvironmentHandler {

    private int tickCounter = 0;
    private boolean wasInSnow = false;
    private boolean wasInNether = false;
    private boolean wasInCold = false;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (player.level.isClientSide) {
            handleClientEnvironment(player);
        }
    }

    private void handleClientEnvironment(Player player) {
        tickCounter++;
        // 每 10 tick 检查一次 (减少频繁调用)
        if (tickCounter % 10 != 0) return;

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
            double intensity = 0.2 * ModConfig.NETHER_INTENSITY.get();
            // 低频沉闷的波形
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 200);
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
                double intensity = 0.15 * ModConfig.COLD_INTENSITY.get();
                // 高频细碎的麻木感
                WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 100);
            } else {
                wasInCold = false;
            }
        }

        // 3. 检查玩家脚下的方块
        checkPlayerFootBlock(player);
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
    private void checkPlayerFootBlock(Player player) {
        Block feetBlock = player.level.getBlockState(player.blockPosition().below()).getBlock();

        // 细雪 - 高频麻木感
        if (feetBlock == Blocks.POWDER_SNOW) {
            double intensity = 0.2 * ModConfig.COLD_INTENSITY.get();
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 100);
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
            double intensity = 0.25;
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 200);
        }

        // 砂砾/沙子 (在水中) - 轻微压迫感
        if ((feetBlock == Blocks.GRAVEL || feetBlock == Blocks.SAND) && player.isInWater()) {
            double intensity = 0.1;
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 300);
        }

        // 甜蜜泥浆 - 轻微粘稠感
        if (feetBlock == Blocks.HONEY_BLOCK) {
            double intensity = 0.08;
            WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 200);
        }

        // 粘液块 - 弹跳感
        if (feetBlock == Blocks.SLIME_BLOCK) {
            double intensity = 0.1;
            WebSocketServerManager.getInstance().sendStimulus("A", "square", intensity, 150);
        }

        // 蜘蛛网 - 轻微束缚感
        if (feetBlock == Blocks.COBWEB) {
            double intensity = 0.12;
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 150);
        }

        // 下界传送门方块 - 空间扭曲感
        if (feetBlock == Blocks.NETHER_PORTAL) {
            double intensity = 0.15;
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 100);
        }

        // 灵魂沙/灵魂土 - 低沉压迫感
        if (feetBlock == Blocks.SOUL_SAND || feetBlock == Blocks.SOUL_SOIL) {
            double intensity = 0.18 * ModConfig.NETHER_INTENSITY.get();
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 300);
        }

        // 哭泣的黑曜石 - 轻微脉动
        if (feetBlock == Blocks.CRYING_OBSIDIAN) {
            double intensity = 0.1;
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 200);
        }

        // 附魔台附近 - 轻微酥麻感
        if (feetBlock == Blocks.ENCHANTING_TABLE || feetBlock == Blocks.BOOKSHELF) {
            double intensity = 0.07;
            WebSocketServerManager.getInstance().sendStimulus("B", "pulse", intensity, 250);
        }

        // 终界传送门方块 - 空间传送感
        if (feetBlock == Blocks.END_PORTAL || feetBlock == Blocks.END_GATEWAY) {
            double intensity = 0.2;
            WebSocketServerManager.getInstance().sendStimulus("A", "pulse", intensity, 100);
        }

        // 苔石/石头 - 轻微粗糙感
        if (feetBlock == Blocks.MOSSY_COBBLESTONE || feetBlock == Blocks.STONE) {
            double intensity = 0.03;
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 400);
        }

        // 灯笼下 - 轻微温暖
        if (player.level.getBlockState(player.blockPosition().above()).getBlock() == Blocks.LANTERN) {
            double intensity = 0.06;
            WebSocketServerManager.getInstance().sendStimulus("B", "sine", intensity, 400);
        }
    }
}

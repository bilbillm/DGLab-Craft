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
    private boolean wasInEnd = false;
    private boolean wasInCold = false;
    private boolean wasInNetherPortal = false;

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

        // 1. 下界环境反馈 - 每40tick发送一次 breath 波形，AB通道同步
        if (isNetherDimension) {
            if (!wasInNether) {
                wasInNether = true;
                wasInCold = false;
            }
            // 每 40 tick (2秒) 发送一次
            if (tickCounter % 40 == 0) {
                int intensity = (int)(10.0 * ModConfig.NETHER_MULTIPLIER.get());
                intensity = Math.min(intensity, maxIntensity);
                // A通道发送 breath 波形
                WebSocketServerManager.getInstance().sendWaveformData("A", "breath", intensity);
                // B通道同步发送 breath 波形
                WebSocketServerManager.getInstance().sendWaveformData("B", "breath", intensity);
            }
        } else {
            if (wasInNether) {
                // 离开下界时停止波形
                WebSocketServerManager.getInstance().stopStimulus("A");
                WebSocketServerManager.getInstance().stopStimulus("B");
            }
            wasInNether = false;
        }

        // 1.5 终界环境反馈 - 每40tick发送一次 tide 波形，AB通道同步
        DimensionType dimTypeEnd = player.level.dimensionType();
        boolean isEndDimension = dimTypeEnd != null &&
            dimTypeEnd.toString().toLowerCase().contains("end");
        if (isEndDimension) {
            if (!wasInEnd) {
                wasInEnd = true;
            }
            // 每 40 tick (2秒) 发送一次
            if (tickCounter % 40 == 0) {
                int intensity = Math.min(10, maxIntensity);
                // A通道发送 tide 波形
                WebSocketServerManager.getInstance().sendWaveformData("A", "tide", intensity);
                // B通道同步发送 tide 波形
                WebSocketServerManager.getInstance().sendWaveformData("B", "tide", intensity);
            }
        } else {
            if (wasInEnd) {
                // 离开终界时停止波形
                WebSocketServerManager.getInstance().stopStimulus("A");
                WebSocketServerManager.getInstance().stopStimulus("B");
            }
            wasInEnd = false;
        }

        // 2. 寒冷环境检测 (通过玩家是否接触细雪方块判断)
        if (!isNetherDimension) {
            // 检查玩家是否接触到细雪方块
            boolean touchingSnow = isTouchingBlock(player, Blocks.POWDER_SNOW);
            if (touchingSnow) {
                if (!wasInCold) {
                    wasInCold = true;
                }
                // 每1.5秒发送一次
                if (tickCounter % 30 == 0) {
                    int intensity = (int)(8.0 * ModConfig.FREEZE_MULTIPLIER.get());
                    intensity = Math.min(intensity, maxIntensity);
                    WebSocketServerManager.getInstance().sendWaveformData("A", "fast_pinch", intensity);
                }
            } else {
                wasInCold = false;
            }
        }

        // 3. 检查玩家脚下的方块
        checkPlayerFootBlock(player, maxIntensity, tickCounter);
    }

    /**
     * 检查玩家是否直接接触指定方块（只检查脚部和身体位置）
     */
    private boolean isTouchingBlock(Player player, Block targetBlock) {
        BlockPos pos = player.blockPosition();
        // 只检查玩家当前所在的方块和脚下方块
        Block blockAtFeet = player.level.getBlockState(pos.below()).getBlock();
        Block blockAtBody = player.level.getBlockState(pos).getBlock();
        return blockAtFeet == targetBlock || blockAtBody == targetBlock;
    }

    /**
     * 检查玩家脚下的方块
     */
    private void checkPlayerFootBlock(Player player, int maxIntensity, int tickCounter) {
        Block feetBlock = player.level.getBlockState(player.blockPosition().below()).getBlock();

        // 细雪 - 每1.5秒发送一次 fast_pinch 波形
        if (feetBlock == Blocks.POWDER_SNOW) {
            // 每 30 tick (1.5秒) 发送一次
            if (tickCounter % 30 == 0) {
                int intensity = (int)(10.0 * ModConfig.FREEZE_MULTIPLIER.get());
                intensity = Math.min(intensity, maxIntensity);
                WebSocketServerManager.getInstance().sendWaveformData("A", "fast_pinch", intensity);
            }
            wasInSnow = true;
        } else {
            if (wasInSnow) {
                // 离开细雪时发送停止信号
                WebSocketServerManager.getInstance().stopStimulus("A");
                wasInSnow = false;
            }
        }

        // 注：仙人掌伤害已由 DamageHandler 处理，此处不再重复发送

        // 下界传送门方块 - 检测玩家身体位置是否在传送门内部
        boolean inNetherPortal = isTouchingBlock(player, Blocks.NETHER_PORTAL);
        if (inNetherPortal) {
            if (!wasInNetherPortal) {
                wasInNetherPortal = true;
            }
            // 每 30 tick (1.5秒) 发送一次 pinch_intensify 波形
            if (tickCounter % 30 == 0) {
                int intensity = Math.min(10, maxIntensity);
                // A通道发送 pinch_intensify 波形
                WebSocketServerManager.getInstance().sendWaveformData("A", "pinch_intensify", intensity);
                // B通道同步发送 pinch_intensify 波形
                WebSocketServerManager.getInstance().sendWaveformData("B", "pinch_intensify", intensity);
            }
        } else {
            if (wasInNetherPortal) {
                // 离开下界传送门时停止波形
                WebSocketServerManager.getInstance().stopStimulus("A");
                WebSocketServerManager.getInstance().stopStimulus("B");
                wasInNetherPortal = false;
            }
        }
    }
}

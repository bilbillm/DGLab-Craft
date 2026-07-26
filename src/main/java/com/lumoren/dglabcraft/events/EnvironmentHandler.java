package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
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
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (!event.player.level.isClientSide()) return;
        if (mc.player == null) return;

        // 使用 UUID 比较
        Player eventPlayer = event.player;
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        Player player = event.player;
        handleClientEnvironment(player);
    }

    private void handleClientEnvironment(Player player) {
        tickCounter++;
        // 每 10 tick 检查一次 (减少频繁调用)
        if (tickCounter % 10 != 0) return;

        // 获取 WebSocket 管理器
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // 计算 A/B 通道实际强度上限
        int appMaxStrengthA = ws.getAppAMaxStrength();
        int appMaxStrengthB = ws.getAppBMaxStrength();
        int maxIntensityA = ModConfig.getEffectiveMaxIntensity(appMaxStrengthA);
        int maxIntensityB = ModConfig.getEffectiveMaxIntensity(appMaxStrengthB);

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
                int intensityA, intensityB;
                if (ModConfig.SYNC_CHANNELS.get()) {
                    // 同步模式：分别用 A/B 通道上限计算
                    intensityA = (int)(maxIntensityA * ModConfig.NETHER_MULTIPLIER.get() / 100.0);
                    intensityA = Math.min(intensityA, maxIntensityA);
                    intensityB = (int)(maxIntensityB * ModConfig.NETHER_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "nether", "breath", intensityA, intensityB);
                } else {
                    // 非同步模式：只用 B 通道
                    intensityB = (int)(maxIntensityB * ModConfig.NETHER_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    intensityA = 0;
                    ws.requestEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "nether", "B", "breath", intensityB);
                }
                // 更新 FadeManager
                FadeManager.updateEnvironment(intensityA, intensityB);
            }
        } else {
            if (wasInNether) {
                // 离开下界时通知 FadeManager 开始渐变
                FadeManager.stopEnvironment();
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
                int intensityA, intensityB;
                if (ModConfig.SYNC_CHANNELS.get()) {
                    // 同步模式：分别用 A/B 通道上限计算
                    intensityA = (int)(maxIntensityA * ModConfig.END_MULTIPLIER.get() / 100.0);
                    intensityA = Math.min(intensityA, maxIntensityA);
                    intensityB = (int)(maxIntensityB * ModConfig.END_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "end", "tide", intensityA, intensityB);
                } else {
                    // 非同步模式：只用 B 通道
                    intensityB = (int)(maxIntensityB * ModConfig.END_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    intensityA = 0;
                    ws.requestEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "end", "B", "tide", intensityB);
                }
                // 更新 FadeManager
                FadeManager.updateEnvironment(intensityA, intensityB);
            }
        } else {
            if (wasInEnd) {
                // 离开终界时通知 FadeManager 开始渐变
                FadeManager.stopEnvironment();
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
                    intensity = Math.min(intensity, maxIntensityA);
                    if (ModConfig.SYNC_CHANNELS.get()) {
                        ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "freeze", "fast_pinch", intensity, intensity);
                    } else {
                        ws.requestEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "freeze", "A", "fast_pinch", intensity);
                    }
                    // 细雪只用 A 通道，更新 FadeManager
                    FadeManager.updateEnvironment(intensity, 0);
                }
            } else {
                wasInCold = false;
            }
        }

        // 3. 检查玩家脚下的方块
        checkPlayerFootBlock(player, maxIntensityA, maxIntensityB, tickCounter);
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
    private void checkPlayerFootBlock(Player player, int maxIntensityA, int maxIntensityB, int tickCounter) {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        Block feetBlock = player.level.getBlockState(player.blockPosition().below()).getBlock();

        // 细雪 - 每1.5秒发送一次 fast_pinch 波形
        if (feetBlock == Blocks.POWDER_SNOW) {
            // 每 30 tick (1.5秒) 发送一次
            if (tickCounter % 30 == 0) {
                int intensity = (int)(10.0 * ModConfig.FREEZE_MULTIPLIER.get());
                intensity = Math.min(intensity, maxIntensityA);
                if (ModConfig.SYNC_CHANNELS.get()) {
                    ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "freeze", "fast_pinch", intensity, intensity);
                } else {
                    ws.requestEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "freeze", "A", "fast_pinch", intensity);
                }
                // 细雪只用 A 通道，更新 FadeManager
                FadeManager.updateEnvironment(intensity, 0);
            }
            wasInSnow = true;
        } else {
            if (wasInSnow) {
                // 离开细雪时通知 FadeManager 开始渐变
                FadeManager.stopEnvironment();
            }
            wasInSnow = false;
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
                int intensityA, intensityB;
                if (ModConfig.SYNC_CHANNELS.get()) {
                    // 同步模式：分别用 A/B 通道上限计算
                    intensityA = (int)(maxIntensityA * ModConfig.PORTAL_MULTIPLIER.get() / 100.0);
                    intensityA = Math.min(intensityA, maxIntensityA);
                    intensityB = (int)(maxIntensityB * ModConfig.PORTAL_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    ws.requestSyncedEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "portal", "pinch_intensify", intensityA, intensityB);
                } else {
                    // 非同步模式：只用 B 通道
                    intensityB = (int)(maxIntensityB * ModConfig.PORTAL_MULTIPLIER.get() / 100.0);
                    intensityB = Math.min(intensityB, maxIntensityB);
                    intensityA = 0;
                    ws.requestEffect(WebSocketServerManager.EffectSource.ENVIRONMENT, "portal", "B", "pinch_intensify", intensityB);
                }
                // 更新 FadeManager
                FadeManager.updateEnvironment(intensityA, intensityB);
            }
        } else {
            if (wasInNetherPortal) {
                // 离开下界传送门时通知 FadeManager 开始渐变
                FadeManager.stopEnvironment();
            }
            wasInNetherPortal = false;
        }
    }
}

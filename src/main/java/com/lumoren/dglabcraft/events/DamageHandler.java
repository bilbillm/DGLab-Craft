package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 伤害事件处理
 * 根据 DamageSource.getMsgId() 映射到对应的倍率配置
 */
public class DamageHandler {

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        // 只处理客户端玩家自身
        Minecraft mc = Minecraft.getInstance();

        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null || mc.level == null) return;

        // UUID 比较判断是否是本地玩家
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) {
            return;
        }

        Player player = eventPlayer;

        DamageSource source = event.getSource();
        float damage = event.getAmount();
        String msgId = source.getMsgId();

        System.out.println("[DGLabCraft] 伤害来源: " + msgId + ", 伤害值: " + damage);

        // 获取 WebSocket 管理器
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // 获取玩家最大生命值
        float maxHealth = player.getMaxHealth();

        // 获取波形
        String waveform = WaveformManager.getWaveformIdForDamage(msgId);

        // 获取倍率
        double multiplier = getMultiplierForDamage(msgId);
        float healthRatio = damage / maxHealth;

        // 同步模式：分别计算 A/B 通道强度
        if (ModConfig.SYNC_CHANNELS.get()) {
            int appMaxStrengthA = ws.getAppAMaxStrength();
            int appMaxStrengthB = ws.getAppBMaxStrength();
            int effectiveMaxA = ModConfig.getEffectiveMaxIntensity(appMaxStrengthA);
            int effectiveMaxB = ModConfig.getEffectiveMaxIntensity(appMaxStrengthB);

            int strengthA = (int)(effectiveMaxA * healthRatio * 2 * multiplier);
            strengthA = Math.max(1, Math.min(strengthA, effectiveMaxA));

            int strengthB = (int)(effectiveMaxB * healthRatio * 2 * multiplier);
            strengthB = Math.max(1, Math.min(strengthB, effectiveMaxB));

            System.out.println("[DGLabCraft] 计算强度: A=" + strengthA + "(上限" + effectiveMaxA + "), B=" + strengthB + "(上限" + effectiveMaxB + ")");
            System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());

            ws.sendWaveformDataDualChannelWithDifferentIntensity(waveform, strengthA, strengthB);
        } else {
            // 非同步模式：只用 A 通道
            int appMaxStrength = ws.getAppAMaxStrength();
            int effectiveMaxIntensity = ModConfig.getEffectiveMaxIntensity(appMaxStrength);

            int strength = (int)(effectiveMaxIntensity * healthRatio * 2 * multiplier);
            strength = Math.max(1, Math.min(strength, effectiveMaxIntensity));

            System.out.println("[DGLabCraft] 计算强度: " + strength + ", 有效上限: " + effectiveMaxIntensity + ", 通道: A");
            System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());

            ws.sendWaveformData("A", waveform, strength);
        }
    }

    /**
     * 根据伤害来源 ID 获取对应的倍率配置
     */
    private double getMultiplierForDamage(String msgId) {
        switch (msgId) {
            // 锐器与穿刺 (fast_pinch)
            case "cactus":
                return ModConfig.CACTUS_MULTIPLIER.get();
            case "sweetberrybush":
                return ModConfig.SWEETBERRY_BUSH_MULTIPLIER.get();
            case "arrow":
                return ModConfig.ARROW_MULTIPLIER.get();
            case "trident":
                return ModConfig.TRIDENT_MULTIPLIER.get();
            case "stalagmite":
                return ModConfig.STALAGMITE_MULTIPLIER.get();

            // 钝器与撞击 (beat)
            case "fall":
                return ModConfig.FALL_MULTIPLIER.get();
            case "mob":
                return ModConfig.MOB_ATTACK_MULTIPLIER.get();
            case "player":
                return ModConfig.PLAYER_ATTACK_MULTIPLIER.get();
            case "flyIntoWall":
                return ModConfig.FLY_INTO_WALL_MULTIPLIER.get();
            case "explosion":
                return ModConfig.EXPLOSION_MULTIPLIER.get();
            case "fireworks":
                return ModConfig.FIREWORKS_MULTIPLIER.get();

            // 高温与灼烧 (burn)
            case "onFire":
                return ModConfig.ON_FIRE_MULTIPLIER.get();
            case "inFire":
                return ModConfig.IN_FIRE_MULTIPLIER.get();
            case "lava":
                return ModConfig.LAVA_MULTIPLIER.get();
            case "hotFloor":
                return ModConfig.HOT_FLOOR_MULTIPLIER.get();

            // 挤压与窒息 (compress)
            case "inWall":
                return ModConfig.IN_WALL_MULTIPLIER.get();
            case "cramming":
                return ModConfig.CRAMMING_MULTIPLIER.get();
            case "fallingBlock":
                return ModConfig.FALLING_BLOCK_MULTIPLIER.get();
            case "anvil":
                return ModConfig.ANVIL_MULTIPLIER.get();

            // 环境与缺氧 (drown)
            case "drown":
                return ModConfig.DROWN_MULTIPLIER.get();
            case "freeze":
                return ModConfig.FREEZE_MULTIPLIER.get();

            // 魔法与毒素 (tide)
            case "magic":
                return ModConfig.MAGIC_MULTIPLIER.get();
            case "wither":
                return ModConfig.WITHER_MULTIPLIER.get();
            case "dragonBreath":
                return ModConfig.DRAGON_BREATH_MULTIPLIER.get();
            case "starve":
                return ModConfig.STARVE_MULTIPLIER.get();

            default:
                return 1.0;
        }
    }
}

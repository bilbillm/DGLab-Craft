package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 伤害事件处理
 * 根据 DamageSource 获取伤害类型并映射到对应的倍率配置
 */
public class DamageHandler {

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onLivingDamage(LivingDamageEvent.Post event) {
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
        float damage = event.getNewDamage();
        // 1.20.1: getMsgId() 已废弃但仍可用，返回伤害类型字符串 ID
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
        if (DGLabConfig.SYNC_CHANNELS.get()) {
            int appMaxStrengthA = ws.getAppAMaxStrength();
            int appMaxStrengthB = ws.getAppBMaxStrength();
            int effectiveMaxA = DGLabConfig.getEffectiveMaxIntensity(appMaxStrengthA);
            int effectiveMaxB = DGLabConfig.getEffectiveMaxIntensity(appMaxStrengthB);

            int strengthA = (int)(effectiveMaxA * healthRatio * 2 * multiplier);
            strengthA = Math.max(1, Math.min(strengthA, effectiveMaxA));

            int strengthB = (int)(effectiveMaxB * healthRatio * 2 * multiplier);
            strengthB = Math.max(1, Math.min(strengthB, effectiveMaxB));

            System.out.println("[DGLabCraft] 计算强度: A=" + strengthA + "(上限" + effectiveMaxA + "), B=" + strengthB + "(上限" + effectiveMaxB + ")");
            System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());

            ws.sendWaveformDataDualChannelWithDifferentIntensity(waveform, strengthA, strengthB);
            // 更新 FadeManager
            FadeManager.updateDamage(strengthA, strengthB);
        } else {
            // 非同步模式：只用 A 通道
            int appMaxStrength = ws.getAppAMaxStrength();
            int effectiveMaxIntensity = DGLabConfig.getEffectiveMaxIntensity(appMaxStrength);

            int strength = (int)(effectiveMaxIntensity * healthRatio * 2 * multiplier);
            strength = Math.max(1, Math.min(strength, effectiveMaxIntensity));

            System.out.println("[DGLabCraft] 计算强度: " + strength + ", 有效上限: " + effectiveMaxIntensity + ", 通道: A");
            System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());

            ws.sendWaveformData("A", waveform, strength);
            // 更新 FadeManager（A通道用strength，B通道用0）
            FadeManager.updateDamage(strength, 0);
        }
    }

    /**
     * 根据伤害来源 ID 获取对应的倍率配置
     */
    private double getMultiplierForDamage(String msgId) {
        switch (msgId) {
            // 锐器与穿刺 (fast_pinch)
            case "cactus":
                return DGLabConfig.CACTUS_MULTIPLIER.get();
            case "sweetberrybush":
                return DGLabConfig.SWEETBERRY_BUSH_MULTIPLIER.get();
            case "arrow":
                return DGLabConfig.ARROW_MULTIPLIER.get();
            case "trident":
                return DGLabConfig.TRIDENT_MULTIPLIER.get();
            case "stalagmite":
                return DGLabConfig.STALAGMITE_MULTIPLIER.get();

            // 钝器与撞击 (beat)
            case "fall":
                return DGLabConfig.FALL_MULTIPLIER.get();
            case "mob":
                return DGLabConfig.MOB_ATTACK_MULTIPLIER.get();
            case "player":
                return DGLabConfig.PLAYER_ATTACK_MULTIPLIER.get();
            case "flyIntoWall":
                return DGLabConfig.FLY_INTO_WALL_MULTIPLIER.get();
            case "explosion":
                return DGLabConfig.EXPLOSION_MULTIPLIER.get();
            case "fireworks":
                return DGLabConfig.FIREWORKS_MULTIPLIER.get();

            // 高温与灼烧 (burn)
            case "onFire":
                return DGLabConfig.ON_FIRE_MULTIPLIER.get();
            case "inFire":
                return DGLabConfig.IN_FIRE_MULTIPLIER.get();
            case "lava":
                return DGLabConfig.LAVA_MULTIPLIER.get();
            case "hotFloor":
                return DGLabConfig.HOT_FLOOR_MULTIPLIER.get();

            // 挤压与窒息 (compress)
            case "inWall":
                return DGLabConfig.IN_WALL_MULTIPLIER.get();
            case "cramming":
                return DGLabConfig.CRAMMING_MULTIPLIER.get();
            case "fallingBlock":
                return DGLabConfig.FALLING_BLOCK_MULTIPLIER.get();
            case "anvil":
                return DGLabConfig.ANVIL_MULTIPLIER.get();

            // 环境与缺氧 (drown)
            case "drown":
                return DGLabConfig.DROWN_MULTIPLIER.get();
            case "freeze":
                return DGLabConfig.FREEZE_MULTIPLIER.get();

            // 魔法与毒素 (tide)
            case "magic":
                return DGLabConfig.MAGIC_MULTIPLIER.get();
            case "wither":
                return DGLabConfig.WITHER_MULTIPLIER.get();
            case "dragonBreath":
                return DGLabConfig.DRAGON_BREATH_MULTIPLIER.get();
            case "starve":
                return DGLabConfig.STARVE_MULTIPLIER.get();

            default:
                return 1.0;
        }
    }
}

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
        if (mc.player == null) return;

        // UUID 比较判断是否是本地玩家
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) {
            return;
        }

        DamageSource source = event.getSource();
        float damage = event.getAmount();
        String msgId = source.getMsgId();

        System.out.println("[DGLabCraft] 伤害来源: " + msgId + ", 伤害值: " + damage);

        // 获取全局强度上限
        int maxIntensity = ModConfig.BASE_MAX_INTENSITY.get();

        // 获取波形
        String waveform = WaveformManager.getWaveformIdForDamage(msgId);

        // 根据伤害来源确定通道和倍率
        String channel = "A";
        double multiplier = getMultiplierForDamage(msgId);

        // 计算强度: max(1, damage * multiplier * 10)
        int strength = Math.max(1, (int)(damage * multiplier * 10));
        strength = Math.min(strength, maxIntensity);

        System.out.println("[DGLabCraft] 计算强度: " + strength + ", 通道: " + channel);

        // 发送刺激
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());
        ws.sendWaveformData(channel, waveform, strength);
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

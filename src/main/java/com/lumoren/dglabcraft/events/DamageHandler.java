package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 伤害事件处理
 * 参考 CaiJi-ikun/DG_LAB 算法实现
 *
 * 强度计算公式: strength = max(1, damage * multiplier * 10)
 * 数据包格式: strength-<channel>+<mode>+<value>
 */
public class DamageHandler {

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        // 只处理客户端玩家自身 - 使用 UUID 比较
        Minecraft mc = Minecraft.getInstance();

        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        // 使用 UUID 比较来判断是否是同一个玩家
        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) {
            return;  // 不是本地玩家
        }

        Player player = (Player) event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();

        System.out.println("[DGLabCraft] 伤害来源: " + source.getMsgId() + ", 伤害值: " + damage);

        // 获取全局强度上限
        int maxIntensity = ModConfig.BASE_MAX_INTENSITY.get();

        // 根据伤害来源确定通道和波形
        String channel;
        String waveform;
        double multiplier;

        if (source.isFire()) {
            // 火焰/岩浆 - A通道, 持续波形
            channel = "A";
            waveform = "pulse";
            multiplier = ModConfig.FIRE_INTENSITY.get();
        }
        else if (source == DamageSource.FALL) {
            // 跌落 - A通道, 重击波形
            channel = "A";
            waveform = "square";
            multiplier = ModConfig.FALL_INTENSITY.get();
        }
        else if (source == DamageSource.DROWN) {
            // 溺水 - B通道, 压迫波形
            channel = "B";
            waveform = "sine";
            multiplier = ModConfig.DROWN_INTENSITY.get();
        }
        else if (source == DamageSource.WITHER) {
            // 凋零 - B通道, 间歇波形
            channel = "B";
            waveform = "pulse";
            multiplier = ModConfig.WITHER_INTENSITY.get();
        }
        else if (source.getMsgId().contains("poison")) {
            // 中毒 - B通道, 间歇波形
            channel = "B";
            waveform = "pulse";
            multiplier = ModConfig.POISON_INTENSITY.get();
        }
        else {
            // 其他伤害类型，默认A通道
            channel = "A";
            waveform = "pulse";
            multiplier = 1.0;
        }

        // 计算强度: max(1, damage * multiplier * 10)
        int strength = Math.max(1, (int)(damage * multiplier * 10));

        // 应用全局上限
        strength = Math.min(strength, maxIntensity);

        System.out.println("[DGLabCraft] 计算强度: " + strength + ", 通道: " + channel);

        // 发送刺激 - 使用新的数据驱动波形系统
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());
        ws.sendWaveformData(channel, waveform, strength);
    }

    /**
     * 监听玩家持续状态（燃烧、溺水等）
     */
    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null || event.getEntity() != mc.player) return;

        Player player = (Player) event.getEntity();
        int maxIntensity = ModConfig.BASE_MAX_INTENSITY.get();

        // 检查是否在火中
        if (player.isOnFire()) {
            // 火焰持续伤害 - 较低强度
            int strength = (int)(5.0 * ModConfig.FIRE_INTENSITY.get());
            strength = Math.min(strength, maxIntensity);
            WebSocketServerManager.getInstance().sendWaveformData("A", "burn", strength);
        }

        // 检查是否在水中且空气不足
        if (player.isInWater() && player.getAirSupply() < 100) {
            int strength = (int)(8.0 * ModConfig.DROWN_INTENSITY.get());
            strength = Math.min(strength, maxIntensity);
            WebSocketServerManager.getInstance().sendWaveformData("B", "drown", strength);
        }
    }
}

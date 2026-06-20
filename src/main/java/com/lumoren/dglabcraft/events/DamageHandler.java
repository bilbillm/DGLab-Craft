package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 伤害事件处理
 * 根据 DamageSource 获取伤害类型并映射到对应的倍率配置
 */
public class DamageHandler {

    private static final float MIN_DAMAGE_DELTA = 0.01f;
    private static final int DAMAGE_DEBOUNCE_TICKS = 6;
    private static final int EVENT_SOURCE_MAX_AGE_TICKS = 40;

    private float lastHealth = -1.0f;
    private int tickCounter = 0;
    private int lastDamageTriggerTick = -DAMAGE_DEBOUNCE_TICKS;
    private String lastDamageSourceId;
    private int lastDamageSourceTick = Integer.MIN_VALUE;
    private String pendingFallingBlockSource;
    private int pendingFallingBlockTick = Integer.MIN_VALUE;

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onLivingDamage(LivingDamageEvent.Post event) {
        cacheDamageSource(event);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null || mc.level == null) return;

        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        tickCounter++;
        updatePendingFallingBlockSource(eventPlayer, mc);

        Player player = eventPlayer;
        float currentHealth = player.getHealth();

        if (lastHealth < 0.0f) {
            lastHealth = currentHealth;
            return;
        }

        float healthDelta = lastHealth - currentHealth;
        lastHealth = currentHealth;

        if (healthDelta <= MIN_DAMAGE_DELTA) {
            expireStaleDamageSource();
            return;
        }

        if (tickCounter - lastDamageTriggerTick < DAMAGE_DEBOUNCE_TICKS) {
            expireStaleDamageSource();
            return;
        }

        lastDamageTriggerTick = tickCounter;
        triggerDamageFeedback(player, healthDelta, mc);
    }

    @SuppressWarnings("deprecation")
    private void cacheDamageSource(LivingDamageEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player)) return;
        if (mc.player == null) return;

        Player eventPlayer = (Player) event.getEntity();
        if (!eventPlayer.getUUID().equals(mc.player.getUUID())) return;

        DamageSource source = event.getSource();
        if (source == null) return;

        String normalizedSource = normalizeDamageSourceId(source.getMsgId());
        String pendingSource = peekPendingFallingBlockSource();

        if (pendingSource != null && shouldOverrideWithPendingSource(normalizedSource)) {
            lastDamageSourceId = pendingSource;
        } else {
            lastDamageSourceId = source.getMsgId();
        }
        lastDamageSourceTick = tickCounter;
        System.out.println("[DGLabCraft] 原始伤害事件 source.getMsgId(): " + lastDamageSourceId);
    }

    private void triggerDamageFeedback(Player player, float damage, Minecraft mc) {
        String msgId = normalizeDamageSourceId(resolveDamageSourceId(player, mc));
        String waveform = WaveformManager.getWaveformIdForDamage(msgId);
        if ("default".equals(waveform)) {
            waveform = "beat";
        }

        System.out.println("[DGLabCraft] 伤害来源: " + msgId + ", 伤害值: " + damage);

        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        float maxHealth = Math.max(player.getMaxHealth(), 1.0f);
        double multiplier = getMultiplierForDamage(msgId);
        float healthRatio = damage / maxHealth;

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

            ws.requestSyncedEffect(WebSocketServerManager.EffectSource.DAMAGE, msgId, waveform, strengthA, strengthB);
            FadeManager.updateDamage(strengthA, strengthB);
        } else {
            int appMaxStrength = ws.getAppAMaxStrength();
            int effectiveMaxIntensity = DGLabConfig.getEffectiveMaxIntensity(appMaxStrength);

            int strength = (int)(effectiveMaxIntensity * healthRatio * 2 * multiplier);
            strength = Math.max(1, Math.min(strength, effectiveMaxIntensity));

            System.out.println("[DGLabCraft] 计算强度: " + strength + ", 有效上限: " + effectiveMaxIntensity + ", 通道: A");
            System.out.println("[DGLabCraft] WebSocket已连接: " + ws.isConnected());

            ws.requestEffect(WebSocketServerManager.EffectSource.DAMAGE, msgId, "A", waveform, strength);
            FadeManager.updateDamage(strength, 0);
        }
    }

    private String resolveDamageSourceId(Player player, Minecraft mc) {
        String cachedSource = peekRecentDamageSource();
        if (cachedSource != null && !cachedSource.isEmpty()) {
            return cachedSource;
        }

        if (player.isInLava()) {
            return "lava";
        }
        if (isOnHotFloor(player, mc)) {
            return "hotFloor";
        }
        if (player.isOnFire()) {
            return "onFire";
        }
        if (player.isFreezing()) {
            return "freeze";
        }
        if (player.getAirSupply() <= 0) {
            return "drown";
        }
        String positionalOrEntitySource = getPositionalOrEntityDamageSource(player, mc);
        if (positionalOrEntitySource != null) {
            return positionalOrEntitySource;
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            return "wither";
        }
        if (player.getFoodData().getFoodLevel() <= 0) {
            return "starve";
        }
        if (player.hasEffect(MobEffects.POISON) || player.hasEffect(MobEffects.HARM)) {
            return "magic";
        }
        if (player.fallDistance > 3.0f) {
            return "fall";
        }

        String blockDamageSource = getTouchingBlockDamageSource(player, mc);
        if (blockDamageSource != null) {
            return blockDamageSource;
        }

        return "mob";
    }

    String normalizeDamageSourceId(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            return "mob";
        }

        String id = rawId.trim();
        String lower = id.toLowerCase();

        if (lower.contains(".")) {
            lower = lower.split("\\.")[0];
        }

        return switch (lower) {
            case "sweet_berry_bush" -> "sweetberrybush";
            case "hot_floor" -> "hotFloor";
            case "in_wall" -> "inWall";
            case "falling_block" -> "fallingBlock";
            case "dragon_breath" -> "dragonBreath";
            case "fly_into_wall" -> "flyIntoWall";
            case "mob_attack", "mobattack" -> "mob";
            case "player_attack", "playerattack" -> "player";
            case "indirect_magic" -> "magic";
            case "onfire" -> "onFire";
            case "infire" -> "inFire";
            case "hotfloor" -> "hotFloor";
            case "inwall" -> "inWall";
            case "fallingblock" -> "fallingBlock";
            case "dragonbreath" -> "dragonBreath";
            case "flyintowall" -> "flyIntoWall";
            default -> lower;
        };
    }

    private String peekRecentDamageSource() {
        if (lastDamageSourceId == null) {
            return null;
        }
        if (tickCounter - lastDamageSourceTick > EVENT_SOURCE_MAX_AGE_TICKS) {
            lastDamageSourceId = null;
            return null;
        }

        return lastDamageSourceId;
    }

    private void expireStaleDamageSource() {
        if (lastDamageSourceId != null && tickCounter - lastDamageSourceTick > EVENT_SOURCE_MAX_AGE_TICKS) {
            lastDamageSourceId = null;
        }

        if (pendingFallingBlockSource != null && tickCounter - pendingFallingBlockTick > EVENT_SOURCE_MAX_AGE_TICKS) {
            pendingFallingBlockSource = null;
        }
    }

    private String getTouchingBlockDamageSource(Player player, Minecraft mc) {
        if (mc.level == null) {
            return null;
        }

        BlockPos pos = player.blockPosition();
        Block blockAtFeet = mc.level.getBlockState(pos.below()).getBlock();
        Block blockAtBody = mc.level.getBlockState(pos).getBlock();
        Block blockAtHead = mc.level.getBlockState(pos.above()).getBlock();

        if (blockAtFeet == Blocks.CACTUS || blockAtBody == Blocks.CACTUS || blockAtHead == Blocks.CACTUS) {
            return "cactus";
        }
        if (blockAtFeet == Blocks.SWEET_BERRY_BUSH || blockAtBody == Blocks.SWEET_BERRY_BUSH || blockAtHead == Blocks.SWEET_BERRY_BUSH) {
            return "sweetberrybush";
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Block adjacentBlock = mc.level.getBlockState(pos.relative(direction)).getBlock();
            Block adjacentUpperBlock = mc.level.getBlockState(pos.above().relative(direction)).getBlock();
            if (adjacentBlock == Blocks.CACTUS || adjacentUpperBlock == Blocks.CACTUS) {
                return "cactus";
            }
            if (adjacentBlock == Blocks.SWEET_BERRY_BUSH || adjacentUpperBlock == Blocks.SWEET_BERRY_BUSH) {
                return "sweetberrybush";
            }
        }

        return null;
    }

    private String getPositionalOrEntityDamageSource(Player player, Minecraft mc) {
        if (mc.level == null) {
            return null;
        }

        BlockPos pos = player.blockPosition();
        Block blockAtFeet = mc.level.getBlockState(pos.below()).getBlock();
        Block blockAtBody = mc.level.getBlockState(pos).getBlock();

        if (blockAtFeet == Blocks.POINTED_DRIPSTONE || blockAtBody == Blocks.POINTED_DRIPSTONE) {
            return "stalagmite";
        }

        if (player.isInWall()) {
            return "inWall";
        }

        // NeoForge 1.21.4: ClientLevel 不再提供 getGameRules()，回退至单机服务器或使用默认值 24
        int maxCramming = 24;
        try {
            if (mc.getSingleplayerServer() != null) {
                maxCramming = mc.getSingleplayerServer().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            }
        } catch (Exception ignored) {
            // fall through to default
        }
        if (maxCramming > 0) {
            java.util.List<Entity> nearbyEntities = mc.level.getEntities(player, player.getBoundingBox().inflate(0.2D));
            if (nearbyEntities.size() >= maxCramming) {
                return "cramming";
            }
        }

        java.util.List<FallingBlockEntity> fallingBlocks = mc.level.getEntitiesOfClass(FallingBlockEntity.class, player.getBoundingBox().inflate(1.0D, 2.0D, 1.0D));
        for (FallingBlockEntity entity : fallingBlocks) {
            Block fallingBlock = entity.getBlockState().getBlock();
            if (fallingBlock == Blocks.ANVIL || fallingBlock == Blocks.CHIPPED_ANVIL || fallingBlock == Blocks.DAMAGED_ANVIL) {
                return "anvil";
            }
            return "fallingBlock";
        }

        return null;
    }

    private void updatePendingFallingBlockSource(Player player, Minecraft mc) {
        if (mc.level == null) {
            return;
        }

        java.util.List<FallingBlockEntity> fallingBlocks = mc.level.getEntitiesOfClass(FallingBlockEntity.class, player.getBoundingBox().inflate(1.0D, 2.0D, 1.0D));
        for (FallingBlockEntity entity : fallingBlocks) {
            Block fallingBlock = entity.getBlockState().getBlock();
            pendingFallingBlockSource = isAnvilBlock(fallingBlock) ? "anvil" : "fallingBlock";
            pendingFallingBlockTick = tickCounter;
            return;
        }
    }

    private String peekPendingFallingBlockSource() {
        if (pendingFallingBlockSource == null) {
            return null;
        }
        if (tickCounter - pendingFallingBlockTick > EVENT_SOURCE_MAX_AGE_TICKS) {
            pendingFallingBlockSource = null;
            return null;
        }
        return pendingFallingBlockSource;
    }

    private boolean shouldOverrideWithPendingSource(String normalizedSource) {
        return normalizedSource == null
                || normalizedSource.isEmpty()
                || "mob".equals(normalizedSource)
                || "player".equals(normalizedSource)
                || "fallingBlock".equals(normalizedSource)
                || "anvil".equals(normalizedSource);
    }

    private boolean isAnvilBlock(Block block) {
        return block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private boolean isOnHotFloor(Player player, Minecraft mc) {
        if (mc.level == null) {
            return false;
        }

        BlockPos pos = player.blockPosition();
        Block blockAtFeet = mc.level.getBlockState(pos.below()).getBlock();
        return blockAtFeet == Blocks.MAGMA_BLOCK;
    }

    /**
     * 根据伤害来源 ID 获取对应的倍率配置
     */
    private double getMultiplierForDamage(String msgId) {
        msgId = normalizeDamageSourceId(msgId);

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

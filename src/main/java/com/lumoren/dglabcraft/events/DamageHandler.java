package com.lumoren.dglabcraft.events;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class DamageHandler {
    private final LegacyDamageHistory history = new LegacyDamageHistory();
    private volatile SourceHint sourceHint;
    private float previousFallDistance;
    private static final class SourceHint {
        final String id; final long at;
        SourceHint(String id) { this.id = id; this.at = System.nanoTime(); }
    }
    @SubscribeEvent public void cacheSource(LivingHurtEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(event.entityLiving instanceof EntityPlayer) || mc.thePlayer == null) return;
        if (!((EntityPlayer) event.entityLiving).getUniqueID().equals(mc.thePlayer.getUniqueID())) return;
        DamageSource source = event.source;
        if (source != null) sourceHint = new SourceHint(source.getDamageType());
    }
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            history.sample(null, null, 0); sourceHint = null; previousFallDistance = 0; return;
        }
        if (!history.sameSession(player, mc.theWorld)) { sourceHint = null; previousFallDistance = 0; }
        float damage = history.sample(player, mc.theWorld, player.getHealth());
        if (damage > 0) {
            SourceHint hint = sourceHint;
            String id = hint != null && System.nanoTime() - hint.at < 2000000000L ? hint.id : inferSource(player, mc);
            sourceHint = null;
            send(player, damage, normalizeDamageType(id));
        }
        previousFallDistance = player.fallDistance;
    }
    private String inferSource(EntityPlayer player, Minecraft mc) {
        int x = (int) Math.floor(player.posX), y = (int) Math.floor(player.posY), z = (int) Math.floor(player.posZ);
        net.minecraft.block.Block block = mc.theWorld.getBlockState(new net.minecraft.util.BlockPos(x, y, z)).getBlock();
        if (block == Blocks.cactus) return "cactus";
        if (player.isInLava()) return "lava";
        if (block == Blocks.fire) return "inFire";
        if (player.isBurning()) return "onFire";
        if (player.getAir() <= 0) return "drown";
        if (player.isEntityInsideOpaqueBlock()) return "inWall";
        if (player.isPotionActive(net.minecraft.potion.Potion.poison)) return "magic";
        if (player.isPotionActive(net.minecraft.potion.Potion.wither)) return "wither";
        if (player.getFoodStats().getFoodLevel() == 0) return "starve";
        if (previousFallDistance > 3 || player.fallDistance > 3) return "fall";
        return "mob";
    }
    private void send(EntityPlayer player, float damage, String id) {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        double ratio = damage / Math.max(1.0F, player.getMaxHealth()) * 2 * getMultiplierForDamage(id);
        String waveform = WaveformManager.getWaveformIdForDamage(id);
        int capA = ModConfig.getEffectiveMaxIntensity(ws.getAppAMaxStrength());
        int capB = ModConfig.getEffectiveMaxIntensity(ws.getAppBMaxStrength());
        int a = ratio <= 0 || capA <= 0 ? 0 : Math.min(capA, Math.max(1, (int) (capA * ratio)));
        int b = ratio <= 0 || capB <= 0 ? 0 : Math.min(capB, Math.max(1, (int) (capB * ratio)));
        if (ModConfig.SYNC_CHANNELS.get()) ws.requestSyncedEffect(WebSocketServerManager.EffectSource.DAMAGE, id, waveform, a, b);
        else ws.requestEffect(WebSocketServerManager.EffectSource.DAMAGE, id, "A", waveform, a);
        FadeManager.updateDamage(a, ModConfig.SYNC_CHANNELS.get() ? b : 0);
    }
    private static String normalizeDamageType(String type) {
        if (type == null) {
            return "default";
        }
        return type.replace(".", "").replace("_", "").toLowerCase();
    }

    private double getMultiplierForDamage(String msgId) {
        if ("cactus".equals(msgId)) return ModConfig.CACTUS_MULTIPLIER.get();
        if ("arrow".equals(msgId)) return ModConfig.ARROW_MULTIPLIER.get();
        if ("fall".equals(msgId)) return ModConfig.FALL_MULTIPLIER.get();
        if ("mob".equals(msgId)) return ModConfig.MOB_ATTACK_MULTIPLIER.get();
        if ("player".equals(msgId)) return ModConfig.PLAYER_ATTACK_MULTIPLIER.get();
        if ("flyintowall".equals(msgId)) return ModConfig.FLY_INTO_WALL_MULTIPLIER.get();
        if ("explosion".equals(msgId) || "explosionplayer".equals(msgId)) return ModConfig.EXPLOSION_MULTIPLIER.get();
        if ("fireworks".equals(msgId)) return ModConfig.FIREWORKS_MULTIPLIER.get();
        if ("onfire".equals(msgId)) return ModConfig.ON_FIRE_MULTIPLIER.get();
        if ("infire".equals(msgId)) return ModConfig.IN_FIRE_MULTIPLIER.get();
        if ("lava".equals(msgId)) return ModConfig.LAVA_MULTIPLIER.get();
        if ("hotfloor".equals(msgId)) return ModConfig.HOT_FLOOR_MULTIPLIER.get();
        if ("inwall".equals(msgId)) return ModConfig.IN_WALL_MULTIPLIER.get();
        if ("cramming".equals(msgId)) return ModConfig.CRAMMING_MULTIPLIER.get();
        if ("fallingblock".equals(msgId)) return ModConfig.FALLING_BLOCK_MULTIPLIER.get();
        if ("anvil".equals(msgId)) return ModConfig.ANVIL_MULTIPLIER.get();
        if ("drown".equals(msgId)) return ModConfig.DROWN_MULTIPLIER.get();
        if ("magic".equals(msgId) || "indirectmagic".equals(msgId)) return ModConfig.MAGIC_MULTIPLIER.get();
        if ("wither".equals(msgId)) return ModConfig.WITHER_MULTIPLIER.get();
        if ("dragonbreath".equals(msgId)) return ModConfig.DRAGON_BREATH_MULTIPLIER.get();
        if ("starve".equals(msgId)) return ModConfig.STARVE_MULTIPLIER.get();
        return 1.0D;
    }

}

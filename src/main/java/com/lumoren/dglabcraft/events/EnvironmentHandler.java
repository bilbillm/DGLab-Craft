package com.lumoren.dglabcraft.events;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EnvironmentHandler {
    private int tickCounter = 0;
    private boolean wasInNether = false;
    private boolean wasInEnd = false;
    private boolean wasInPortal = false;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.player.world.isRemote) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !event.player.getUniqueID().equals(mc.player.getUniqueID())) {
            return;
        }
        handleClientEnvironment(event.player);
    }

    private void handleClientEnvironment(EntityPlayer player) {
        tickCounter++;
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        int maxIntensityA = ModConfig.getEffectiveMaxIntensity(ws.getAppAMaxStrength());
        int maxIntensityB = ModConfig.getEffectiveMaxIntensity(ws.getAppBMaxStrength());

        boolean inNether = player.dimension == -1;
        if (inNether && tickCounter % 60 == 0) {
            sendEnvironment(ws, "breath", maxIntensityA, maxIntensityB, ModConfig.NETHER_MULTIPLIER.get(), "nether");
        } else if (wasInNether && !inNether) {
            FadeManager.stopEnvironment();
        }
        wasInNether = inNether;

        boolean inEnd = player.dimension == 1;
        if (inEnd && tickCounter % 60 == 0) {
            sendEnvironment(ws, "tide", maxIntensityA, maxIntensityB, ModConfig.END_MULTIPLIER.get(), "end");
        } else if (wasInEnd && !inEnd) {
            FadeManager.stopEnvironment();
        }
        wasInEnd = inEnd;

        BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
        boolean inPortal = player.world.getBlockState(pos).getBlock() == Blocks.PORTAL;
        if (inPortal && tickCounter % 30 == 0) {
            sendEnvironment(ws, "pinch_intensify", maxIntensityA, maxIntensityB, ModConfig.PORTAL_MULTIPLIER.get(), "portal");
        } else if (wasInPortal && !inPortal) {
            FadeManager.stopEnvironment();
        }
        wasInPortal = inPortal;
    }

    private void sendEnvironment(WebSocketServerManager ws, String waveform, int maxA, int maxB, double percent, String detail) {
        int intensityA = Math.max(1, Math.min(maxA, (int) (maxA * percent / 100.0D)));
        int intensityB = Math.max(1, Math.min(maxB, (int) (maxB * percent / 100.0D)));
        if (ModConfig.SYNC_CHANNELS.get()) {
            ws.sendWaveformDataDualChannelWithDifferentIntensity(waveform, intensityA, intensityB);
            ws.noteSyncRuntime(WebSocketServerManager.EffectSource.ENVIRONMENT, detail, waveform, Math.max(intensityA, intensityB));
            FadeManager.updateEnvironment(intensityA, intensityB);
        } else {
            ws.sendWaveformData("B", waveform, intensityB);
            ws.noteChannelRuntime("B", WebSocketServerManager.EffectSource.ENVIRONMENT, detail, waveform, intensityB);
            FadeManager.updateEnvironment(0, intensityB);
        }
    }
}

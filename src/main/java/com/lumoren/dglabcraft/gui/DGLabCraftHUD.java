package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.ClientModEvents;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = "dglabcraft", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DGLabCraftHUD {
    private static final int PANEL_BG = 0xCC101014;
    private static final int PANEL_BORDER = 0xAAE8D57A;
    private static final int TEXT = 0xFFEDE6C8;
    private static final int MUTED = 0xFF9E9E9E;
    private static final int GREEN = 0xFF55FF88;
    private static final int YELLOW = 0xFFE8D57A;
    private static final int RED = 0xFFFF7777;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null) return;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        PoseStack poseStack = event.getPoseStack();
        WebSocketServerManager server = WebSocketServerManager.getInstance();

        if (ModConfig.HUD_ENABLED.get()) {
            renderHudPanel(poseStack, mc.font, server, hudRect(screenWidth, screenHeight), false);
        }

        if (ModConfig.WAVEFORM_OVERLAY_ENABLED.get()) {
            renderWaveformPanel(poseStack, mc.font, server, waveformRect(screenWidth, screenHeight), false);
        }
    }

    static OverlayLayout.Rect hudRect(int screenWidth, int screenHeight) {
        if (!OverlayLayout.hasSavedRatio(ModConfig.HUD_X_RATIO.get(), ModConfig.HUD_Y_RATIO.get())) {
            return OverlayLayout.defaultHudRect(ModConfig.HUD_POSITION.get(), screenWidth, screenHeight);
        }
        return OverlayLayout.rectFromRatio(ModConfig.HUD_X_RATIO.get(), ModConfig.HUD_Y_RATIO.get(),
            OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, screenWidth, screenHeight);
    }

    static OverlayLayout.Rect waveformRect(int screenWidth, int screenHeight) {
        return OverlayLayout.rectFromRatio(ModConfig.WAVEFORM_X_RATIO.get(), ModConfig.WAVEFORM_Y_RATIO.get(),
            OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, screenWidth, screenHeight);
    }

    static void renderHudPanel(PoseStack poseStack, Font font, WebSocketServerManager server,
                               OverlayLayout.Rect rect, boolean editing) {
        drawPanel(poseStack, rect, editing);
        int x = rect.x() + 8;
        int y = rect.y() + 6;
        font.draw(poseStack, Component.translatable("overlay.dglabcraft.hud_title"), x, y, YELLOW);

        if (!server.isConnected()) {
            font.draw(poseStack, Component.translatable("overlay.dglabcraft.disconnected"), x, y + 14, RED);
            String keyName = ClientModEvents.OPEN_SETTINGS_KEY.get().getKey().getDisplayName().getString();
            font.draw(poseStack, Component.translatable("overlay.dglabcraft.open_settings_hint", keyName), x, y + 28, MUTED);
            return;
        }

        font.draw(poseStack, Component.translatable("overlay.dglabcraft.connected"), x, y + 14, GREEN);
        font.draw(poseStack, Component.translatable("overlay.dglabcraft.channel_state",
            "A", (int) server.getChannelAIntensity(), server.getChannelAStatus()), x, y + 28, TEXT);
        font.draw(poseStack, Component.translatable("overlay.dglabcraft.channel_state",
            "B", (int) server.getChannelBIntensity(), server.getChannelBStatus()), x, y + 40, TEXT);
    }

    static void renderWaveformPanel(PoseStack poseStack, Font font, WebSocketServerManager server,
                                    OverlayLayout.Rect rect, boolean editing) {
        drawPanel(poseStack, rect, editing);
        int x = rect.x() + 8;
        int y = rect.y() + 6;
        font.draw(poseStack, Component.translatable("overlay.dglabcraft.waveform_title"), x, y, YELLOW);

        ChannelPreview channelA = channelPreview(server, "A");
        ChannelPreview channelB = channelPreview(server, "B");
        renderChannelWaveform(poseStack, font, channelA, rect.x() + 8, rect.y() + 22, rect.width() - 16, "A");
        renderChannelWaveform(poseStack, font, channelB, rect.x() + 8, rect.y() + 52, rect.width() - 16, "B");
    }

    private static void renderChannelWaveform(PoseStack poseStack, Font font, ChannelPreview preview,
                                              int x, int y, int width, String channel) {
        font.draw(poseStack, Component.translatable("overlay.dglabcraft.channel_label", channel), x, y, TEXT);
        int waveX = x + 18;
        int waveY = y + 2;
        int waveWidth = width - 18;
        int waveHeight = 18;
        GuiComponent.fill(poseStack, waveX, waveY, waveX + waveWidth, waveY + waveHeight, 0xAA050506);

        if (!preview.active()) {
            font.draw(poseStack, Component.translatable("overlay.dglabcraft.idle"), waveX + 4, y, MUTED);
            return;
        }

        List<Integer> samples = preview.samples();
        if (samples.isEmpty()) {
            font.draw(poseStack, Component.translatable("overlay.dglabcraft.no_waveform"), waveX + 4, y, MUTED);
            return;
        }

        int sampleCount = Math.min(samples.size(), waveWidth);
        for (int i = 0; i < sampleCount; i++) {
            int amplitude = samples.get(i);
            int barHeight = Math.max(1, amplitude * waveHeight / 100);
            int barX = waveX + i * waveWidth / sampleCount;
            int nextX = waveX + (i + 1) * waveWidth / sampleCount;
            GuiComponent.fill(poseStack, barX, waveY + waveHeight - barHeight, Math.max(barX + 1, nextX), waveY + waveHeight, YELLOW);
        }

        Component detail = Component.translatable("overlay.dglabcraft.waveform_detail",
            preview.waveform(), formatSeconds(preview.remainingMillis()));
        font.draw(poseStack, detail, waveX + 4, y, TEXT);
    }

    private static ChannelPreview channelPreview(WebSocketServerManager server, String channel) {
        if (!server.isConnected()) {
            return ChannelPreview.idle();
        }

        if (server.isSyncRuntimeActive()) {
            String waveform = server.getSyncRuntimeWaveform();
            return ChannelPreview.active(waveform, server.getSyncRuntimeRemainingMillis(), samplesFor(waveform));
        }

        if (server.isChannelRuntimeActive(channel)) {
            String waveform = server.getChannelRuntimeWaveform(channel);
            return ChannelPreview.active(waveform, server.getChannelRuntimeRemainingMillis(channel), samplesFor(waveform));
        }

        return ChannelPreview.idle();
    }

    private static List<Integer> samplesFor(String waveform) {
        if (waveform == null || waveform.isBlank()) {
            return Collections.emptyList();
        }
        return WaveformPreview.amplitudes(WaveformManager.getInstance().getWaveform(waveform), 96);
    }

    private static void drawPanel(PoseStack poseStack, OverlayLayout.Rect rect, boolean editing) {
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BG);
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 1, PANEL_BORDER);
        GuiComponent.fill(poseStack, rect.x(), rect.y() + rect.height() - 1, rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.x() + 1, rect.y() + rect.height(), PANEL_BORDER);
        GuiComponent.fill(poseStack, rect.x() + rect.width() - 1, rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        if (editing) {
            GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 12, 0x44E8D57A);
        }
    }

    private static String formatSeconds(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        return Component.translatable("duration.dglabcraft.seconds", seconds).getString();
    }

    private record ChannelPreview(boolean active, String waveform, long remainingMillis, List<Integer> samples) {
        static ChannelPreview idle() {
            return new ChannelPreview(false, "", 0L, Collections.emptyList());
        }

        static ChannelPreview active(String waveform, long remainingMillis, List<Integer> samples) {
            return new ChannelPreview(true, waveform == null || waveform.isBlank() ? "default" : waveform, remainingMillis, samples);
        }
    }
}

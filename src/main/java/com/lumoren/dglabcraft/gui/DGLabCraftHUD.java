package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.ClientModEvents;
import com.lumoren.dglabcraft.config.DGLabConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = "dglabcraft", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DGLabCraftHUD {
    private static final int PANEL_BG = 0xCC101014;
    private static final int PANEL_BORDER = 0xAAE8D57A;
    private static final int TEXT = 0xFFEDE6C8;
    private static final int MUTED = 0xFF9E9E9E;
    private static final int GREEN = 0xFF55FF88;
    private static final int YELLOW = 0xFFE8D57A;
    private static final int RED = 0xFFFF7777;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        WebSocketServerManager server = WebSocketServerManager.getInstance();

        if (DGLabConfig.HUD_ENABLED.get()) {
            renderHudPanel(guiGraphics, mc.font, server, hudRect(screenWidth, screenHeight), false);
        }

        if (DGLabConfig.WAVEFORM_OVERLAY_ENABLED.get()) {
            renderWaveformPanel(guiGraphics, mc.font, server, waveformRect(screenWidth, screenHeight), false);
        }
    }

    static OverlayLayout.Rect hudRect(int screenWidth, int screenHeight) {
        if (!OverlayLayout.hasSavedRatio(DGLabConfig.HUD_X_RATIO.get(), DGLabConfig.HUD_Y_RATIO.get())) {
            OverlayLayout.Rect rect = OverlayLayout.defaultHudRect(DGLabConfig.HUD_POSITION.get(), screenWidth, screenHeight);
            double scale = DGLabConfig.HUD_SCALE.get();
            if (Math.abs(scale - 1.0D) < 0.0001D) {
                return rect;
            }
            return OverlayLayout.scaledRectFromRatio(
                OverlayLayout.ratioFromPixel(rect.x(), rect.width(), screenWidth),
                OverlayLayout.ratioFromPixel(rect.y(), rect.height(), screenHeight),
                OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, scale, screenWidth, screenHeight);
        }
        return OverlayLayout.scaledRectFromRatio(DGLabConfig.HUD_X_RATIO.get(), DGLabConfig.HUD_Y_RATIO.get(),
            OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, DGLabConfig.HUD_SCALE.get(), screenWidth, screenHeight);
    }

    static OverlayLayout.Rect waveformRect(int screenWidth, int screenHeight) {
        return OverlayLayout.scaledRectFromRatio(DGLabConfig.WAVEFORM_X_RATIO.get(), DGLabConfig.WAVEFORM_Y_RATIO.get(),
            OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, DGLabConfig.WAVEFORM_SCALE.get(), screenWidth, screenHeight);
    }

    static void renderHudPanel(GuiGraphics guiGraphics, Font font, WebSocketServerManager server,
                               OverlayLayout.Rect rect, boolean editing) {
        drawPanel(guiGraphics, rect, editing);
        renderScaled(guiGraphics, rect, OverlayLayout.HUD_WIDTH, () -> {
            int x = 8;
            int y = 6;
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.hud_title"), x, y, YELLOW, false);

            if (!server.isConnected()) {
                guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.disconnected"), x, y + 14, RED, false);
                String keyName = ClientModEvents.OPEN_SETTINGS_KEY.get().getKey().getDisplayName().getString();
                guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.open_settings_hint", keyName), x, y + 28, MUTED, false);
                return;
            }

            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.connected"), x, y + 14, GREEN, false);
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.channel_state",
                "A", (int) server.getChannelAIntensity(), server.getChannelAStatus()), x, y + 28, TEXT, false);
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.channel_state",
                "B", (int) server.getChannelBIntensity(), server.getChannelBStatus()), x, y + 40, TEXT, false);
        });
    }

    static void renderWaveformPanel(GuiGraphics guiGraphics, Font font, WebSocketServerManager server,
                                    OverlayLayout.Rect rect, boolean editing) {
        drawPanel(guiGraphics, rect, editing);
        renderScaled(guiGraphics, rect, OverlayLayout.WAVEFORM_WIDTH, () -> {
            int x = 8;
            int y = 6;
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.waveform_title"), x, y, YELLOW, false);

            ChannelPreview channelA = channelPreview(server, "A");
            ChannelPreview channelB = channelPreview(server, "B");
            renderChannelWaveform(guiGraphics, font, channelA, 8, 22, OverlayLayout.WAVEFORM_WIDTH - 16, "A");
            renderChannelWaveform(guiGraphics, font, channelB, 8, 52, OverlayLayout.WAVEFORM_WIDTH - 16, "B");
        });
    }

    private static void renderChannelWaveform(GuiGraphics guiGraphics, Font font, ChannelPreview preview,
                                              int x, int y, int width, String channel) {
        guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.channel_label", channel), x, y, TEXT, false);
        int waveX = x + 18;
        int waveY = y + 2;
        int waveWidth = width - 18;
        int waveHeight = 18;
        guiGraphics.fill(waveX, waveY, waveX + waveWidth, waveY + waveHeight, 0xAA050506);

        if (!preview.active()) {
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.idle"), waveX + 4, y, MUTED, false);
            return;
        }

        List<Integer> samples = preview.samples();
        if (samples.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("overlay.dglabcraft.no_waveform"), waveX + 4, y, MUTED, false);
            return;
        }

        int sampleCount = Math.min(samples.size(), waveWidth);
        for (int i = 0; i < sampleCount; i++) {
            int amplitude = samples.get(i);
            int barHeight = Math.max(1, amplitude * waveHeight / 100);
            int barX = waveX + i * waveWidth / sampleCount;
            int nextX = waveX + (i + 1) * waveWidth / sampleCount;
            guiGraphics.fill(barX, waveY + waveHeight - barHeight, Math.max(barX + 1, nextX), waveY + waveHeight, YELLOW);
        }

        Component detail = Component.translatable("overlay.dglabcraft.waveform_detail",
            preview.waveform(), formatSeconds(preview.remainingMillis()));
        guiGraphics.drawString(font, detail, waveX + 4, y, TEXT, false);
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

    private static void drawPanel(GuiGraphics guiGraphics, OverlayLayout.Rect rect, boolean editing) {
        guiGraphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BG);
        guiGraphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 1, PANEL_BORDER);
        guiGraphics.fill(rect.x(), rect.y() + rect.height() - 1, rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        guiGraphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.y() + rect.height(), PANEL_BORDER);
        guiGraphics.fill(rect.x() + rect.width() - 1, rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        if (editing) {
            guiGraphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 12, 0x44E8D57A);
            int grip = 6;
            guiGraphics.fill(rect.x(), rect.y(), rect.x() + grip, rect.y() + grip, PANEL_BORDER);
            guiGraphics.fill(rect.x() + rect.width() - grip, rect.y(), rect.x() + rect.width(), rect.y() + grip, PANEL_BORDER);
            guiGraphics.fill(rect.x(), rect.y() + rect.height() - grip, rect.x() + grip, rect.y() + rect.height(), PANEL_BORDER);
            guiGraphics.fill(rect.x() + rect.width() - grip, rect.y() + rect.height() - grip,
                rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        }
    }

    private static void renderScaled(GuiGraphics guiGraphics, OverlayLayout.Rect rect, int baseWidth, Runnable draw) {
        float scale = (float) Math.max(0.01D, rect.width() / (double) baseWidth);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(rect.x(), rect.y(), 0.0D);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        draw.run();
        guiGraphics.pose().popPose();
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

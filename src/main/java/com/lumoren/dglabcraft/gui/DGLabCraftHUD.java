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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DGLabCraftHUD {
    private static final int PANEL_BG = 0xCC101014;
    private static final int PANEL_BORDER = 0xAAE8D57A;
    private static final int TEXT = 0xFFEDE6C8;
    private static final int MUTED = 0xFF9E9E9E;
    private static final int GREEN = 0xFF55FF88;
    private static final int YELLOW = 0xFFE8D57A;
    private static final int PULSE_CORE = 0x00F6E6A2;
    private static final int PULSE_GLOW = 0x00E8D57A;
    private static final int WAVE_TRACK_BG = 0xBB222224;
    private static final int WAVE_TRACK_SHADOW = 0x66000000;
    private static final int RED = 0xFFFF7777;
    private static final int TIMELINE_LIMIT = 96;
    private static final Map<String, ChannelTimeline> WAVEFORM_TIMELINES = new HashMap<>();

    public static void render(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
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
            OverlayLayout.Rect rect = OverlayLayout.defaultHudRect(ModConfig.HUD_POSITION.get(), screenWidth, screenHeight);
            double scale = ModConfig.HUD_SCALE.get();
            if (Math.abs(scale - 1.0D) < 0.0001D) {
                return rect;
            }
            return OverlayLayout.scaledRectFromRatio(
                OverlayLayout.ratioFromPixel(rect.x(), rect.width(), screenWidth),
                OverlayLayout.ratioFromPixel(rect.y(), rect.height(), screenHeight),
                OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, scale, screenWidth, screenHeight);
        }
        return OverlayLayout.scaledRectFromRatio(ModConfig.HUD_X_RATIO.get(), ModConfig.HUD_Y_RATIO.get(),
            OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, ModConfig.HUD_SCALE.get(), screenWidth, screenHeight);
    }

    static OverlayLayout.Rect waveformRect(int screenWidth, int screenHeight) {
        if (!OverlayLayout.hasSavedRatio(ModConfig.WAVEFORM_X_RATIO.get(), ModConfig.WAVEFORM_Y_RATIO.get())) {
            OverlayLayout.Rect rect = OverlayLayout.defaultWaveformRect(screenWidth, screenHeight);
            double scale = ModConfig.WAVEFORM_SCALE.get();
            if (Math.abs(scale - 1.0D) < 0.0001D) {
                return rect;
            }
            return OverlayLayout.scaledRectFromRatio(
                OverlayLayout.ratioFromPixel(rect.x(), rect.width(), screenWidth),
                OverlayLayout.ratioFromPixel(rect.y(), rect.height(), screenHeight),
                OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, scale, screenWidth, screenHeight);
        }
        return OverlayLayout.scaledRectFromRatio(ModConfig.WAVEFORM_X_RATIO.get(), ModConfig.WAVEFORM_Y_RATIO.get(),
            OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, ModConfig.WAVEFORM_SCALE.get(), screenWidth, screenHeight);
    }

    static void renderHudPanel(PoseStack poseStack, Font font, WebSocketServerManager server,
                               OverlayLayout.Rect rect, boolean editing) {
        drawPanel(poseStack, rect, editing);
        renderScaled(poseStack, rect, OverlayLayout.HUD_WIDTH, () -> {
            int centerX = OverlayLayout.HUD_WIDTH / 2;
            drawCentered(poseStack, font, tr("overlay.dglabcraft.hud_title"), centerX, 7, YELLOW);

            if (!server.isConnected()) {
                drawCentered(poseStack, font, tr("overlay.dglabcraft.disconnected"), centerX, 23, RED);
                String keyName = ClientModEvents.OPEN_SETTINGS_KEY.get().getTranslatedKeyMessage().getString();
                drawCentered(poseStack, font, tr("overlay.dglabcraft.open_settings_hint", keyName), centerX, 39, MUTED);
                return;
            }

            drawCentered(poseStack, font, tr("overlay.dglabcraft.connected"), centerX, 21, GREEN);
            drawCentered(poseStack, font, channelStatusText("A", (int) server.getChannelAIntensity(), server.getChannelAStatus()),
                centerX, 35, TEXT);
            drawCentered(poseStack, font, channelStatusText("B", (int) server.getChannelBIntensity(), server.getChannelBStatus()),
                centerX, 47, TEXT);
        });
    }

    static void renderWaveformPanel(PoseStack poseStack, Font font, WebSocketServerManager server,
                                    OverlayLayout.Rect rect, boolean editing) {
        drawPanel(poseStack, rect, editing);
        renderScaled(poseStack, rect, OverlayLayout.WAVEFORM_WIDTH, () -> {
            int x = 8;
            int y = 6;
            font.draw(poseStack, tr("overlay.dglabcraft.waveform_title"), x, y, YELLOW);

            ChannelPreview channelA = channelPreview(server, "A");
            ChannelPreview channelB = channelPreview(server, "B");
            long animationTime = System.currentTimeMillis();
            renderChannelWaveform(poseStack, font, channelA, 8, 20, OverlayLayout.WAVEFORM_WIDTH - 16, "A", animationTime);
            renderChannelWaveform(poseStack, font, channelB, 8, 52, OverlayLayout.WAVEFORM_WIDTH - 16, "B", animationTime);
        });
    }

    private static void renderChannelWaveform(PoseStack poseStack, Font font, ChannelPreview preview,
                                              int x, int y, int width, String channel, long animationTime) {
        font.draw(poseStack, tr("overlay.dglabcraft.channel_label", channel), x, y, TEXT);
        int waveX = x + 18;
        int waveY = y + 11;
        int waveWidth = width - 18;
        int waveHeight = 20;
        drawWaveTrack(poseStack, waveX, waveY, waveWidth, waveHeight);

        if (!preview.active()) {
            updateTimeline(channel, preview, animationTime);
            font.draw(poseStack, tr("overlay.dglabcraft.idle"), waveX + 4, y, MUTED);
            renderPulseBars(poseStack, timelineFor(channel).history(), waveX, waveY, waveWidth, waveHeight, animationTime);
            return;
        }

        List<Integer> samples = preview.samples();
        if (samples.isEmpty()) {
            updateTimeline(channel, preview, animationTime);
            font.draw(poseStack, tr("overlay.dglabcraft.no_waveform"), waveX + 4, y, MUTED);
            renderPulseBars(poseStack, timelineFor(channel).history(), waveX, waveY, waveWidth, waveHeight, animationTime);
            return;
        }
        updateTimeline(channel, preview, animationTime);

        Component detail = tr("overlay.dglabcraft.waveform_detail",
            chineseWaveformName(preview.waveform()), formatSeconds(preview.remainingMillis()));
        font.draw(poseStack, detail, waveX + 4, y, MUTED);
        renderPulseBars(poseStack, timelineFor(channel).history(), waveX, waveY, waveWidth, waveHeight, animationTime);
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
        if (waveform == null || waveform.trim().isEmpty()) {
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
            int grip = 6;
            GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.x() + grip, rect.y() + grip, PANEL_BORDER);
            GuiComponent.fill(poseStack, rect.x() + rect.width() - grip, rect.y(), rect.x() + rect.width(), rect.y() + grip, PANEL_BORDER);
            GuiComponent.fill(poseStack, rect.x(), rect.y() + rect.height() - grip, rect.x() + grip, rect.y() + rect.height(), PANEL_BORDER);
            GuiComponent.fill(poseStack, rect.x() + rect.width() - grip, rect.y() + rect.height() - grip,
                rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        }
    }

    private static void drawWaveTrack(PoseStack poseStack, int x, int y, int width, int height) {
        GuiComponent.fill(poseStack, x, y, x + width, y + height, WAVE_TRACK_BG);
        GuiComponent.fill(poseStack, x, y + height - 2, x + width, y + height, WAVE_TRACK_SHADOW);
        GuiComponent.fill(poseStack, x, y, x + width, y + 1, 0x332A2A2E);
    }

    private static void renderPulseBars(PoseStack poseStack, List<Integer> samples, int x, int y,
                                        int width, int height, long animationTime) {
        for (WaveformPreview.PulseBar bar : WaveformPreview.pulseBars(samples, width, height, animationTime)) {
            int barX = x + bar.x();
            int barY = y + height - bar.height();
            int barRight = Math.min(x + width, barX + bar.width());
            int barBottom = Math.min(y + height, barY + bar.height());
            if (barRight <= x || barX >= x + width) {
                continue;
            }

            int glowAlpha = Math.min(96, Math.max(36, bar.alpha() / 3));
            GuiComponent.fill(poseStack, Math.max(x, barX - 1), Math.max(y, barY - 1),
                Math.min(x + width, barRight + 1), barBottom,
                withAlpha(PULSE_GLOW, glowAlpha));
            GuiComponent.fill(poseStack, Math.max(x, barX), barY, barRight, barBottom, withAlpha(PULSE_CORE, bar.alpha()));
        }
    }

    private static void updateTimeline(String channel, ChannelPreview preview, long animationTime) {
        ChannelTimeline timeline = timelineFor(channel);
        long slot = Math.max(0L, animationTime) / WaveformPreview.STRENGTH_BAR_MILLIS;
        if (timeline.lastSlot() < 0L) {
            timeline.setLastSlot(slot - 1L);
        } else if (slot - timeline.lastSlot() > TIMELINE_LIMIT) {
            timeline.setLastSlot(slot - TIMELINE_LIMIT);
        }

        if (preview.active() && !preview.samples().isEmpty()) {
            timeline.startWaveformIfNeeded(preview.waveform(), slot);
        } else {
            timeline.clearActiveWaveform();
        }

        while (timeline.lastSlot() < slot) {
            long nextSlot = timeline.lastSlot() + 1L;
            int amplitude = 0;
            if (preview.active() && !preview.samples().isEmpty()) {
                long elapsed = Math.max(0L, (nextSlot - timeline.activeStartSlot()) * WaveformPreview.STRENGTH_BAR_MILLIS);
                amplitude = WaveformPreview.sampleAt(preview.samples(), elapsed);
            }
            timeline.add(amplitude);
            timeline.setLastSlot(nextSlot);
        }
    }

    private static ChannelTimeline timelineFor(String channel) {
        return WAVEFORM_TIMELINES.computeIfAbsent(channel, ignored -> new ChannelTimeline());
    }

    private static void renderScaled(PoseStack poseStack, OverlayLayout.Rect rect, int baseWidth, Runnable draw) {
        float scale = (float) Math.max(0.01D, rect.width() / (double) baseWidth);
        poseStack.pushPose();
        poseStack.translate(rect.x(), rect.y(), 0.0D);
        poseStack.scale(scale, scale, 1.0F);
        draw.run();
        poseStack.popPose();
    }

    private static String formatSeconds(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        return tr("duration.dglabcraft.seconds", seconds).getString();
    }

    private static Component channelStatusText(String channel, int intensity, String status) {
        return txt(channel + ": " + intensity + "% " + chineseWaveformName(status));
    }

    private static Component tr(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    private static Component txt(String value) {
        return new TextComponent(value);
    }

    private static String chineseWaveformName(String status) {
        if (status == null || status.trim().isEmpty() || "Idle".equalsIgnoreCase(status)) {
            return "空闲";
        }
        if ("beat".equals(status)) return "节拍";
        if ("bounce_gradual".equals(status)) return "渐弹";
        if ("breath".equals(status)) return "呼吸";
        if ("burn".equals(status)) return "灼烧";
        if ("compress".equals(status)) return "压缩";
        if ("drown".equals(status)) return "溺水";
        if ("fast_pinch".equals(status)) return "快夹";
        if ("grain_friction".equals(status)) return "颗粒摩擦";
        if ("heartbeat".equals(status)) return "心跳";
        if ("pinch_intensify".equals(status)) return "夹紧增强";
        if ("rain_wash".equals(status)) return "雨刷";
        if ("rhythm_step".equals(status)) return "节奏步进";
        if ("signal_light".equals(status)) return "信号灯";
        if ("tease1".equals(status)) return "挑逗一";
        if ("tease2".equals(status)) return "挑逗二";
        if ("tide".equals(status)) return "潮汐";
        if ("variable_speed".equals(status)) return "变速";
        if ("wave_ripple".equals(status)) return "波纹";
        if ("default".equals(status)) return "默认";
        if ("ADamage".equals(status)) return "A伤害";
        if ("BDamage".equals(status)) return "B伤害";
        return status;
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
    }

    private static void drawCentered(PoseStack poseStack, Font font, Component text, int centerX, int y, int color) {
        font.draw(poseStack, text, centerX - font.width(text) / 2, y, color);
    }

    private static final class ChannelTimeline {
        private final ArrayList<Integer> history = new ArrayList<>();
        private long lastSlot = -1L;
        private String activeWaveform = "";
        private long activeStartSlot = 0L;

        void add(int amplitude) {
            history.add(amplitude);
            while (history.size() > TIMELINE_LIMIT) {
                history.remove(0);
            }
        }

        List<Integer> history() {
            return history;
        }

        long lastSlot() {
            return lastSlot;
        }

        void setLastSlot(long lastSlot) {
            this.lastSlot = lastSlot;
        }

        void startWaveformIfNeeded(String waveform, long currentSlot) {
            String safeWaveform = waveform == null ? "" : waveform;
            if (!safeWaveform.equals(activeWaveform)) {
                activeWaveform = safeWaveform;
                activeStartSlot = currentSlot;
            }
        }

        void clearActiveWaveform() {
            activeWaveform = "";
        }

        long activeStartSlot() {
            return activeStartSlot;
        }
    }

    private static final class ChannelPreview {
        private final boolean active;
        private final String waveform;
        private final long remainingMillis;
        private final List<Integer> samples;

        private ChannelPreview(boolean active, String waveform, long remainingMillis, List<Integer> samples) {
            this.active = active;
            this.waveform = waveform;
            this.remainingMillis = remainingMillis;
            this.samples = samples;
        }

        boolean active() { return active; }
        String waveform() { return waveform; }
        long remainingMillis() { return remainingMillis; }
        List<Integer> samples() { return samples; }

        static ChannelPreview idle() {
            return new ChannelPreview(false, "", 0L, Collections.emptyList());
        }

        static ChannelPreview active(String waveform, long remainingMillis, List<Integer> samples) {
            return new ChannelPreview(true, waveform == null || waveform.trim().isEmpty() ? "default" : waveform, remainingMillis, samples);
        }
    }
}

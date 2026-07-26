package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.ClientModEvents;
import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.WaveformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DGLabCraftHUD extends Gui {
    private static final int PANEL_BG = 0xCC101014;
    private static final int PANEL_BORDER = 0xAAE8D57A;
    private static final int TEXT = 0xFFEDE6C8;
    private static final int MUTED = 0xFF9E9E9E;
    private static final int GREEN = 0xFF55FF88;
    private static final int YELLOW = 0xFFE8D57A;
    private static final int RED = 0xFFFF7777;
    private static final int TIMELINE_LIMIT = 96;
    private static final Map<String, ChannelTimeline> WAVEFORM_TIMELINES = new HashMap<String, ChannelTimeline>();

    @SubscribeEvent
    public void onRenderGuiOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.currentScreen != null) {
            return;
        }
        ScaledResolution resolution = event.resolution;
        WebSocketServerManager server = WebSocketServerManager.getInstance();
        if (ModConfig.HUD_ENABLED.get()) {
            renderHudPanel(mc.fontRendererObj, server, hudRect(resolution.getScaledWidth(), resolution.getScaledHeight()), false);
        }
        if (ModConfig.WAVEFORM_OVERLAY_ENABLED.get()) {
            renderWaveformPanel(mc.fontRendererObj, server, waveformRect(resolution.getScaledWidth(), resolution.getScaledHeight()), false);
        }
    }

    static OverlayLayout.Rect hudRect(int screenWidth, int screenHeight) {
        if (!OverlayLayout.hasSavedRatio(ModConfig.HUD_X_RATIO.get(), ModConfig.HUD_Y_RATIO.get())) {
            return OverlayLayout.defaultHudRect(ModConfig.HUD_POSITION.get(), screenWidth, screenHeight);
        }
        return OverlayLayout.scaledRectFromRatio(ModConfig.HUD_X_RATIO.get(), ModConfig.HUD_Y_RATIO.get(),
            OverlayLayout.HUD_WIDTH, OverlayLayout.HUD_HEIGHT, ModConfig.HUD_SCALE.get(), screenWidth, screenHeight);
    }

    static OverlayLayout.Rect waveformRect(int screenWidth, int screenHeight) {
        if (!OverlayLayout.hasSavedRatio(ModConfig.WAVEFORM_X_RATIO.get(), ModConfig.WAVEFORM_Y_RATIO.get())) {
            return OverlayLayout.defaultWaveformRect(screenWidth, screenHeight);
        }
        return OverlayLayout.scaledRectFromRatio(ModConfig.WAVEFORM_X_RATIO.get(), ModConfig.WAVEFORM_Y_RATIO.get(),
            OverlayLayout.WAVEFORM_WIDTH, OverlayLayout.WAVEFORM_HEIGHT, ModConfig.WAVEFORM_SCALE.get(), screenWidth, screenHeight);
    }

    static void renderHudPanel(FontRenderer font, WebSocketServerManager server, OverlayLayout.Rect rect, boolean editing) {
        drawPanel(rect, editing);
        renderScaled(rect, OverlayLayout.HUD_WIDTH, new Runnable() {
            public void run() {
                int centerX = OverlayLayout.HUD_WIDTH / 2;
                drawCentered(font, "DGLab 状态", centerX, 7, YELLOW);
                if (!server.isConnected()) {
                    drawCentered(font, "未连接", centerX, 23, RED);
                    drawCentered(font, "按 " + net.minecraft.client.settings.GameSettings.getKeyDisplayString(ClientModEvents.OPEN_SETTINGS_KEY.getKeyCode()) + " 打开设置", centerX, 39, MUTED);
                    return;
                }
                drawCentered(font, "已连接", centerX, 21, GREEN);
                drawCentered(font, "A: " + (int) server.getChannelAIntensity() + "% " + chineseWaveformName(server.getChannelAStatus()), centerX, 35, TEXT);
                drawCentered(font, "B: " + (int) server.getChannelBIntensity() + "% " + chineseWaveformName(server.getChannelBStatus()), centerX, 47, TEXT);
            }
        });
    }

    static void renderWaveformPanel(FontRenderer font, WebSocketServerManager server, OverlayLayout.Rect rect, boolean editing) {
        drawPanel(rect, editing);
        renderScaled(rect, OverlayLayout.WAVEFORM_WIDTH, new Runnable() {
            public void run() {
                font.drawStringWithShadow("波形预览", 8, 6, YELLOW);
                long now = System.currentTimeMillis();
                renderChannelWaveform(font, channelPreview(server, "A"), 8, 20, OverlayLayout.WAVEFORM_WIDTH - 16, "A", now);
                renderChannelWaveform(font, channelPreview(server, "B"), 8, 52, OverlayLayout.WAVEFORM_WIDTH - 16, "B", now);
            }
        });
    }

    private static void renderChannelWaveform(FontRenderer font, ChannelPreview preview, int x, int y, int width, String channel, long now) {
        font.drawStringWithShadow(channel, x, y, TEXT);
        int waveX = x + 18;
        int waveY = y + 11;
        int waveWidth = width - 18;
        int waveHeight = 20;
        Gui.drawRect(waveX, waveY, waveX + waveWidth, waveY + waveHeight, 0xBB222224);
        updateTimeline(channel, preview, now);
        if (preview.active) {
            font.drawStringWithShadow(chineseWaveformName(preview.waveform), waveX + 4, y, MUTED);
        } else {
            font.drawStringWithShadow("空闲", waveX + 4, y, MUTED);
        }
        renderPulseBars(timelineFor(channel).history(), waveX, waveY, waveWidth, waveHeight, now);
    }

    private static ChannelPreview channelPreview(WebSocketServerManager server, String channel) {
        if (!server.isConnected()) {
            return ChannelPreview.idle();
        }
        if (server.isSyncRuntimeActive()) {
            String waveform = server.getSyncRuntimeWaveform();
            return ChannelPreview.active(waveform, WaveformPreview.amplitudes(WaveformManager.getInstance().getWaveform(waveform), 96));
        }
        if (server.isChannelRuntimeActive(channel)) {
            String waveform = server.getChannelRuntimeWaveform(channel);
            return ChannelPreview.active(waveform, WaveformPreview.amplitudes(WaveformManager.getInstance().getWaveform(waveform), 96));
        }
        if (server.isPulsePreviewActive(channel)) {
            String waveform = server.getPulsePreviewWaveform(channel);
            return ChannelPreview.active(waveform, WaveformPreview.amplitudes(WaveformManager.getInstance().getWaveform(waveform), 96));
        }
        return ChannelPreview.idle();
    }

    private static void drawPanel(OverlayLayout.Rect rect, boolean editing) {
        Gui.drawRect(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BG);
        Gui.drawRect(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 1, PANEL_BORDER);
        Gui.drawRect(rect.x(), rect.y() + rect.height() - 1, rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        Gui.drawRect(rect.x(), rect.y(), rect.x() + 1, rect.y() + rect.height(), PANEL_BORDER);
        Gui.drawRect(rect.x() + rect.width() - 1, rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), PANEL_BORDER);
        if (editing) {
            Gui.drawRect(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + 12, 0x44E8D57A);
        }
    }

    private static void renderPulseBars(List<Integer> samples, int x, int y, int width, int height, long now) {
        for (WaveformPreview.PulseBar bar : WaveformPreview.pulseBars(samples, width, height, now)) {
            int barX = x + bar.x();
            int barY = y + height - bar.height();
            int barRight = Math.min(x + width, barX + bar.width());
            if (barRight <= x || barX >= x + width) {
                continue;
            }
            Gui.drawRect(Math.max(x, barX), barY, barRight, y + height, withAlpha(0x00F6E6A2, bar.alpha()));
        }
    }

    private static void updateTimeline(String channel, ChannelPreview preview, long now) {
        ChannelTimeline timeline = timelineFor(channel);
        long slot = Math.max(0L, now) / WaveformPreview.STRENGTH_BAR_MILLIS;
        if (timeline.lastSlot < 0L) {
            timeline.lastSlot = slot - 1L;
        } else if (slot - timeline.lastSlot > TIMELINE_LIMIT) {
            timeline.lastSlot = slot - TIMELINE_LIMIT;
        }
        if (preview.active && !preview.samples.isEmpty()) {
            if (!preview.waveform.equals(timeline.activeWaveform)) {
                timeline.activeWaveform = preview.waveform;
                timeline.activeStartSlot = slot;
            }
        } else {
            timeline.activeWaveform = "";
        }
        while (timeline.lastSlot < slot) {
            long nextSlot = timeline.lastSlot + 1L;
            int amplitude = 0;
            if (preview.active && !preview.samples.isEmpty()) {
                long elapsed = Math.max(0L, (nextSlot - timeline.activeStartSlot) * WaveformPreview.STRENGTH_BAR_MILLIS);
                amplitude = WaveformPreview.sampleAt(preview.samples, elapsed);
            }
            timeline.add(amplitude);
            timeline.lastSlot = nextSlot;
        }
    }

    private static ChannelTimeline timelineFor(String channel) {
        ChannelTimeline timeline = WAVEFORM_TIMELINES.get(channel);
        if (timeline == null) {
            timeline = new ChannelTimeline();
            WAVEFORM_TIMELINES.put(channel, timeline);
        }
        return timeline;
    }

    private static void renderScaled(OverlayLayout.Rect rect, int baseWidth, Runnable draw) {
        float scale = (float) Math.max(0.01D, rect.width() / (double) baseWidth);
        GL11.glPushMatrix();
        GL11.glTranslatef(rect.x(), rect.y(), 0.0F);
        GL11.glScalef(scale, scale, 1.0F);
        draw.run();
        GL11.glPopMatrix();
    }

    private static void drawCentered(FontRenderer font, String text, int centerX, int y, int color) {
        font.drawStringWithShadow(text, centerX - font.getStringWidth(text) / 2, y, color);
    }

    static String chineseWaveformName(String status) {
        if (status == null || status.trim().isEmpty() || "Idle".equalsIgnoreCase(status)) return "空闲";
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
        if ("tide".equals(status)) return "潮汐";
        return status;
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
    }

    private static final class ChannelTimeline {
        private final ArrayList<Integer> history = new ArrayList<Integer>();
        private long lastSlot = -1L;
        private String activeWaveform = "";
        private long activeStartSlot = 0L;
        void add(int amplitude) {
            history.add(amplitude);
            while (history.size() > TIMELINE_LIMIT) history.remove(0);
        }
        List<Integer> history() { return history; }
    }

    private static final class ChannelPreview {
        private final boolean active;
        private final String waveform;
        private final List<Integer> samples;
        private ChannelPreview(boolean active, String waveform, List<Integer> samples) {
            this.active = active;
            this.waveform = waveform == null || waveform.trim().isEmpty() ? "default" : waveform;
            this.samples = samples == null ? Collections.<Integer>emptyList() : samples;
        }
        static ChannelPreview idle() { return new ChannelPreview(false, "", Collections.<Integer>emptyList()); }
        static ChannelPreview active(String waveform, List<Integer> samples) { return new ChannelPreview(true, waveform, samples); }
    }
}

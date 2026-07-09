package com.lumoren.dglabcraft.gui;

import com.lumoren.dglabcraft.config.ModConfig;
import com.lumoren.dglabcraft.network.WebSocketServerManager;
import com.lumoren.dglabcraft.util.IssueReportBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int CONTENT_WIDTH = 320;
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_HEIGHT = 12;
    private static final int SECTION_GAP = 8;

    private final Screen parent;
    private Button regenerateQrButton;
    private Button copyIssueButton;
    private Button silenceButton;
    private Button doneButton;
    private Component copyStatus = txt("");
    private int scrollOffset;
    private int maxScroll;

    public DiagnosticScreen(Screen parent) {
        super(tr("screen.dglabcraft.diagnostics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int totalButtonWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int startX = centerX - totalButtonWidth / 2;
        int firstRowY = this.height - 54;
        int secondRowY = this.height - 30;

        this.regenerateQrButton = new Button(startX, firstRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            tr("button.dglabcraft.regenerate_qr"),
            button -> WebSocketServerManager.getInstance().generateQrUrl());
        this.addRenderableWidget(this.regenerateQrButton);

        this.copyIssueButton = new Button(startX + BUTTON_WIDTH + BUTTON_GAP, firstRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            tr("button.dglabcraft.copy_issue_info"),
            button -> {
                String report = buildIssueReport(WebSocketServerManager.getInstance());
                this.minecraft.keyboardHandler.setClipboard(report);
                this.copyStatus = tr("status.dglabcraft.issue_info_copied").withStyle(ChatFormatting.GREEN);
            });
        this.addRenderableWidget(this.copyIssueButton);

        this.silenceButton = new Button(startX, secondRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            tr("button.dglabcraft.stop_feedback_now"),
            button -> WebSocketServerManager.getInstance().safeSilenceAll());
        this.addRenderableWidget(this.silenceButton);

        this.doneButton = new Button(startX + BUTTON_WIDTH + BUTTON_GAP, secondRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            tr("button.dglabcraft.done"), button -> this.onClose());
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(poseStack);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        int centerX = this.width / 2;
        int contentX = centerX - CONTENT_WIDTH / 2;
        int y = 20;

        drawCenteredString(poseStack, this.font, tr("screen.dglabcraft.diagnostics"), centerX, y, 0xFFFFFF);
        y += 18;
        drawCenteredString(poseStack, this.font, tr("message.dglabcraft.diagnostics_summary"), centerX, y, 0xAAAAAA);
        y += 20;
        if (!copyStatus.getString().isEmpty()) {
            drawCenteredString(poseStack, this.font, copyStatus, centerX, y, 0x55FF55);
        }
        y += 14;

        int viewportTop = y;
        int viewportBottom = DiagnosticLayout.viewportBottom(this.height);
        List<RenderLine> lines = buildRenderLines(server);
        int contentHeight = lines.stream().mapToInt(line -> line.height).sum();
        this.maxScroll = DiagnosticLayout.maxScroll(contentHeight, viewportTop, viewportBottom);
        clampScroll();

        int lineY = viewportTop - this.scrollOffset;
        for (RenderLine line : lines) {
            if (line.text != null && DiagnosticLayout.isLineFullyVisible(lineY, line.height, viewportTop, viewportBottom)) {
                this.font.draw(poseStack, line.text, contentX, lineY, line.color);
            }
            lineY += line.height;
        }
        renderScrollBar(poseStack, contentX + CONTENT_WIDTH + 8, viewportTop, viewportBottom, contentHeight);

        this.regenerateQrButton.active = server.isRunning() && !server.isConnected();
        this.silenceButton.active = server.isConnected() && server.hasLiveOutput();

        super.render(poseStack, pMouseX, pMouseY, pPartialTick);
    }

    private List<Component> buildConnectionLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();

        if (server.isConnected()) {
            lines.add(tr("diagnostic.dglabcraft.connection.connected"));
            lines.add(tr("diagnostic.dglabcraft.connection.check_channels"));
        } else if (server.isWaitingForAppBind()) {
            lines.add(tr("diagnostic.dglabcraft.connection.waiting_bind"));
            lines.add(tr("diagnostic.dglabcraft.connection.confirm_or_rescan"));
        } else if (server.isRunning()) {
            lines.add(tr("diagnostic.dglabcraft.connection.not_connected"));
            lines.add(tr("diagnostic.dglabcraft.connection.scan_same_lan"));
        } else {
            lines.add(tr("diagnostic.dglabcraft.connection.service_stopped"));
            lines.add(tr("diagnostic.dglabcraft.connection.check_port"));
        }

        return lines;
    }

    private List<Component> buildChannelLines(WebSocketServerManager server, String channel) {
        List<Component> lines = new ArrayList<>();
        boolean active = server.isChannelRuntimeActive(channel);
        int intensity = server.getChannelRuntimeIntensity(channel);
        Component detail = toPlainDetail(server.getChannelRuntimeSource(channel), server.getChannelRuntimeDetail(channel));

        if (!server.isConnected()) {
            lines.add(tr("diagnostic.dglabcraft.channel.disconnected"));
            return lines;
        }

        if (active) {
            lines.add(tr("diagnostic.dglabcraft.channel.active", channel));
            lines.add(tr("diagnostic.dglabcraft.channel.active_detail", intensity, detail));
            lines.add(tr("diagnostic.dglabcraft.channel.stop_hint"));
        } else {
            lines.add(tr("diagnostic.dglabcraft.channel.idle", channel));
            lines.add(tr("diagnostic.dglabcraft.channel.waiting_event"));
        }

        return lines;
    }

    private List<Component> buildRuntimeLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();

        if (!server.isConnected()) {
            lines.add(tr("diagnostic.dglabcraft.runtime.none_disconnected"));
            lines.add(tr("diagnostic.dglabcraft.runtime.connect_first"));
            return lines;
        }

        if (server.isSyncRuntimeActive()) {
            lines.add(tr("diagnostic.dglabcraft.runtime.sync_active"));
            lines.add(tr("diagnostic.dglabcraft.runtime.current_source",
                toPlainDetail(server.getSyncRuntimeSource(), server.getSyncRuntimeDetail())));
            lines.add(tr("diagnostic.dglabcraft.runtime.remaining", formatSeconds(server.getSyncRuntimeRemainingMillis())));
            return lines;
        }

        if (server.hasActiveEffects()) {
            lines.add(tr("diagnostic.dglabcraft.runtime.active_effects"));
            lines.add(tr("diagnostic.dglabcraft.runtime.check_channels"));
        } else {
            lines.add(tr("diagnostic.dglabcraft.runtime.no_recent_effects"));
            lines.add(tr("diagnostic.dglabcraft.runtime.no_event_now"));
            lines.add(tr("diagnostic.dglabcraft.runtime.stop_if_device_continues"));
        }

        return lines;
    }

    private List<Component> buildDetailLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();
        lines.add(tr("diagnostic.dglabcraft.detail.address", server.resolveConnectionHost(), server.getPort()).withStyle(ChatFormatting.GRAY));
        lines.add(tr("diagnostic.dglabcraft.detail.service", statusKey(server.isRunning())).withStyle(ChatFormatting.GRAY));
        lines.add(tr("diagnostic.dglabcraft.detail.socket", statusKey(server.hasClientSocketConnection())).withStyle(ChatFormatting.GRAY));
        lines.add(tr("diagnostic.dglabcraft.detail.bind", bindStatus(server)).withStyle(ChatFormatting.GRAY));

        if (server.getConnectedClientId() != null) {
            lines.add(tr("diagnostic.dglabcraft.detail.client_id", IssueReportBuilder.redactId(server.getConnectedClientId())).withStyle(ChatFormatting.GRAY));
        }
        if (server.getTargetId() != null) {
            lines.add(tr("diagnostic.dglabcraft.detail.target_id", IssueReportBuilder.redactId(server.getTargetId())).withStyle(ChatFormatting.GRAY));
        }

        lines.add(txt("A: status=" + server.getChannelAStatus() + ", intensity=" + (int) server.getChannelAIntensity() + "%"
            + formatWaveformDetail(server, "A")).withStyle(ChatFormatting.GRAY));
        lines.add(txt("B: status=" + server.getChannelBStatus() + ", intensity=" + (int) server.getChannelBIntensity() + "%"
            + formatWaveformDetail(server, "B")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private String buildIssueReport(WebSocketServerManager server) {
        IssueReportBuilder.EnvironmentInfo environment = new IssueReportBuilder.EnvironmentInfo(
            modVersion("dglabcraft"),
            SharedConstants.getCurrentVersion().getName(),
            "Fabric",
            modVersion("fabricloader"),
            System.getProperty("java.version", "unknown"),
            System.getProperty("os.name", "unknown"),
            System.getProperty("os.version", "unknown"),
            server.resolveConnectionHost(),
            server.getPort(),
            server.isRunning(),
            server.hasClientSocketConnection(),
            server.isConnected(),
            server.isWaitingForAppBind(),
            ModConfig.SYNC_CHANNELS.get(),
            ModConfig.HUD_ENABLED.get(),
            server.getConnectedClientId(),
            server.getTargetId()
        );

        return IssueReportBuilder.build(
            environment,
            channelInfo(server, "A", server.getChannelAStatus(), (int) server.getChannelAIntensity()),
            channelInfo(server, "B", server.getChannelBStatus(), (int) server.getChannelBIntensity()),
            new IssueReportBuilder.RuntimeInfo(
                server.isSyncRuntimeActive(),
                server.getSyncRuntimeSource().name(),
                server.getSyncRuntimeDetail(),
                server.getSyncRuntimeWaveform(),
                server.getSyncRuntimeRemainingMillis()
            )
        );
    }

    private IssueReportBuilder.ChannelInfo channelInfo(WebSocketServerManager server, String channel, String status, int intensity) {
        return new IssueReportBuilder.ChannelInfo(
            channel,
            status,
            intensity,
            server.isChannelRuntimeActive(channel),
            server.getChannelRuntimeSource(channel).name(),
            server.getChannelRuntimeDetail(channel),
            server.getChannelRuntimeWaveform(channel),
            server.getChannelRuntimeRemainingMillis(channel)
        );
    }

    private String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }

    private static MutableComponent tr(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    private static MutableComponent txt(String value) {
        return new TextComponent(value);
    }

    private Component statusKey(boolean enabled) {
        return tr(enabled ? "status.dglabcraft.started" : "status.dglabcraft.stopped");
    }

    private Component bindStatus(WebSocketServerManager server) {
        if (server.isConnected()) {
            return tr("status.dglabcraft.completed");
        }
        if (server.isWaitingForAppBind()) {
            return tr("status.dglabcraft.waiting_confirmation");
        }
        return tr("status.dglabcraft.incomplete");
    }

    private String formatWaveformDetail(WebSocketServerManager server, String channel) {
        String waveform = server.getChannelRuntimeWaveform(channel);
        long remainingMillis = server.getChannelRuntimeRemainingMillis(channel);
        if (waveform == null || waveform.isBlank()) {
            return "";
        }
        return ", waveform=" + waveform + ", remaining=" + formatSeconds(remainingMillis);
    }

    private List<RenderLine> buildRenderLines(WebSocketServerManager server) {
        List<RenderLine> lines = new ArrayList<>();
        addSection(lines, tr("section.dglabcraft.connection_status"), buildConnectionLines(server));
        addSection(lines, tr("section.dglabcraft.channel_status", "A"), buildChannelLines(server, "A"));
        addSection(lines, tr("section.dglabcraft.channel_status", "B"), buildChannelLines(server, "B"));
        addSection(lines, tr("section.dglabcraft.runtime_summary"), buildRuntimeLines(server));
        addSection(lines, tr("section.dglabcraft.details"), buildDetailLines(server));
        return lines;
    }

    private void addSection(List<RenderLine> renderLines, Component title, List<Component> lines) {
        renderLines.add(new RenderLine(title.getVisualOrderText(), 0xFFFFFF, TITLE_HEIGHT));
        for (Component line : lines) {
            List<FormattedCharSequence> wrapped = this.font.split(line, CONTENT_WIDTH);
            for (FormattedCharSequence textLine : wrapped) {
                renderLines.add(new RenderLine(textLine, 0xD0D0D0, LINE_HEIGHT));
            }
        }
        renderLines.add(new RenderLine(null, 0, SECTION_GAP));
    }

    private void renderScrollBar(PoseStack poseStack, int x, int top, int bottom, int contentHeight) {
        int viewportHeight = bottom - top;
        if (this.maxScroll <= 0 || viewportHeight <= 0) {
            return;
        }

        fill(poseStack, x, top, x + 4, bottom, 0x66000000);
        int thumbHeight = Math.max(18, viewportHeight * viewportHeight / Math.max(viewportHeight, contentHeight));
        int thumbTop = top + (viewportHeight - thumbHeight) * this.scrollOffset / this.maxScroll;
        fill(poseStack, x, thumbTop, x + 4, thumbTop + thumbHeight, 0xFFAAAAAA);
    }

    private void clampScroll() {
        this.scrollOffset = DiagnosticLayout.clampScroll(this.scrollOffset, this.maxScroll);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.maxScroll > 0) {
            this.scrollOffset = DiagnosticLayout.scrollByWheel(this.scrollOffset, pDelta, this.maxScroll);
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private Component toPlainDetail(WebSocketServerManager.EffectSource source, String detail) {
        return switch (source) {
            case DAMAGE -> switch (normalize(detail)) {
                case "lava" -> tr("effect.dglabcraft.damage.lava");
                case "onfire" -> tr("effect.dglabcraft.damage.onfire");
                case "infire" -> tr("effect.dglabcraft.damage.infire");
                case "hotfloor" -> tr("effect.dglabcraft.damage.hotfloor");
                case "drown" -> tr("effect.dglabcraft.damage.drown");
                case "freeze" -> tr("effect.dglabcraft.damage.freeze");
                case "fall" -> tr("effect.dglabcraft.damage.fall");
                case "mob_attack" -> tr("effect.dglabcraft.damage.mob_attack");
                case "player_attack" -> tr("effect.dglabcraft.damage.player_attack");
                case "explosion", "explosion.player" -> tr("effect.dglabcraft.damage.explosion");
                default -> tr("effect.dglabcraft.damage.default");
            };
            case HEARTBEAT -> tr("effect.dglabcraft.heartbeat.low_health");
            case ENVIRONMENT -> switch (normalize(detail)) {
                case "nether" -> tr("effect.dglabcraft.environment.nether");
                case "end" -> tr("effect.dglabcraft.environment.end");
                case "portal" -> tr("effect.dglabcraft.environment.portal");
                case "powder_snow" -> tr("effect.dglabcraft.environment.powder_snow");
                default -> tr("effect.dglabcraft.environment.default");
            };
            case NONE -> tr("effect.dglabcraft.none");
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String formatSeconds(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        return tr("duration.dglabcraft.seconds", seconds).getString();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private static final class RenderLine {
        private final FormattedCharSequence text;
        private final int color;
        private final int height;

        private RenderLine(FormattedCharSequence text, int color, int height) {
            this.text = text;
            this.color = color;
            this.height = height;
        }
    }
}

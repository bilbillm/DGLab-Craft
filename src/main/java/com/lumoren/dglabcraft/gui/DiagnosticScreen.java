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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int CONTENT_WIDTH = 320;

    private final Screen parent;
    private Button regenerateQrButton;
    private Button copyIssueButton;
    private Button silenceButton;
    private Button doneButton;
    private Component copyStatus = TextComponent.EMPTY;

    public DiagnosticScreen(Screen parent) {
        super(new TranslatableComponent("screen.dglabcraft.diagnostics"));
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
            new TranslatableComponent("button.dglabcraft.regenerate_qr"),
            button -> WebSocketServerManager.getInstance().generateQrUrl());
        this.addRenderableWidget(this.regenerateQrButton);

        this.copyIssueButton = new Button(startX + BUTTON_WIDTH + BUTTON_GAP, firstRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            new TranslatableComponent("button.dglabcraft.copy_issue_info"),
            button -> {
                this.minecraft.keyboardHandler.setClipboard(buildIssueReport(WebSocketServerManager.getInstance()));
                this.copyStatus = new TranslatableComponent("status.dglabcraft.issue_info_copied").withStyle(ChatFormatting.GREEN);
            });
        this.addRenderableWidget(this.copyIssueButton);

        this.silenceButton = new Button(startX, secondRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            new TranslatableComponent("button.dglabcraft.stop_feedback_now"),
            button -> WebSocketServerManager.getInstance().safeSilenceAll());
        this.addRenderableWidget(this.silenceButton);

        this.doneButton = new Button(startX + BUTTON_WIDTH + BUTTON_GAP, secondRowY, BUTTON_WIDTH, BUTTON_HEIGHT,
            new TranslatableComponent("button.dglabcraft.done"), button -> this.onClose());
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        WebSocketServerManager server = WebSocketServerManager.getInstance();
        int centerX = this.width / 2;
        int contentX = centerX - CONTENT_WIDTH / 2;
        int y = 20;

        drawCenteredString(poseStack, this.font, new TranslatableComponent("screen.dglabcraft.diagnostics"), centerX, y, 0xFFFFFF);
        y += 18;
        drawCenteredString(poseStack, this.font, new TranslatableComponent("message.dglabcraft.diagnostics_summary"), centerX, y, 0xAAAAAA);
        y += 20;
        if (!copyStatus.getString().isEmpty()) {
            drawCenteredString(poseStack, this.font, copyStatus, centerX, y, 0x55FF55);
        }
        y += 14;

        y = drawSection(poseStack, contentX, y, new TranslatableComponent("section.dglabcraft.connection_status"), buildConnectionLines(server));
        y = drawSection(poseStack, contentX, y, new TranslatableComponent("section.dglabcraft.channel_status", "A"), buildChannelLines(server, "A"));
        y = drawSection(poseStack, contentX, y, new TranslatableComponent("section.dglabcraft.channel_status", "B"), buildChannelLines(server, "B"));
        y = drawSection(poseStack, contentX, y, new TranslatableComponent("section.dglabcraft.runtime_summary"), buildRuntimeLines(server));
        drawSection(poseStack, contentX, y, new TranslatableComponent("section.dglabcraft.details"), buildDetailLines(server));

        this.regenerateQrButton.active = server.isRunning() && !server.isConnected();
        this.silenceButton.active = server.isConnected() && server.hasLiveOutput();

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private List<Component> buildConnectionLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();
        if (server.isConnected()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.connected"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.check_channels"));
        } else if (server.isWaitingForAppBind()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.waiting_bind"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.confirm_or_rescan"));
        } else if (server.isRunning()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.not_connected"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.scan_same_lan"));
        } else {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.service_stopped"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.connection.check_port"));
        }
        return lines;
    }

    private List<Component> buildChannelLines(WebSocketServerManager server, String channel) {
        List<Component> lines = new ArrayList<>();
        boolean active = server.isChannelRuntimeActive(channel);
        int intensity = server.getChannelRuntimeIntensity(channel);
        Component detail = toPlainDetail(server.getChannelRuntimeSource(channel), server.getChannelRuntimeDetail(channel));
        if (!server.isConnected()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.disconnected"));
            return lines;
        }
        if (active) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.active", channel));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.active_detail", intensity, detail));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.stop_hint"));
        } else {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.idle", channel));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.channel.waiting_event"));
        }
        return lines;
    }

    private List<Component> buildRuntimeLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();
        if (!server.isConnected()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.none_disconnected"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.connect_first"));
            return lines;
        }
        if (server.isSyncRuntimeActive()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.sync_active"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.current_source",
                toPlainDetail(server.getSyncRuntimeSource(), server.getSyncRuntimeDetail())));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.remaining", formatSeconds(server.getSyncRuntimeRemainingMillis())));
            return lines;
        }
        if (server.hasActiveEffects()) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.active_effects"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.check_channels"));
        } else {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.no_recent_effects"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.no_event_now"));
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.runtime.stop_if_device_continues"));
        }
        return lines;
    }

    private List<Component> buildDetailLines(WebSocketServerManager server) {
        List<Component> lines = new ArrayList<>();
        lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.address", server.resolveConnectionHost(), server.getPort()).withStyle(ChatFormatting.GRAY));
        lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.service", statusKey(server.isRunning())).withStyle(ChatFormatting.GRAY));
        lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.socket", statusKey(server.hasClientSocketConnection())).withStyle(ChatFormatting.GRAY));
        lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.bind", bindStatus(server)).withStyle(ChatFormatting.GRAY));
        if (server.getConnectedClientId() != null) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.client_id", IssueReportBuilder.redactId(server.getConnectedClientId())).withStyle(ChatFormatting.GRAY));
        }
        if (server.getTargetId() != null) {
            lines.add(new TranslatableComponent("diagnostic.dglabcraft.detail.target_id", IssueReportBuilder.redactId(server.getTargetId())).withStyle(ChatFormatting.GRAY));
        }
        lines.add(new TextComponent("A: status=" + server.getChannelAStatus() + ", intensity=" + (int) server.getChannelAIntensity() + "%"
            + formatWaveformDetail(server, "A")).withStyle(ChatFormatting.GRAY));
        lines.add(new TextComponent("B: status=" + server.getChannelBStatus() + ", intensity=" + (int) server.getChannelBIntensity() + "%"
            + formatWaveformDetail(server, "B")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private String buildIssueReport(WebSocketServerManager server) {
        IssueReportBuilder.EnvironmentInfo environment = new IssueReportBuilder.EnvironmentInfo(
            modVersion("dglabcraft"),
            SharedConstants.getCurrentVersion().getName(),
            "Forge",
            modVersion("forge"),
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
        return ModList.get().getModContainerById(modId)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown");
    }

    private Component statusKey(boolean enabled) {
        return new TranslatableComponent(enabled ? "status.dglabcraft.started" : "status.dglabcraft.stopped");
    }

    private Component bindStatus(WebSocketServerManager server) {
        if (server.isConnected()) {
            return new TranslatableComponent("status.dglabcraft.completed");
        }
        if (server.isWaitingForAppBind()) {
            return new TranslatableComponent("status.dglabcraft.waiting_confirmation");
        }
        return new TranslatableComponent("status.dglabcraft.incomplete");
    }

    private String formatWaveformDetail(WebSocketServerManager server, String channel) {
        String waveform = server.getChannelRuntimeWaveform(channel);
        long remainingMillis = server.getChannelRuntimeRemainingMillis(channel);
        if (waveform == null || waveform.isBlank()) {
            return "";
        }
        return ", waveform=" + waveform + ", remaining=" + formatSeconds(remainingMillis);
    }

    private int drawSection(PoseStack poseStack, int x, int y, Component title, List<Component> lines) {
        this.font.draw(poseStack, title, x, y, 0xFFFFFF);
        y += 12;
        for (Component line : lines) {
            List<FormattedCharSequence> wrapped = this.font.split(line, CONTENT_WIDTH);
            for (FormattedCharSequence textLine : wrapped) {
                this.font.draw(poseStack, textLine, x, y, 0xD0D0D0);
                y += 10;
            }
        }
        return y + 8;
    }

    private Component toPlainDetail(WebSocketServerManager.EffectSource source, String detail) {
        return switch (source) {
            case DAMAGE -> switch (normalize(detail)) {
                case "lava" -> new TranslatableComponent("effect.dglabcraft.damage.lava");
                case "onfire" -> new TranslatableComponent("effect.dglabcraft.damage.onfire");
                case "infire" -> new TranslatableComponent("effect.dglabcraft.damage.infire");
                case "hotfloor" -> new TranslatableComponent("effect.dglabcraft.damage.hotfloor");
                case "drown" -> new TranslatableComponent("effect.dglabcraft.damage.drown");
                case "freeze" -> new TranslatableComponent("effect.dglabcraft.damage.freeze");
                case "fall" -> new TranslatableComponent("effect.dglabcraft.damage.fall");
                case "mob_attack" -> new TranslatableComponent("effect.dglabcraft.damage.mob_attack");
                case "player_attack" -> new TranslatableComponent("effect.dglabcraft.damage.player_attack");
                case "explosion", "explosion.player" -> new TranslatableComponent("effect.dglabcraft.damage.explosion");
                default -> new TranslatableComponent("effect.dglabcraft.damage.default");
            };
            case HEARTBEAT -> new TranslatableComponent("effect.dglabcraft.heartbeat.low_health");
            case ENVIRONMENT -> switch (normalize(detail)) {
                case "nether" -> new TranslatableComponent("effect.dglabcraft.environment.nether");
                case "end" -> new TranslatableComponent("effect.dglabcraft.environment.end");
                case "portal" -> new TranslatableComponent("effect.dglabcraft.environment.portal");
                case "powder_snow" -> new TranslatableComponent("effect.dglabcraft.environment.powder_snow");
                default -> new TranslatableComponent("effect.dglabcraft.environment.default");
            };
            case NONE -> new TranslatableComponent("effect.dglabcraft.none");
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String formatSeconds(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        return new TranslatableComponent("duration.dglabcraft.seconds", seconds).getString();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}

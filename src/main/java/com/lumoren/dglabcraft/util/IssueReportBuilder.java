package com.lumoren.dglabcraft.util;

public final class IssueReportBuilder {
    private IssueReportBuilder() {
    }

    public record EnvironmentInfo(
        String modVersion,
        String minecraftVersion,
        String loaderName,
        String loaderVersion,
        String javaVersion,
        String osName,
        String osVersion,
        String websocketHost,
        int websocketPort,
        boolean serviceRunning,
        boolean socketConnected,
        boolean appBound,
        boolean waitingForBind,
        boolean syncChannels,
        boolean hudEnabled,
        String clientId,
        String targetId
    ) {
    }

    public record ChannelInfo(
        String channel,
        String status,
        int intensity,
        boolean runtimeActive,
        String source,
        String detail,
        String waveform,
        long remainingMillis
    ) {
    }

    public record RuntimeInfo(
        boolean syncRuntimeActive,
        String syncSource,
        String syncDetail,
        String syncWaveform,
        long syncRemainingMillis
    ) {
    }

    public static String build(EnvironmentInfo env, ChannelInfo channelA, ChannelInfo channelB, RuntimeInfo runtime) {
        StringBuilder builder = new StringBuilder();
        builder.append("## DGLab Craft Issue Info\n\n");
        builder.append("### Environment\n");
        appendLine(builder, "- Mod version", valueOrUnknown(env.modVersion()));
        appendLine(builder, "- Minecraft version", valueOrUnknown(env.minecraftVersion()));
        appendLine(builder, "- Loader", valueOrUnknown(env.loaderName()) + " " + valueOrUnknown(env.loaderVersion()));
        appendLine(builder, "- Java", valueOrUnknown(env.javaVersion()));
        appendLine(builder, "- OS", valueOrUnknown(env.osName()) + " " + valueOrUnknown(env.osVersion()));
        builder.append('\n');

        builder.append("### Connection\n");
        appendLine(builder, "- WebSocket", valueOrUnknown(env.websocketHost()) + ":" + env.websocketPort());
        appendLine(builder, "- Service running", yesNo(env.serviceRunning()));
        appendLine(builder, "- Socket connected", yesNo(env.socketConnected()));
        appendLine(builder, "- App bound", yesNo(env.appBound()));
        appendLine(builder, "- Waiting for bind", yesNo(env.waitingForBind()));
        appendLine(builder, "- Client ID", redactId(env.clientId()));
        appendLine(builder, "- Target ID", redactId(env.targetId()));
        builder.append('\n');

        builder.append("### Config\n");
        appendLine(builder, "- Sync channels", yesNo(env.syncChannels()));
        appendLine(builder, "- HUD enabled", yesNo(env.hudEnabled()));
        builder.append('\n');

        builder.append("### Runtime\n");
        appendLine(builder, "- Sync runtime active", yesNo(runtime.syncRuntimeActive()));
        appendLine(builder, "- Sync source", valueOrNone(runtime.syncSource()));
        appendLine(builder, "- Sync detail", valueOrNone(runtime.syncDetail()));
        appendLine(builder, "- Sync waveform", valueOrNone(runtime.syncWaveform()));
        appendLine(builder, "- Sync remaining", formatMillis(runtime.syncRemainingMillis()));
        appendChannel(builder, channelA);
        appendChannel(builder, channelB);
        return builder.toString();
    }

    public static String redactId(String id) {
        if (id == null || id.isBlank()) {
            return "absent";
        }
        String trimmed = id.trim();
        int visible = Math.min(4, trimmed.length());
        return "present (ending " + trimmed.substring(trimmed.length() - visible) + ")";
    }

    private static void appendChannel(StringBuilder builder, ChannelInfo channel) {
        builder.append('\n').append("#### Channel ").append(valueOrUnknown(channel.channel())).append('\n');
        appendLine(builder, "- Status", valueOrUnknown(channel.status()));
        appendLine(builder, "- Intensity", channel.intensity() + "%");
        appendLine(builder, "- Runtime active", yesNo(channel.runtimeActive()));
        appendLine(builder, "- Source", valueOrNone(channel.source()));
        appendLine(builder, "- Detail", valueOrNone(channel.detail()));
        appendLine(builder, "- Waveform", valueOrNone(channel.waveform()));
        appendLine(builder, "- Remaining", formatMillis(channel.remainingMillis()));
    }

    private static void appendLine(StringBuilder builder, String key, String value) {
        builder.append(key).append(": ").append(value).append('\n');
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value.trim();
    }

    private static String valueOrNone(String value) {
        return value == null || value.isBlank() ? "none" : value.trim();
    }

    private static String formatMillis(long millis) {
        return Math.max(0L, millis) + " ms";
    }
}

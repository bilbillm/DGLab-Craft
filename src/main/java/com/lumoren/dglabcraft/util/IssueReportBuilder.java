package com.lumoren.dglabcraft.util;

public final class IssueReportBuilder {
    private IssueReportBuilder() {
    }

    public static final class EnvironmentInfo {
        private final String modVersion;
        private final String minecraftVersion;
        private final String loaderName;
        private final String loaderVersion;
        private final String javaVersion;
        private final String osName;
        private final String osVersion;
        private final String websocketHost;
        private final int websocketPort;
        private final boolean serviceRunning;
        private final boolean socketConnected;
        private final boolean appBound;
        private final boolean waitingForBind;
        private final boolean syncChannels;
        private final boolean hudEnabled;
        private final String clientId;
        private final String targetId;

        public EnvironmentInfo(String modVersion, String minecraftVersion, String loaderName, String loaderVersion,
                               String javaVersion, String osName, String osVersion, String websocketHost,
                               int websocketPort, boolean serviceRunning, boolean socketConnected, boolean appBound,
                               boolean waitingForBind, boolean syncChannels, boolean hudEnabled,
                               String clientId, String targetId) {
            this.modVersion = modVersion;
            this.minecraftVersion = minecraftVersion;
            this.loaderName = loaderName;
            this.loaderVersion = loaderVersion;
            this.javaVersion = javaVersion;
            this.osName = osName;
            this.osVersion = osVersion;
            this.websocketHost = websocketHost;
            this.websocketPort = websocketPort;
            this.serviceRunning = serviceRunning;
            this.socketConnected = socketConnected;
            this.appBound = appBound;
            this.waitingForBind = waitingForBind;
            this.syncChannels = syncChannels;
            this.hudEnabled = hudEnabled;
            this.clientId = clientId;
            this.targetId = targetId;
        }

        public String modVersion() { return modVersion; }
        public String minecraftVersion() { return minecraftVersion; }
        public String loaderName() { return loaderName; }
        public String loaderVersion() { return loaderVersion; }
        public String javaVersion() { return javaVersion; }
        public String osName() { return osName; }
        public String osVersion() { return osVersion; }
        public String websocketHost() { return websocketHost; }
        public int websocketPort() { return websocketPort; }
        public boolean serviceRunning() { return serviceRunning; }
        public boolean socketConnected() { return socketConnected; }
        public boolean appBound() { return appBound; }
        public boolean waitingForBind() { return waitingForBind; }
        public boolean syncChannels() { return syncChannels; }
        public boolean hudEnabled() { return hudEnabled; }
        public String clientId() { return clientId; }
        public String targetId() { return targetId; }
    }

    public static final class ChannelInfo {
        private final String channel;
        private final String status;
        private final int intensity;
        private final boolean runtimeActive;
        private final String source;
        private final String detail;
        private final String waveform;
        private final long remainingMillis;

        public ChannelInfo(String channel, String status, int intensity, boolean runtimeActive, String source,
                           String detail, String waveform, long remainingMillis) {
            this.channel = channel;
            this.status = status;
            this.intensity = intensity;
            this.runtimeActive = runtimeActive;
            this.source = source;
            this.detail = detail;
            this.waveform = waveform;
            this.remainingMillis = remainingMillis;
        }

        public String channel() { return channel; }
        public String status() { return status; }
        public int intensity() { return intensity; }
        public boolean runtimeActive() { return runtimeActive; }
        public String source() { return source; }
        public String detail() { return detail; }
        public String waveform() { return waveform; }
        public long remainingMillis() { return remainingMillis; }
    }

    public static final class RuntimeInfo {
        private final boolean syncRuntimeActive;
        private final String syncSource;
        private final String syncDetail;
        private final String syncWaveform;
        private final long syncRemainingMillis;

        public RuntimeInfo(boolean syncRuntimeActive, String syncSource, String syncDetail,
                           String syncWaveform, long syncRemainingMillis) {
            this.syncRuntimeActive = syncRuntimeActive;
            this.syncSource = syncSource;
            this.syncDetail = syncDetail;
            this.syncWaveform = syncWaveform;
            this.syncRemainingMillis = syncRemainingMillis;
        }

        public boolean syncRuntimeActive() { return syncRuntimeActive; }
        public String syncSource() { return syncSource; }
        public String syncDetail() { return syncDetail; }
        public String syncWaveform() { return syncWaveform; }
        public long syncRemainingMillis() { return syncRemainingMillis; }
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
        if (id == null || id.trim().isEmpty()) {
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
        return value == null || value.trim().isEmpty() ? "unknown" : value.trim();
    }

    private static String valueOrNone(String value) {
        return value == null || value.trim().isEmpty() ? "none" : value.trim();
    }

    private static String formatMillis(long millis) {
        return Math.max(0L, millis) + " ms";
    }
}

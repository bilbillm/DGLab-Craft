package com.lumoren.dglabcraft.network;

final class PulsePreviewState {
    private String waveformId = "";
    private long leaseUntilAt = 0L;

    void record(String waveformId, long nowMillis, long leaseMillis) {
        if (!isPulseWaveform(waveformId) || leaseMillis <= 0L) {
            clear();
            return;
        }
        this.waveformId = waveformId;
        this.leaseUntilAt = nowMillis + leaseMillis;
    }

    boolean isActive(long nowMillis) {
        return !waveformId.trim().isEmpty() && leaseUntilAt > nowMillis;
    }

    String waveformId() {
        return waveformId;
    }

    long remainingMillis(long nowMillis) {
        return Math.max(0L, leaseUntilAt - nowMillis);
    }

    void clear() {
        waveformId = "";
        leaseUntilAt = 0L;
    }

    private static boolean isPulseWaveform(String waveformId) {
        return waveformId != null
            && !waveformId.trim().isEmpty()
            && !"Idle".equalsIgnoreCase(waveformId)
            && !"increase".equalsIgnoreCase(waveformId)
            && !"decrease".equalsIgnoreCase(waveformId);
    }
}

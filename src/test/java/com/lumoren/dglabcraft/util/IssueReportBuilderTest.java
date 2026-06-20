package com.lumoren.dglabcraft.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueReportBuilderTest {
    @Test
    void issueReportContainsUsefulFieldsAndRedactsIds() {
        String report = IssueReportBuilder.build(
            new IssueReportBuilder.EnvironmentInfo(
                "1.0.10",
                "1.20.1",
                "Forge",
                "47.2.0",
                "17.0.10",
                "Windows 11",
                "10.0",
                "192.168.1.20",
                8877,
                true,
                true,
                true,
                false,
                true,
                false,
                "1234-123456789-12345-12345-01",
                "abcdef1234567890"
            ),
            new IssueReportBuilder.ChannelInfo("A", "busy", 35, true, "DAMAGE", "lava", "burn", 1200),
            new IssueReportBuilder.ChannelInfo("B", "idle", 0, false, "NONE", "", "", 0),
            new IssueReportBuilder.RuntimeInfo(true, "DAMAGE", "lava", "burn", 1200)
        );

        assertTrue(report.contains("Mod version: 1.0.10"));
        assertTrue(report.contains("Minecraft version: 1.20.1"));
        assertTrue(report.contains("Loader: Forge 47.2.0"));
        assertTrue(report.contains("WebSocket: 192.168.1.20:8877"));
        assertTrue(report.contains("Sync channels: yes"));
        assertTrue(report.contains("HUD enabled: no"));
        assertTrue(report.contains("#### Channel A"));
        assertTrue(report.contains("Waveform: burn"));
        assertTrue(report.contains("present (ending 5-01)"));
        assertTrue(report.contains("present (ending 7890)"));
        assertFalse(report.contains("1234-123456789-12345-12345-01"));
        assertFalse(report.contains("abcdef1234567890"));
        assertFalse(report.toLowerCase().contains("http://"));
        assertFalse(report.toLowerCase().contains("https://"));
    }

    @Test
    void blankIdsAreReportedAsAbsent() {
        assertTrue(IssueReportBuilder.redactId(null).contains("absent"));
        assertTrue(IssueReportBuilder.redactId("").contains("absent"));
        assertTrue(IssueReportBuilder.redactId("   ").contains("absent"));
    }
}

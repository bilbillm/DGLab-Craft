package com.lumoren.dglabcraft.util;

public final class IssueReportBuilder {
    private IssueReportBuilder() {
    }

    public static String redactId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "absent";
        }
        String trimmed = id.trim();
        if (trimmed.length() <= 4) {
            return "present";
        }
        return "present(... " + trimmed.substring(trimmed.length() - 4) + ")";
    }

    public static String unknownIfBlank(String value) {
        return value == null || value.trim().isEmpty() ? "unknown" : value.trim();
    }
}

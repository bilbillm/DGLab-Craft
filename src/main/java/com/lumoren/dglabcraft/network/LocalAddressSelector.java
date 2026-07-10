package com.lumoren.dglabcraft.network;

import java.util.List;

final class LocalAddressSelector {
    private LocalAddressSelector() {
    }

    static String chooseBestLocalIpAddress(List<Candidate> candidates) {
        Candidate best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Candidate candidate : candidates) {
            int score = score(candidate);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best == null ? null : best.ip();
    }

    private static int score(Candidate candidate) {
        int score = 0;
        String ip = candidate.ip();
        if (ip.startsWith("192.168.")) {
            score += 40;
        } else if (ip.startsWith("10.")) {
            score += 35;
        } else if (ip.matches("^172\\.(1[6-9]|2[0-9]|3[01])\\..*")) {
            score += 35;
        }
        if (!candidate.virtualInterface()) {
            score += 20;
        }
        if (candidate.supportsMulticast()) {
            score += 5;
        }
        String name = (candidate.name() + " " + candidate.displayName()).toLowerCase();
        if (name.contains("virtual") || name.contains("vmware") || name.contains("virtualbox") || name.contains("hyper-v")) {
            score -= 30;
        }
        if (name.contains("wlan") || name.contains("wi-fi") || name.contains("ethernet")) {
            score += 5;
        }
        return score;
    }

    static final class Candidate {
        private final String ip;
        private final String name;
        private final String displayName;
        private final boolean virtualInterface;
        private final boolean supportsMulticast;

        Candidate(String ip, String name, String displayName, boolean virtualInterface, boolean supportsMulticast) {
            this.ip = ip;
            this.name = name == null ? "" : name;
            this.displayName = displayName == null ? "" : displayName;
            this.virtualInterface = virtualInterface;
            this.supportsMulticast = supportsMulticast;
        }

        String ip() { return ip; }
        String name() { return name; }
        String displayName() { return displayName; }
        boolean virtualInterface() { return virtualInterface; }
        boolean supportsMulticast() { return supportsMulticast; }
    }
}

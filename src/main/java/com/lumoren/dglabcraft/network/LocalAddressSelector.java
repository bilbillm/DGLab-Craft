package com.lumoren.dglabcraft.network;

import java.util.Comparator;
import java.util.List;

final class LocalAddressSelector {
    private LocalAddressSelector() {
    }

    static String chooseBestLocalIpAddress(List<Candidate> candidates) {
        return candidates.stream()
            .filter(candidate -> isPrivateIpv4(candidate.ip()))
            .min(Comparator.comparingInt(LocalAddressSelector::score))
            .map(Candidate::ip)
            .orElse(null);
    }

    private static int score(Candidate candidate) {
        int score = 0;
        if (candidate.virtualInterface()) {
            score += 100;
        }
        if (!candidate.supportsMulticast()) {
            score += 10;
        }
        String name = (candidate.name() + " " + candidate.displayName()).toLowerCase();
        if (name.contains("vmware") || name.contains("virtualbox") || name.contains("hyper-v") || name.contains("wsl")) {
            score += 100;
        }
        return score;
    }

    static boolean isPrivateIpv4(String ip) {
        if (ip == null || ip.equals("127.0.0.1")) {
            return false;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    record Candidate(String ip, String name, String displayName, boolean virtualInterface, boolean supportsMulticast) {
    }
}

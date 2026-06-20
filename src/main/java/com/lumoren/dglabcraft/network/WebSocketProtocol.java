package com.lumoren.dglabcraft.network;

import java.util.List;

final class WebSocketProtocol {
    private WebSocketProtocol() {
    }

    static String normalizeChannel(String channel) {
        return "A".equalsIgnoreCase(channel) ? "A" : "B";
    }

    static int channelNumber(String channel) {
        return "A".equalsIgnoreCase(channel) ? 1 : 2;
    }

    static String clearCommand(int channelNumber) {
        return "clear-" + channelNumber;
    }

    static String strengthCommand(int channelNumber, int intensity) {
        return "strength-" + channelNumber + "+2+" + intensity;
    }

    static String pulseCommand(String channel, List<String> chunk) {
        StringBuilder hexArray = new StringBuilder("[");
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) {
                hexArray.append(",");
            }
            String hex = chunk.get(i).toUpperCase();
            while (hex.length() < 16) {
                hex = "0" + hex;
            }
            if (hex.length() > 16) {
                hex = hex.substring(0, 16);
            }
            hexArray.append("\"").append(hex).append("\"");
        }
        hexArray.append("]");
        return "pulse-" + normalizeChannel(channel) + ":" + hexArray;
    }

    static int priorityOf(WebSocketServerManager.EffectSource source) {
        return switch (source) {
            case DAMAGE -> 3;
            case HEARTBEAT -> 2;
            case ENVIRONMENT -> 1;
            case NONE -> -1;
        };
    }

    static int leaseTicks(WebSocketServerManager.EffectSource source, String detail) {
        String normalizedDetail = detail == null ? "" : detail.toLowerCase();
        return switch (source) {
            case HEARTBEAT -> 55;
            case ENVIRONMENT -> switch (normalizedDetail) {
                case "portal", "powder_snow" -> 40;
                case "nether", "end" -> 55;
                default -> 45;
            };
            case DAMAGE -> switch (normalizedDetail) {
                case "onfire", "infire", "lava", "hotfloor", "drown", "freeze" -> 30;
                default -> 12;
            };
            case NONE -> 0;
        };
    }

    static StrengthLimits parseStrengthLimits(String message, int currentA, int currentB) {
        if (message == null || !message.startsWith("strength-")) {
            return new StrengthLimits(currentA, currentB);
        }

        String[] parts = message.substring(9).split("\\+");
        try {
            if (parts.length >= 4) {
                return new StrengthLimits(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            }
            if (parts.length >= 3) {
                int channel = Integer.parseInt(parts[0]);
                int value = Integer.parseInt(parts[2]);
                if (channel == 1) {
                    return new StrengthLimits(value, currentB);
                }
                if (channel == 2) {
                    return new StrengthLimits(currentA, value);
                }
            }
        } catch (NumberFormatException ignored) {
            return new StrengthLimits(currentA, currentB);
        }

        return new StrengthLimits(currentA, currentB);
    }

    record StrengthLimits(int channelA, int channelB) {
    }
}

# DGLab Craft — Minecraft 1.18.2 Fabric

[中文](README.md) · [GitHub Releases](https://github.com/bilbillm/DGLab-Craft/releases) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dglab-craft)

Haptic feedback for damage, low health and environmental events.

- Branch: `1.18.2-Fabric`; mod version: `1.1.4`.
- Runtime: Minecraft `1.18.2`, Fabric 0.19.3 / Fabric API 0.77.0+1.18.2, Java `17`.
- Download: [DGLabCraft-1.18.2-1.1.4-Fabric.jar](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.18.2-Fabric). Download the JAR, not a source archive.

## Installation and connection

1. Install the exact Minecraft and Fabric version above. Install Fabric API `0.77.0+1.18.2` as well.
2. Put `DGLabCraft-1.18.2-1.1.4-Fabric.jar` in the selected instance's `mods` directory. Remove duplicate old versions.
3. Enter a world and open DGLab Craft with its configured key (default `K`). Only the client needs the device-control mod; a normal multiplayer server does not.
4. Keep the phone and game client on the same LAN. Scan the in-game QR code using SOCKET control in DGLab App 3.x. The public game-server/FRP address is not the device endpoint.
5. For connection failures, check Java firewall access, port `8877`, network isolation, and virtual adapters. Set the client's real LAN IPv4 in connection settings when necessary. Opening the QR image/folder remains available as a fallback.

## Compatibility

| Target | Status |
| --- | --- |
| DG-LAB 3.0 hardware with DGLab App 3.x SOCKET control | Supported route |
| DGLab App 4.0 | Not implemented |
| DG-LAB 2.0 / other hardware brands | Unverified |
| Bedrock / NetEase editions | Unsupported |
| Android FCL/Pojav Java launchers | Community reports only; no full mobile validation |

Remote damage feedback observes actual client health changes. Exact damage types that are not synchronized by the server are inferred locally; arbitrary modded sources may fall back to a generic waveform.

## Troubleshooting

Check Controls for key conflicts, the selected instance's loader/API, and duplicate JARs. `latest.log` records `Registered key binding`; conflicts record `Key binding conflict` and display a warning after entering a world. Include MC, loader, mod, Java and App versions, launcher, game mode and `latest.log` when reporting problems. Use the runtime Java listed above rather than downgrading all versions to Java 17.

Set the heartbeat threshold to `0` to disable heartbeat. Disable the corresponding environment multipliers for damage-only feedback. Waveform preview is available; the custom waveform editor/import UI is still pending.

## Versions

| Minecraft | Forge | NeoForge | Fabric |
| --- | --- | --- | --- |
| 1.7.10 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.7.10-Forge) | — | — |
| 1.8.9 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.8.9-Forge) beta | — | — |
| 1.12.2 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.12.2-Forge) | — | — |
| 1.16.5 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.16.5-Forge) | — | [1.1.2](https://github.com/bilbillm/DGLab-Craft/releases) |
| 1.17.1 | [1.1.1](https://github.com/bilbillm/DGLab-Craft/releases) | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.17.1-Fabric) |
| 1.18.2 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.18.2-Forge) | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.18.2-Fabric) |
| 1.19.2 | [1.1.1](https://github.com/bilbillm/DGLab-Craft/releases) | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.19.2-Fabric) |
| 1.20.1 | [1.1.1](https://github.com/bilbillm/DGLab-Craft/releases) | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.20.1-Fabric) |
| 1.21.1 | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.1-Forge) | [1.1.1](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.1-Fabric) |
| 1.21.4 | — | [1.1.1](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.4-Fabric) |
| 1.21.8 | — | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.8-Fabric) |
| 1.21.11 | — | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.11-NeoForge) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.11-Fabric) |
| 26.1 | — | [1.1.2](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1-Fabric) |
| 26.1.1 | — | [1.1.2](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1.1-Fabric) |
| 26.1.2 | — | [1.1.2](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1.2-Fabric) |
| 26.2 | — | [1.1.2](https://github.com/bilbillm/DGLab-Craft/releases) | [1.1.4](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.2-Fabric) |


## Build

Use JDK `21` to launch Gradle, then run `./gradlew build`. Installable artifacts are in `build/libs/`. This branch is licensed under GPL-3.0.

# DGLab Craft

[中文说明](./README.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-43.5.0+-orange)
![Java](https://img.shields.io/badge/Java-17-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> Branch target: `1.19.2`

DGLab Craft is a Minecraft Forge mod that starts a local WebSocket service and converts in-game damage, environment changes, and low-health states into DGLab device feedback.

## Branch Scope

This branch is specifically for:

- Minecraft `1.19.2`
- Forge `43.5.0+`
- Java `17`

If you need another version:

- use branch `1.20.1` for Minecraft 1.20.1 Forge
- use branch `1.21.1-NeoForge` for Minecraft 1.21.1 NeoForge

## Features

- Low-health heartbeat feedback
- Environment feedback (Nether, End, portal, powder snow, etc.)
- Damage feedback with waveform mapping by damage source
- A/B channel sync or split behavior
- QR-code based DGLab App connection
- Fade-out handling after stimulus ends

## Requirements

- Minecraft `1.19.2`
- Forge `43.5.0` or newer within the same major line
- Java `17`
- DGLab device and mobile app

## Installation

1. Download the `.jar` built for this branch
2. Put it into `.minecraft/mods`
3. Launch the game with Forge 1.19.2

## Quick Start

1. Enter a single-player world or multiplayer server
2. Press `K` to open the settings screen
3. Open the connection screen and generate the QR code
4. Use the DGLab App SOCKET mode to scan the QR code
5. Make sure the phone and PC are on the same LAN

## Main Configuration

You can configure:

- Global intensity limit
- Heartbeat trigger threshold
- Environment intensity settings
- Damage multipliers
- HUD visibility and position
- A/B channel sync
- WebSocket port

## Build

```bash
git clone https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

To generate IDE run configs:

```bash
./gradlew genEclipseRuns
# or
./gradlew genIntellijRuns
```

Artifacts are generated in `build/libs/`.

## Notes

- This is the legacy Forge 1.19.2 line
- Its documentation only applies to this branch
- Do not mix this README or its artifacts with the 1.20.1 or 1.21.1 branches

## Bug Reports

When reporting issues, please include:

- branch / version
- Minecraft and Forge version
- Java version
- `latest.log`
- whether the problem happens in single-player, multiplayer, or a modpack

## Credits

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## License

GPL 3.0

## Author

Lumoren

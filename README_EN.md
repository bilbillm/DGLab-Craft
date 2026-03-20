# DGLab Craft

[中文说明](./README.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen)
![Forge](https://img.shields.io/badge/Forge-47.2.0+-orange)
![Java](https://img.shields.io/badge/Java-17-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> Branch target: `1.20.1`

DGLab Craft is a Minecraft Forge mod that connects to the DGLab App through a local WebSocket server and converts in-game damage, environment changes, and low-health states into device feedback.

## Branch Scope

This branch is specifically for:

- Minecraft `1.20.1`
- Forge `47.2.0+`
- Java `17`

If you need another version:

- use branch `1.19.2` for Minecraft 1.19.2 Forge
- use branch `1.21.1-NeoForge` for Minecraft 1.21.1 NeoForge

## Features

- Low-health heartbeat feedback
- Environment feedback (Nether, End, portal, powder snow, etc.)
- Damage feedback with waveform mapping by damage source
- A/B channel sync or split behavior
- QR-code based DGLab App connection
- Fade-out handling after stimulus ends

## Requirements

- Minecraft `1.20.1`
- Forge `47.2.0` or newer in the same major line
- Java `17`
- DGLab device and mobile app

## Installation

1. Download the `.jar` built for this branch
2. Put it into `.minecraft/mods`
3. Launch the game with Forge 1.20.1

## Quick Start

1. Enter a single-player world or a multiplayer server
2. Press `K` to open the settings screen
3. Generate the QR code from the connection screen
4. Use the DGLab App SOCKET mode to scan the QR code
5. Make sure your phone and PC are on the same LAN

## Branch Notes

The 1.20.1 line already contains several compatibility-focused fixes, including:

- multiplayer client damage feedback fixes
- health-delta based damage triggering in problematic environments
- iOS pulse payload formatting compatibility fixes

When reporting bugs, it is especially helpful to mention whether the problem happens in:

- a modpack
- multiplayer client mode
- iOS or Android

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

- This branch is only for Forge 1.20.1
- Its documentation does not apply to the NeoForge 1.21.1 branch
- Real-world verification is still recommended for large modpacks, multiplayer setups, and device-specific edge cases

## Bug Reports

Please include:

- branch / mod version
- Minecraft / Forge version
- Java version
- device platform (iOS / Android)
- `latest.log`
- whether the problem happens in single-player, multiplayer, or a modpack

## Credits

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## License

GPL 3.0

## Author

Lumoren

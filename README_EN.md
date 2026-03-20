# DGLab Craft

[中文说明](./README.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.61+-orange)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> Branch target: `1.21.1-NeoForge`

DGLab Craft is a Minecraft NeoForge mod that starts a local WebSocket service and converts damage, environment changes, and low-health states into DGLab device feedback.

## Branch Scope

This branch is specifically for:

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`

If you need another version:

- use branch `1.19.2` for Minecraft 1.19.2 Forge
- use branch `1.20.1` for Minecraft 1.20.1 Forge

## Branch Highlights

This is the modern NeoForge line of the project. It includes:

- migration from Forge to NeoForge
- Java 21 runtime alignment
- updated UI/config implementation for 1.21.1
- maintained fixes for multiplayer and modpack damage-feedback behavior

## Features

- Low-health heartbeat feedback
- Environment feedback (Nether, End, portal, powder snow, etc.)
- Damage feedback with waveform mapping by damage source
- A/B channel sync or split behavior
- QR-code based DGLab App connection
- Fade-out handling after stimulus ends

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.61` or newer in the same major line
- Java `21`
- DGLab device and mobile app

## Installation

1. Download the `.jar` built for this branch
2. Put it into `.minecraft/mods`
3. Launch the game with NeoForge 1.21.1

## Quick Start

1. Enter a single-player world or multiplayer server
2. Press `K` to open the main UI
3. Open the connection screen and generate the QR code
4. Use the DGLab App SOCKET mode to scan the QR code
5. Make sure the phone and PC are on the same LAN

## Current Branch Notes

The 1.21.1-NeoForge branch is currently focused on:

- NeoForge compatibility
- connection-path stability
- multiplayer and modpack damage-feedback fixes

When reporting bugs for this branch, it helps to include whether you are using:

- a clean NeoForge setup or a modpack
- single-player or multiplayer client mode
- iOS or Android
- the exact Java version and launcher

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

This branch uses the NeoForge / ModDevGradle build flow.

```bash
git clone https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

Artifacts are generated in `build/libs/`.

## Notes

- This branch does not target legacy Forge
- Java 21 is required at runtime
- This documentation only applies to the 1.21.1 NeoForge branch

## Bug Reports

Please include:

- branch / mod version
- Minecraft / NeoForge version
- Java version
- `latest.log`
- whether the issue happens in single-player, multiplayer, or a modpack

## Credits

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## License

GPL 3.0

## Author

Lumoren

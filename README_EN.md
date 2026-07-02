# DGLab Craft

<p align="center"><img src="docs/logo.jpg" alt="DGLab Craft" width="200"></p>

[中文说明](./README.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.61+-orange)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

DGLab Craft is a Minecraft mod that converts in-game damage, low-health states, and environment changes into DGLab device feedback. It connects to the DGLab mobile app through a local WebSocket server.

Current branch: `1.21.1-NeoForge`
Target version: Minecraft `1.21.1` / NeoForge `21.1.61` / Java `21`

> Note: use the jar that exactly matches your Minecraft version and loader. Do not mix Minecraft, Forge, or NeoForge builds.

Download pages:

- [GitHub Releases](https://github.com/bilbillm/DGLab-Craft/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dglab-craft)

## Part 1: Install and Play

### 1. Check your Minecraft version first

This branch only targets:

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`

If your Minecraft version is different, use the matching branch and release below. Do not mix jars built for different Minecraft versions.

| Minecraft | Loader | Branch | Latest release |
| --- | --- | --- | --- |
| 1.18.2 | Forge 40.2.21 | `1.18.2` | [v1.0.7](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.7-1.18.2) |
| 1.19.2 | Forge 43.5.0 | `1.19.2` | [v1.0.7](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.7-1.19.2) |
| 1.20.1 | Forge 47.x | `1.20.1` | [v1.0.11](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.11-1.20.1) |
| 1.21.1 | NeoForge 21.1.x | `1.21.1-NeoForge` | [v1.0.8](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.8-1.21.1-NeoForge) |
| 1.21.4 | NeoForge 21.4.x | `1.21.4-NeoForge` | [v1.0.8](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.8-1.21.4-NeoForge) |

### 2. Download the Mod from GitHub

1. Open this release: [`DGLabCraft-1.21.1-1.0.8.jar`](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.8-1.21.1-NeoForge)
2. Scroll to **Assets**.
3. Download `DGLabCraft-1.21.1-1.0.8.jar`.
4. Do not download `Source code.zip` or `Source code.tar.gz` for normal play. Those are source packages for developers and will not work as a mod jar.

You can also start from the project [Releases page](https://github.com/bilbillm/DGLab-Craft/releases) and choose the release that matches your Minecraft version.

### 3. Install it into Minecraft

1. Create or select a Minecraft `1.21.1` instance in your launcher.
2. Install NeoForge `21.1.61+` for that instance.
3. Open the instance's `mods` folder. A common path is `.minecraft/mods`.
4. Put `DGLabCraft-1.21.1-1.0.8.jar` into that folder.
5. Start the game and check the Mods screen for `DGLab Craft`.

If you cannot find the instance folder, most launchers provide an “Open Folder” or “Open Game Directory” action for each instance.

### 4. Connect the DGLab App for the first time

1. Enter a single-player world or server.
2. Press `K` to open the DGLab Craft screen.
3. Open the connection screen and generate the QR code.
4. Open the DGLab mobile app and use SOCKET / QR connection.
5. Your phone and PC must be on the same LAN, usually the same Wi-Fi.

If connection fails, check these first:

- The phone and PC are on the same Wi-Fi.
- Windows Firewall is not blocking Java or Minecraft.
- The shown IP looks like a LAN address, such as `192.168.x.x`, `10.x.x.x`, or `172.16-31.x.x`.
- If a virtual-adapter IP is shown, manually set the PC's real LAN IP in the config.

### 5. What the mod reacts to

- Low-health heartbeat feedback
- Damage feedback with waveform mapping by damage source
- Nether, End, portal, powder snow, and other environment feedback
- A/B channel sync or split control
- Fade-out after feedback ends
- In-game HUD and connection state display

## Part 2: Developers and Maintainers

### Branch target

This branch maintains:

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`
- Mod version `1.0.8`

When maintaining multiple versions, put fixes on the matching Minecraft branch first. Do not blindly copy Forge and NeoForge code between branches because their events, registration APIs, and build flows differ.

### Build

```bash
git clone -b 1.21.1-NeoForge https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

Artifacts are generated in `build/libs/`.

For release uploads, use the full jar, normally the one without `-slim` in its filename.

Release checks:

```bash
pwsh scripts/release-check.ps1 -Tag v1.0.8-1.21.1-NeoForge -SkipGitHubReleaseCheck
pwsh scripts/release-postcheck.ps1 -Tag v1.0.8-1.21.1-NeoForge
```

You can manually run the `Release Rehearsal` GitHub Action to validate release metadata and build output without creating a GitHub Release or uploading to CurseForge.

### Maintained branches

| Minecraft | Loader | Branch | Latest release |
| --- | --- | --- | --- |
| 1.18.2 | Forge 40.2.21 | `1.18.2` | [v1.0.7](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.7-1.18.2) |
| 1.19.2 | Forge 43.5.0 | `1.19.2` | [v1.0.7](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.7-1.19.2) |
| 1.20.1 | Forge 47.x | `1.20.1` | [v1.0.11](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.11-1.20.1) |
| 1.21.1 | NeoForge 21.1.x | `1.21.1-NeoForge` | [v1.0.8](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.8-1.21.1-NeoForge) |
| 1.21.4 | NeoForge 21.4.x | `1.21.4-NeoForge` | [v1.0.8](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.0.8-1.21.4-NeoForge) |

### Useful bug report details

Ask users to include:

- the full text from the diagnostics page Copy issue info button
- Minecraft version
- Forge / NeoForge version
- DGLab Craft version
- Java version
- single-player, multiplayer, or modpack environment
- iOS or Android DGLab App
- `latest.log`
- for connection issues, the IP and port shown by the connection screen

### Common maintenance checks

- Keep `gradle.properties` Minecraft, loader, and mod versions aligned with the release.
- Keep `META-INF/mods.toml` or NeoForge metadata version ranges aligned with the branch.
- Keep README download links and jar filenames aligned with the latest release.
- For connection bugs, first check whether WebSocket started and whether the QR code uses a reachable LAN IP.

## License

GPL 3.0

## Credits

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## Author

Lumoren

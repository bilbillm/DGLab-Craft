# DGLab Craft

DGLab Craft converts Minecraft damage, low-health states, and environment changes into DGLab device feedback through a local WebSocket connection to the DGLab mobile app.

## Supported Versions

| Minecraft | Loader | Java | Release |
| --- | --- | --- | --- |
| 1.18.2 | Forge 40.2.21 | 17 | v1.0.7 |
| 1.19.2 | Forge 43.5.0 | 17 | v1.0.7 |
| 1.20.1 | Forge 47.x | 17 | v1.0.11 |
| 1.21.1 | NeoForge 21.1.x | 21 | v1.0.7 |
| 1.21.4 | NeoForge 21.4.x | 21 | v1.0.8 |

Install the file that exactly matches your Minecraft version and loader. Do not mix Forge and NeoForge builds.

## Features

- Damage feedback with waveform mapping by damage source
- Low-health heartbeat feedback
- Nether, End, portal, powder snow, and other environment feedback
- A/B channel sync or split control
- In-game HUD and connection diagnostics
- One-click diagnostic info copy for GitHub issue reports

## Connection Notes

The DGLab mobile app and Minecraft client must be on the same LAN. If the QR code cannot bind, check Windows Firewall and confirm the shown address is a reachable LAN IP such as `192.168.x.x`, `10.x.x.x`, or `172.16-31.x.x`.

For bugs, open the in-game diagnostics page, click "Copy issue info", and paste it into the GitHub bug report template.

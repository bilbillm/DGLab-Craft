# DGLab Craft

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-43.5.0-orange)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> **Topics:** `minecraft-mod` `forge-mod` `dglab` `websocket` `haptic-feedback` `minecraft-1-19-2`

DGLab Craft is a Minecraft Forge mod that connects to the DGLab App via WebSocket, converting in-game events into physical feedback sent to DGLab devices.

## Before You Start

This is a Forge mod. The popular one on Bilibili was for Fabric, and our ideas about the "electric play" experience are quite different (lol). To understand the difference, download it and try it out~

I'll probably write about the differences between the two mods tomorrow.

Developing this mod was quite a journey - I worked on it for four consecutive days and finally finished it at 2 AM. Some testing may not be comprehensive, so please feel free to report bugs via issues. This was my first time working with Java and WebSocket servers 😂 - I had to dig through documentation and ask AI for help. Pull requests with better, more elegant code and algorithms are very welcome! I might record a video to promote it on Bilibili later, and add some GIFs and screenshots to improve the README~

## 📖 Beginner's Guide: Your First "Shocking" Journey

If this is your first time installing a Minecraft mod, don't worry! Follow these simple steps, and you'll be experiencing unprecedented haptic feedback in just a few minutes.

### ⚠️ Step 1: Preparation (What you need)
Before we start, ensure you have the following ready:
1. **Minecraft 1.19.2** installed.
2. **Forge Mod Loader** (Version **43.5.0** or higher).
   * *Pro-tip: Third-party launchers like Prism Launcher or CurseForge make installing Forge a 1-click breeze!*
3. Your **DGLab Coyote device** and the **DGLab App** installed on your smartphone.

### 📥 Step 2: Installation
1. Download the latest **DGLab Craft** mod file (`.jar` extension) from the [`Releases`](https://github.com/bilbillm/DGLab-Craft/releases/) page on this repository.
2. Locate your Minecraft game directory:
   * Official Launcher: Press `Win + R`, type `%appdata%\.minecraft`, and hit Enter.
   * Third-party Launchers: Usually located within the specific instance's folder.
3. Find the `mods` folder in your game directory (if it doesn't exist, simply create a new folder named `mods`).
4. Drag and drop the downloaded `.jar` file into this folder. Done!

### 🔌 Step 3: Connect Your DGLab Device
1. Open your launcher and start the game using your **Forge 1.19.2** profile.
2. Enter any single-player world or join a multiplayer server.
3. Press the **`K` key** (default shortcut) on your keyboard to open the mod's main settings menu.
4. Click the **"Connect Device" (连接设备)→Refresh QR Code刷新二维码→Open QR ode打开二维码** button, and a QR code will pop up on your screen.
5. Open the **DGLab App** on your phone, conact the socket,then tap the scan icon , and scan the QR code on your monitor.
   * *Note: Make sure your PC and phone are connected to the network. If scanning fails, click "Refresh QR Code" in-game and try again.*
6. Once successfully scanned, the App will connect to the WebSocket server, and your in-game HUD will proudly display "Connected"!

### ⚙️ Step 4: Safety First! Customize Your Intensity
**[STRONGLY RECOMMENDED] Before you dive into a pool of lava or hug a Creeper, please adjust your intensity settings!**

Press the **`K` key** in-game to bring up the settings menu, where you'll find plenty of customization options:
* **Global Intensity Limit:** This is your safety lock. For your first run, it's highly recommended to set this to **20%~30%**. You can gradually increase it later based on your tolerance.
* **Heartbeat Threshold:** By default, when your health drops below 30%, the device will simulate a heartbeat waveform. You can freely adjust this trigger threshold.
* **Damage Multipliers:** Think lava isn't stimulating enough? Or cactus pricks are too sharp? You can individually tweak the intensity multiplier for specific types of damage (e.g., sharp, explosive, fall damage).
* **Channel Routing:** By default, the mod assigns different waveforms to Channel A and Channel B. You can enable "A/B Channel Sync" for a more uniform experience across both channels.

> **💡 Gameplay Tip:**
> The mod features a built-in "Fade" effect. When you escape danger (like crawling out of lava), the stimulation won't cut off abruptly. Instead, it smoothly transitions to 0 over 2 seconds, providing a highly realistic lingering sensation. Have fun (and stay safe) in your Minecraft world!

#### 1. Design Philosophy of "Shock Play": Sandbox Customization vs. Immersive Simulation
* **CaiJi-ikun Version (Sandbox Customization):** Focuses on providing players with a high degree of freedom and customization. Players can act like they are operating a mixing console, deciding for themselves which game events correspond to which waveforms and parameters. It provides a highly flexible underlying control surface.
* **DGLab Craft (Immersive Simulation):** This mod focuses on an out-of-the-box, immersive scenario experience. Under the hood, we have pre-bound in-game survival states to specific physical feedback. For example, damage is subdivided into different dimensions of sensation, such as sharp weapons (Fast Pinch), blunt force (Beat), high temperature (Burn), and magic (Tide). It also introduces ambient environment feedback (like the Nether and the End) and a physiological heartbeat simulation when health drops below 30%.

#### 2. Channel Scheduling Strategy: Free Combination vs. Preset Scheduling
The two mods also handle the utilization of the Coyote device's dual channels differently:
* **CaiJi-ikun Version:** Channel allocation is primarily left to the player to plan and combine freely within their custom configurations.
* **DGLab Craft:** Adopts a preset, scenario-based channel scheduling scheme. By default (in asynchronous mode), the mod tends to assign "ambient environments" (like the Breath waveform in the Nether, or the Tide waveform in the End) to Channel B, while assigning "damage environments" (like being on fire) to Channel A. For intense environmental interactions (like traveling through a portal), it uses a coordinated A+B channel fade-in approach. Of course, we also retain a global "A/B Channel Sync" option for players who prefer a unified pace across both channels.

## Features

### Feedback Types

#### 1. Heartbeat Feedback
Triggers when player's health falls below a set threshold.

| Property | Description |
|----------|-------------|
| Trigger Condition | Player health below heartbeat threshold (default 30%) |
| Waveform | Heartbeat rhythm |
| Feedback Channel | A channel (async mode) / A+B channels (sync mode) |
| Intensity Calculation | Lower health = higher intensity (20%~100%) |

#### 2. Environment Feedback
Triggers when player is in specific environments.

| Environment | Waveform | Feedback Channel | Intensity |
|-------------|----------|------------------|-----------|
| Nether | Breath | B channel | Fixed |
| End | Tide | B channel | Fixed |
| Portal | Pinch Intensify | A+B channels | Gradual increase |
| Snow | Fast Pinch | A channel | Fixed |
| Feedback Channel | B channel (async mode) / A+B channels (sync mode) |

#### 3. Damage Feedback
Triggers when player takes damage. Uses different waveforms based on damage type.

| Damage Type | Waveform | Description |
|-------------|----------|-------------|
| Piercing | Fast Pinch | Cactus, sweet berry bush, arrows, tridents, stalactites |
| Blunt | Beat | Fall damage, mob attacks, player attacks, hitting walls, explosions, fireworks |
| Heat/Burning | Burn | Fire, lava, magma block |
| Crushing/Suffocation | Compress | Suffocation in walls, entity crushing, falling blocks, anvils |
| Environment/Drowning | Drown | Drowning, freezing |
| Magic/Poison | Tide | Magic, wither, dragon breath, hunger |

| Property | Description |
|----------|-------------|
| Feedback Channel | A channel (async mode) / A+B channels (sync mode) |
| Intensity Calculation | Based on damage value and max health ratio |

#### 4. Fade (Intensity Transition)
When the trigger condition ends, intensity smoothly transitions to 0.

| Stage | Description |
|-------|-------------|
| Delay | 2 seconds after state ends |
| Fade | Intensity drops from current to 0 within 2 seconds |
| Send Interval | Every 0.5 seconds |

### Connection Methods

- Connect via DGLab App scanning QR code
- LAN auto-discovery support
- QR code refresh function

### Intensity Fade

- Fade starts 2 seconds after state ends
- Intensity smoothly drops to 0 within 2 seconds

## System Requirements

- Minecraft 1.19.2
- Forge 43.5.0+
- Java 17+
- DGLab device (supports Socket control)

## Installation

1. Download the built mod JAR file
2. Place the JAR file in `.minecraft/mods` folder
3. Launch the game

## Usage Guide

### First Connection

1. Launch the game - WebSocket server starts automatically
2. Open the connection screen (click HUD button or use hotkey)
3. Use DGLab App to scan the QR code on screen
4. After successful connection, you're ready to use it

### Configuration Options

The following options can be set in-game or in the config file:

#### Basic Settings

| Config | Default | Description |
|--------|---------|-------------|
| Global Intensity Limit | 100% | Max intensity percentage for A/B channels |
| HUD Display | On | Show connection status, A/B channel intensity and waveform on screen |
| HUD Position | Top Left | HUD display position (top-left/top-right/bottom-left/bottom-right) |
| A/B Channel Sync | On | In sync mode, both channels use same waveform, intensity calculated independently |

#### WebSocket Settings

| Config | Default | Description |
|--------|---------|-------------|
| Server Port | 8877 | WebSocket server listening port |
| Server Enable | On | Whether to enable WebSocket server |

#### Heartbeat Settings

| Config | Default | Description |
|--------|---------|-------------|
| Trigger Threshold | 30% | Health threshold to trigger heartbeat feedback |
| Intensity Multiplier | 1.0x | Heartbeat intensity multiplier |

#### Environment Intensity

| Config | Default | Description |
|--------|---------|-------------|
| Nether Intensity | 20% | Intensity limit in the Nether |
| End Intensity | 20% | Intensity limit in the End |
| Portal Intensity | 20% | Intensity limit inside portals |

#### Damage Multipliers

Separate multipliers can be set for different damage types:

| Damage Type | Default | Damage Type | Default | Damage Type | Default |
|-------------|---------|-------------|---------|-------------|---------|
| Cactus | 1.0x | Sweet Berry Bush | 1.0x | Arrow | 1.0x |
| Trident | 1.0x | Stalactite | 1.0x | Fall Damage | 1.0x |
| Mob Attack | 1.0x | Player Attack | 1.0x | Wall Hit | 1.0x |
| Explosion | 1.0x | Firework | 1.0x | On Fire | 1.0x |
| In Fire | 1.0x | Lava | 1.0x | Magma Block | 1.0x |
| Suffocation | 1.0x | Entity Crushing | 1.0x | Falling Block | 1.0x |
| Anvil | 1.0x | Drowning | 1.0x | Freezing | 1.0x |
| Magic | 1.0x | Wither | 1.0x | Dragon Breath | 1.0x |
| Hunger | 1.0x | | | | |

#### Waveform Settings

Different waveforms can be selected in-game:

| Waveform ID | Name | Waveform ID | Name |
|-------------|------|-------------|------|
| heartbeat | Heartbeat Rhythm | breath | Breath |
| tide | Tide | beat | Beat |
| compress | Compress | bounce_gradual | Bounce Gradual |
| grain_friction | Grain Friction | wave_ripple | Wave Ripple |
| rain_wash | Rain Wash | variable_speed | Variable Speed |
| signal_light | Signal Light | tease1 | Tease 1 |
| tease2 | Tease 2 | burn | Burn |
| fast_pinch | Fast Pinch | pinch_intensify | Pinch Intensify |
| rhythm_step | Rhythm Step | drown | Drown |

### Hotkeys

- `K` - Default key to open settings screen

## Build Instructions

```bash
# Clone the project
git clone https://github.com/your-repo/DGLab-Craft.git
cd DGLab-Craft

# Build
./gradlew build

# Generate run configurations (IDE)
./gradlew genEclipseRuns
# or
./gradlew genIntellijRuns
```

After building, the JAR file is located in the `build/libs/` directory.

## Project Structure

```
src/main/java/com/lumoren/dglabcraft/
├── DGLabCraft.java          # Main class
├── config/
│   └── ModConfig.java       # Config file
├── events/
│   ├── HeartbeatHandler.java   # Heartbeat handler
│   ├── EnvironmentHandler.java # Environment handler
│   ├── DamageHandler.java      # Damage handler
│   └── FadeManager.java        # Fade manager
├── gui/
│   ├── MainScreen.java      # Main screen
│   └── ConnectionScreen.java # Connection screen
└── network/
    └── WebSocketServerManager.java # WebSocket server
```

## Acknowledgments

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB) - Reference implementation
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE) - Official Socket protocol documentation

## License

GPL 3.0

## Author

Lumoren

# AGENTS.md — DGLab Craft

Minecraft Forge 1.20.1 mod that connects to a DGLab device via local WebSocket for haptic feedback.

## Quick Reference

| Item | Value |
|------|-------|
| Mod ID | `dglabcraft` |
| Package | `com.lumoren.dglabcraft` |
| MC / Forge | 1.20.1 / 47.2.0+ |
| Java | 17 (toolchain enforced) |
| Mappings | `official` (no Parchment) |
| License | GPL 3.0 |
| Build output | `build/libs/DGLabCraft-1.20.1.jar` (jarJar), `-slim.jar` (slim) |

## Build

```bash
# Full build (produces both slim and fat jars):
./gradlew build

# Run Minecraft client from IDE:
./gradlew runClient

# Generate IDE run configs:
./gradlew genEclipseRuns
./gradlew genIntellijRuns
```

- `build.dependsOn 'jarJar'` — `build` always produces the fat jar with bundled deps.
- The fat jar (no classifier) is the distributable; the slim jar is `archiveClassifier = 'slim'`.
- `gradle.properties` contains HTTP proxy settings (127.0.0.1:7890). Remove these if they cause resolution failures in your environment.
- `org.gradle.daemon=false` — every Gradle invocation starts a fresh JVM.

## Architecture

**This mod does NOT use Forge networking (SimpleChannel/registerMessage).** Communication with the DGLab App is entirely via a local WebSocket server embedded in the client JVM. The mod is client-side only — all event handlers bail out on `!level().isClientSide()`.

### Package Map (19 source files)

```
com.lumoren.dglabcraft/
├── DGLabCraft.java              # @Mod entrypoint: registers handlers, starts WS
├── ClientModEvents.java         # @Mod.EventBusSubscriber: K key mapping
├── config/
│   └── ModConfig.java           # ForgeConfigSpec — all settings
├── events/
│   ├── DamageHandler.java       # LivingDamageEvent + LivingTickEvent → damage feedback
│   ├── EnvironmentHandler.java  # LivingTickEvent → dimension/env feedback
│   ├── HeartbeatHandler.java    # LivingTickEvent → low-HP heartbeat
│   ├── StatusEffectHandler.java # STUB (disabled, body empty)
│   └── FadeManager.java        # @Mod.EventBusSubscriber: idle safe-silence
├── gui/
│   ├── MainScreen.java          # Entry screen (K key), dispatches to sub-screens
│   ├── ConnectionScreen.java    # QR code generation, manual IP
│   ├── DGLabCraftScreen.java    # Settings with scrollable slider list
│   ├── DiagnosticScreen.java    # Runtime status readout
│   ├── DGLabCraftHUD.java       # @Mod.EventBusSubscriber: overlay rendering
│   └── Slider.java              # Custom slider widget
├── network/
│   └── WebSocketServerManager.java  # Singleton WS server, DG-LAB protocol
└── util/
    ├── QRCodeGenerator.java     # ZXing QR code
    ├── WaveformGenerator.java   # Legacy waveform time→hex converter
    ├── WaveformManager.java     # Loads waveform JSON from assets
    └── WaveformType.java        # Enum of DG-LAB waveform types
```

### Event Flow

```
Game Event (Minecraft)
  → @SubscribeEvent on MinecraftForge.EVENT_BUS (client-side only)
  → DamageHandler / EnvironmentHandler / HeartbeatHandler
  → WebSocketServerManager.requestEffect() or requestSyncedEffect()
  → JSON over WebSocket → DGLab App
```

**Handler registration** is manual in `DGLabCraft` constructor:
```java
MinecraftForge.EVENT_BUS.register(new DamageHandler());
MinecraftForge.EVENT_BUS.register(new EnvironmentHandler());
MinecraftForge.EVENT_BUS.register(new HeartbeatHandler());
```

**@Mod.EventBusSubscriber** registrations (auto-registered):
- `ClientModEvents` — MOD bus, `Dist.CLIENT` — keybinding
- `FadeManager` — FORGE bus — idle safe-silence
- `DGLabCraftHUD` — FORGE bus, `Dist.CLIENT` — overlay render

### Priority Arbitration (WebSocketServerManager)

When multiple effects compete for a channel, priority wins:
- **DAMAGE = 3** (highest — overrides everything)
- **HEARTBEAT = 2**
- **ENVIRONMENT = 1** (lowest — yields to any damage or heartbeat)

Each effect has a **lease duration** (ticks) after which the channel auto-expires, allowing lower-priority effects to resume. Leases are computed by `getLeaseTicks()` (e.g., damage=12, onFire=30, heartbeat=55, nether=55).

### Damage Source Detection

Two-phase approach in `DamageHandler`:
1. `LivingDamageEvent` caches `DamageSource.getMsgId()` (up to 40 ticks)
2. `LivingTickEvent` detects health delta → resolves source via cached ID or positional fallback (lava, fire, freezing, blocks, cramming, falling blocks, effects)

This was specifically designed to fix multiplayer client-side damage triggering.

## WebSocket Protocol (DG-LAB)

Default port **8877**, configurable via `WS_PORT`. Server auto-detects LAN IP; manual override via `WS_HOST`.

Fixed client ID: `1234-123456789-12345-12345-01`

Messages are JSON with `type`, `clientId`, `targetId`, `message`:

| Direction | Type | Purpose |
|-----------|------|---------|
| Server→App | `bind` | Initiate connection |
| App→Server | `bind` | Accept with `"message":"DGLAB"` |
| Bidirectional | `heartbeat` | Every 60s, `"message":"200"` |
| Server→App | `msg` | Commands below |

**msg commands:**
- `strength-<1|2>+<mode>+<value>` — set intensity (mode: 0=dec, 1=inc, 2=absolute)
- `pulse-<A|B>:[hex1,hex2,...]` — waveform data (16-char uppercase hex, chunked at 100)
- `clear-<1|2>` — clear waveform queue

**Send sequence for a stimulus:**
1. `clear-<channel>` — flush old waveform
2. `pulse-A/B:[chunks...]` — load new waveform
3. `strength-<channel>+2+<value>` — fire at intensity

Sync mode (when `SYNC_CHANNELS=true`): clears both channels, loads same waveform to both, fires both simultaneously.

## Dependencies

| Library | Version | Bundling |
|---------|---------|----------|
| [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) | [1.5.4, 2.0) | jarJar |
| [ZXing Core](https://github.com/zxing/zxing) | [3.5.1, 4.0) | jarJar |

Both are declared as `minecraftLibrary` (dev classpath) + `jarJar` (bundled in fat jar).

## Config (ModConfig.java)

Forge config spec (`ModConfig.Type.COMMON`). All fields are `public static final ForgeConfigSpec.ConfigValue<T>`.

- Use `ModConfig.save()` to flush to disk (called explicitly after HUD toggle/position changes in `MainScreen`).
- `BASE_MAX_INTENSITY` is deprecated/retained for compat — effective max is computed by `getEffectiveMaxIntensity(appMaxStrength)` = `appMaxStrength × MAX_INTENSITY_PERCENTAGE / 100`.

Organized categories: `general`, `websocket`, `fast_pinch`, `beat`, `burn`, `compress`, `drown`, `tide`, `environment`, `heartbeat`, `buff`.

## GUI Notes

- **No i18n in GUI code** — all button labels, slider texts, and status messages are hardcoded Chinese strings via `Component.literal()`. There are only 3 lang keys in `en_us.json`/`zh_cn.json` (for the keybinding).
- **No textures** — only `lang/` and `waveforms/` under `assets/dglabcraft/`.
- **Screen hierarchy**: `MainScreen` (router) → `ConnectionScreen` / `DGLabCraftScreen` / `DiagnosticScreen`. Opened via `mc.setScreen()` in `DGLabCraft.onClientTick()` on K key.
- **Custom Slider**: extends `AbstractSliderButton`, supports `stepSize` (0.1 or 1.0), deferred commit on mouse release or screen close via `commitCurrentValue()`.
- **DGLabCraftScreen** uses inner classes: `SettingsList` extends `ContainerObjectSelectionList<Entry>`, entry types: `HeaderEntry`, `LabelEntry`, `RowEntry`.

## Waveforms

18 JSON files in `src/main/resources/assets/dglabcraft/waveforms/`. Loaded by `WaveformManager` with classpath fallback (if `ResourceManager` init hasn't fired yet).

Format: `{"name_cn":"...", "frame_count":N, "data":["hex...","hex..."]}` or plain array `["hex..."]`.

`DAMAGE_WAVEFORM_MAP` maps damage source IDs → waveform files (6 categories: `fast_pinch`, `beat`, `burn`, `compress`, `drown`, `tide`).

## Gotchas

- **VSCode `launch.json` is stale** — references old project name `DGLab-Craft` (hyphen) and 1.19.2 paths (`forgeVersion 43.5.0`, `mcVersion 1.19.2`). Regenerate via `./gradlew genVSCodeRuns` or fix manually.
- **`StatusEffectHandler` is a stub** — body is empty. Potion effects (wither/poison) are handled in `DamageHandler.resolveDamageSourceId()` instead.
- **No tests** — `src/test/java/` and `src/test/resources/` are empty.
- **No CI** — no `.github/workflows/`.
- **`System.out.println` debug logging** in `DamageHandler` — uses stdout, not SLF4J.
- **`gradle.properties` proxy** — HTTP/HTTPS proxy at 127.0.0.1:7890. Remove for clean environments.
- **Windows line endings** — `.gitattributes` only enforces LF for `src/generated/**` files. Ensure LF for all source files.
- **The mod references an upstream project**: [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB) and [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE). Protocol conventions derive from these.

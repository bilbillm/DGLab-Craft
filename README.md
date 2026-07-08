# DGLab Craft

<p align="center"><img src="docs/logo.jpg" alt="DGLab Craft" width="200"></p>

[English Version](./README_EN.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.61+-orange)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

DGLab Craft 是一个 Minecraft 模组。它会把游戏里的受伤、低血量、环境变化等事件转换成 DGLab 设备反馈，并通过本地 WebSocket 和 DGLab App 连接。

当前分支：`1.21.1-NeoForge`
适配版本：Minecraft `1.21.1` / NeoForge `21.1.61` / Java `21`

> 提醒：请优先选择与你 MC 版本完全一致的 jar；不要混装不同 Minecraft、Forge 或 NeoForge 版本。

下载页面：

- [GitHub Releases](https://github.com/bilbillm/DGLab-Craft/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dglab-craft)

## 第一部分：我只是想安装使用

### 1. 先确认你的 MC 版本

这个分支只适合：

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`

如果你的 Minecraft 版本不一样，请先在下面表格里找对应分支，不要混装不同 MC 版本的 jar。

| MC 版本 | 加载器 | 分支 | 最新 release |
| --- | --- | --- | --- |
| 1.18.2 | Forge 40.2.21 | `1.18.2` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.18.2) |
| 1.19.2 | Forge 43.5.0 | `1.19.2` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.19.2) |
| 1.20.1 | Forge 47.x | `1.20.1` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.20.1) |
| 1.21.1 | NeoForge 21.1.x | `1.21.1-NeoForge` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.21.1-NeoForge) |
| 1.21.4 | NeoForge 21.4.x | `1.21.4-NeoForge` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.21.4-NeoForge) |

### 2. 在 GitHub 下载 Mod

1. 打开这个版本的 release：[`DGLabCraft-1.21.1-1.1.0.jar`](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.21.1-NeoForge)
2. 在页面下方找到 **Assets**。
3. 下载 `DGLabCraft-1.21.1-1.1.0.jar`。
4. 不要下载 `Source code.zip` 或 `Source code.tar.gz`，那是给开发者看的源码包，直接放进游戏不会生效。

也可以从项目的 [Releases 页面](https://github.com/bilbillm/DGLab-Craft/releases) 进入，按自己的 Minecraft 版本选择 release。

### 3. 安装到 Minecraft

1. 用启动器创建或选择一个 Minecraft `1.21.1` 实例。
2. 给这个实例安装 NeoForge `21.1.61+`。
3. 打开实例目录里的 `mods` 文件夹。常见路径是 `.minecraft/mods`。
4. 把刚下载的 `DGLabCraft-1.21.1-1.1.0.jar` 放进去。
5. 启动游戏，进入主菜单后点“模组”列表，确认能看到 `DGLab Craft`。

如果你不知道实例目录在哪：在启动器里通常可以右键实例，选择“打开文件夹”或“打开游戏目录”。

### 4. 第一次连接 DGLab App

1. 进入单人世界或服务器。
2. 按默认快捷键 `K` 打开 DGLab Craft 主界面。
3. 打开连接界面，生成二维码。
4. 手机打开 DGLab App，使用 SOCKET 控制/扫码连接。
5. 手机和电脑必须在同一个局域网里，通常就是连同一个 Wi-Fi。

连不上时先检查这几件事：

- 手机和电脑是不是同一个 Wi-Fi。
- Windows 防火墙有没有拦截 Java 或 Minecraft。
- README 或界面里显示的 IP 是否像 `192.168.x.x`、`10.x.x.x`、`172.16-31.x.x` 这样的局域网地址。
- 如果显示的是虚拟网卡地址，可以在配置里手动填写电脑真实局域网 IP。

### 5. 它会反馈哪些内容

- 低血量心跳反馈
- 受伤反馈，并按伤害来源选择不同波形
- 下界、末地、传送门、细雪等环境反馈
- A/B 通道同步或独立控制
- 反馈结束后的强度淡出
- 游戏内 HUD 和连接状态显示

## 第二部分：开发者和维护者

### 分支目标

当前分支维护：

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`
- Mod 版本 `1.1.0`

维护多个版本时，请优先把修复放到对应 MC 版本分支。不要把 Forge 和 NeoForge 的代码直接互相覆盖，它们的事件、注册和构建方式不完全一样。

### 构建

```bash
git clone -b 1.21.1-NeoForge https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

构建产物在：`build/libs/`

发布 release 时，通常上传不带 `-slim` 的完整 jar。

发布前后检查：

```bash
pwsh scripts/release-check.ps1 -Tag v1.1.0-1.21.1-NeoForge -SkipGitHubReleaseCheck
pwsh scripts/release-postcheck.ps1 -Tag v1.1.0-1.21.1-NeoForge
```

GitHub Actions 里可以手动运行 `Release Rehearsal`，它只构建和验证发布元数据，不创建 GitHub Release，也不会上传 CurseForge。

### 维护分支一览

| MC 版本 | 加载器 | 分支 | 最新 release |
| --- | --- | --- | --- |
| 1.18.2 | Forge 40.2.21 | `1.18.2` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.18.2) |
| 1.19.2 | Forge 43.5.0 | `1.19.2` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.19.2) |
| 1.20.1 | Forge 47.x | `1.20.1` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.20.1) |
| 1.21.1 | NeoForge 21.1.x | `1.21.1-NeoForge` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.21.1-NeoForge) |
| 1.21.4 | NeoForge 21.4.x | `1.21.4-NeoForge` | [v1.1.0](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.21.4-NeoForge) |

### 报 bug 时需要的信息

请尽量让用户附上：

- 诊断页一键复制反馈信息的完整内容
- Minecraft 版本
- Forge / NeoForge 版本
- DGLab Craft 版本
- Java 版本
- 单人、联机还是整合包
- DGLab App 是 iOS 还是 Android
- `latest.log`
- 如果是连接问题，请附上界面里显示的 IP 和端口

### 常见维护点

- `gradle.properties` 里的 MC、加载器和 mod 版本要和 release 对齐。
- `META-INF/mods.toml` 或 NeoForge 对应 metadata 里的版本范围要和分支一致。
- README 的下载链接和 jar 文件名要跟最新 release 同步。
- 连接问题优先看 WebSocket 是否启动、二维码 IP 是否是可访问的局域网地址。

## 许可证

GPL 3.0

## 致谢

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## 作者

Lumoren

# DGLab Craft — Minecraft 1.12.2 Forge

[English](README_EN.md) · [GitHub Releases](https://github.com/bilbillm/DGLab-Craft/releases) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dglab-craft)

将 Minecraft 的受伤、低血量和环境变化转换为 DG-LAB 反馈。

- 分支：`1.12.2-Forge`
- 模组：`1.1.4`
- 游戏：Minecraft `1.12.2` / Forge 14.23.5.2847 / Java `8`

## 安装

1. 下载 [本分支的 1.1.4 发布包](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.12.2-Forge)，选择 `DGLabCraft-1.12.2-1.1.4.jar`，不要下载源码压缩包。
2. 安装与游戏版本完全匹配的 Forge。
3. 将模组放入该游戏实例的 `mods` 文件夹。不同 MC 版本和加载器的 JAR 不可混装；删除旧版重复包。
4. 启动游戏、进入世界，在控制设置中确认“打开 DGLab Craft 设置”的绑定（默认 `K`）。

模组安装在客户端；普通联机服务器无需安装设备控制模块。远程联机按客户端实际掉血触发；原版不向客户端同步的精确伤害来源会使用本地状态推断，不能保证所有模组伤害都被精确分类。

## 设备与 App

| 项目 | 当前兼容情况 |
| --- | --- |
| DG-LAB 主机 3.0 + DGLab App 3.x SOCKET 控制 | 当前支持路线 |
| DGLab App 4.0 | 尚未适配；不可把扫码失败直接归因于 Java |
| 郊狼 2.0、其他品牌设备 | 未验证，不承诺兼容 |
| 基岩版、网易版 | 不支持此 Java 模组 |
| Android 上的 FCL/Pojav Java 启动器 | 社区有可用反馈；未完成移动端兼容验证 |

## 连接

1. 手机与运行游戏的电脑连接同一局域网。游戏服务器的公网地址、樱花 FRP 地址不是设备连接地址。
2. 进入世界后按 `K`，打开“连接设置”，手机 DGLab App 3.x 使用 SOCKET 控制扫描游戏内二维码。
3. 无法扫描时可刷新、打开二维码图片或打开图片目录。存在虚拟网卡时填写电脑真实的局域网 IPv4 地址，再刷新二维码。
4. 默认 WebSocket 端口为 `8877`。检查 Java 的防火墙放行和该端口占用；公司/校园 Wi-Fi 的设备隔离也可能阻止手机访问电脑。

按键无反应时先检查加载器/API/JAR 是否匹配，再检查控制设置的冲突和输入法。模组会记录 `Registered key binding`；冲突会记录 `Key binding conflict` 并在进入世界后提示。报告问题时附 `latest.log`、启动器、MC/加载器/Java/模组版本、单人或联机及 App 版本。不要为所有版本统一降到 Java 17，应使用上表的游戏 Java。

## 反馈设置

- 支持按伤害类型设置倍率、A/B 通道同步或分开、低血量心跳、环境反馈及 HUD/波形显示。
- 关闭心跳请将心跳阈值设为 `0`；仅受伤模式还需将下界、末地、传送门及细雪相关环境倍率设为 `0`。
- 波形预览已经提供；自定义波形编辑/导入界面尚未实现。

## 版本与加载器

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


## 构建

```powershell
git clone -b 1.12.2-Forge https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

构建启动 JDK：`8`。产物在 `build/libs/`，选择完整可安装 JAR。发布前执行 `scripts/release-check.ps1 -Tag v1.1.4-1.12.2-Forge -RequireBuiltJar -SkipGitHubReleaseCheck`；发布后执行 `scripts/release-postcheck.ps1`。源码许可为 GPL-3.0。

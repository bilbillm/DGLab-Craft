# DGLab Craft

[English Version](./README_EN.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.61+-orange)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> **Topics:** `minecraft-mod` `neoforge-mod` `dglab` `websocket` `haptic-feedback` `minecraft-1-21-1`

DGLab Craft 是一个基于 Minecraft **NeoForge** 的模组，通过 WebSocket 与 DGLab App 连接，将游戏内的事件（受伤、环境变化、特定状态）精准转换为物理反馈发送到 DGLab 设备，为你带来全方位的 4D 沉浸式硬核体验。

## 📝 写在前面

**欢迎来到 1.21.1 的新纪元！**

这是本模组经历的最彻底的一次底层重构。从 1.19.2/1.20.1 跨越到 1.21.1，我们告别了老旧的 Forge，全面拥抱了更现代的 **NeoForge** 生态。

在最新的版本中，我们不仅修复了诸多底层逻辑，还迎来了几项极其硬核的升级：
1. **真实伤害判定计算：** 接入了全新的 `getNewDamage()` API，现在你的护甲、附魔和抗性药水终于能为你“挡电”了！只有真正扣除的血量才会转化为电击反馈。
2. **现代化 UI 渲染：** 全面重写了 GUI 渲染管线，完美适配 1.21 官方的高斯模糊（Blur）滤镜，界面质感大幅提升。
3. **更纯净的依赖环境：** 彻底解决了 WebSocket 和 ZXing 库的打包冲突，开箱即用。

开发不易，连续爆肝跨越大版本更是让人头秃。这是我第一次深度接触 Java 模组开发与 WebSocket，如果在 1.21.1 的新环境下遇到 Bug，极其欢迎提交 Issue 或 PR！如果玩得开心，别忘了给个 Star！⭐

---

## ⚡ 功能特点

### 反馈类型

#### 1. 心跳反馈 (Heartbeat)
当玩家血量低于设定阈值时触发。

| 项目 | 说明 |
|------|------|
| 触发条件 | 玩家血量低于心跳阈值（默认30%） |
| 波形 | 心跳节奏 |
| 反馈通道 | A通道 (非同步模式) / A+B通道 (同步模式) |
| 强度计算 | 血量越低，强度越高（20%~100%） |

#### 2. 环境反馈 (Environment)
当玩家处于特定环境时触发。

| 环境 | 波形 | 反馈通道 | 强度 |
|------|------|----------|------|
| 下界 (Nether) | 呼吸 | B通道 | 固定 |
| 末地 (End) | 潮汐 | B通道 | 固定 |
| 传送门 (Portal) | 按捏渐强 | A+B通道 | 渐强 |
| 细雪 (Snow) | 快速按捏 | A通道 | 固定 |

#### 3. 伤害反馈 (Damage)
当玩家受到伤害时触发。根据不同伤害类型使用不同波形（受护甲减伤影响）。

| 伤害类型 | 波形 | 说明 |
|----------|------|------|
| 锐器与穿刺 | 快速按捏 | 仙人掌、甜浆果丛、弓箭、三叉戟、钟乳石 |
| 钝器与撞击 | 连击 | 摔落、生物攻击、玩家攻击、撞墙、爆炸、烟花 |
| 高温与灼烧 | 燃烧 | 着火、火中、岩浆、岩浆块 |
| 挤压与窒息 | 压缩 | 墙内窒息、实体挤压、坠落方块、铁砧 |
| 环境与缺氧 | 溺水 | 溺水、冰冻 |
| 魔法与毒素 | 潮汐 | 魔法、凋零、龙息、饥饿 |

#### 4. 强度渐变 (Fade)
当触发条件结束后，电击不会生硬断开，强度会平滑过渡到 0，还原真实的痛感消退过程。

---

## 💻 系统要求 (⚠️ 非常重要)

由于 Minecraft 1.21.1 的底层变更，请严格确保你的运行环境符合以下要求：

- **Minecraft:** 1.21.1
- **Mod Loader:** NeoForge 21.1.61 或更高版本 (不支持老版 Forge)
- **Java:** **Java 21** (1.21.1 强制要求)
- **设备:** DGLab 设备 (支持 Socket 控制)

## 📦 安装与连接说明

1. 确保已安装 Java 21 和 NeoForge 1.21.1。
2. 下载已构建的模组 JAR 文件，将其放入 `.minecraft/mods` 文件夹并启动游戏。
3. 进入游戏后，按下快捷键 **`K`** 打开全新的控制面板。
4. 使用手机 DGLab App 扫描屏幕上的二维码连接。
5. *(强烈建议：首次使用请将全局强度上限调低测试，安全第一！)*

## 🛠️ 编译方法

本项目现已全面迁移至 `ModDevGradle` 构建体系。

```bash
# 克隆项目
git clone [https://github.com/your-repo/DGLab-Craft.git](https://github.com/your-repo/DGLab-Craft.git)
cd DGLab-Craft

# 使用 Java 21 进行编译
./gradlew build
```
编译完成后，纯净的 JAR 文件将生成在 build/libs/ 目录下。

## 📂 核心项目结构 (1.21.1)
src/main/java/com/lumoren/dglabcraft/
├── DGLabCraft.java          # NeoForge 主类
├── config/
│   └── DGLabConfig.java     # 全新 ModConfigSpec 配置文件
├── events/
│   ├── HeartbeatHandler.java   # 心跳处理
│   ├── EnvironmentHandler.java # 环境处理
│   ├── DamageHandler.java      # 真实伤害判定 (getNewDamage)
│   └── FadeManager.java        # 平滑渐变管理
├── gui/
│   ├── MainScreen.java      # 现代化高斯模糊主界面
│   ├── ConnectionScreen.java # 扫码连接界面
│   └── DGLabCraftScreen.java # 强度与倍率滚动设置界面
└── network/
    └── WebSocketServerManager.java # WebSocket 核心逻辑

## 🙏 致谢
CaiJi-ikun/DG_LAB - 参考实现
DG-LAB-OPENSOURCE - 官方 Socket 协议文档
NeoForge 社区 - 感谢提供优雅的 1.21.1 模组加载 API

## 📄 许可证
GPL 3.0

## 👤 作者
Lumoren
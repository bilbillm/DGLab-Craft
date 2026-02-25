# DGLab Craft

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-43.5.0-orange)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

DGLab Craft 是一个 Minecraft Forge 模组，通过 WebSocket 与 DGLab App 连接，将游戏内的事件转换为物理反馈发送到 DGLab 设备。

## 功能特点

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
| 反馈通道 | B通道 (非同步模式) / A+B通道 (同步模式) |

#### 3. 伤害反馈 (Damage)
当玩家受到伤害时触发。根据不同伤害类型使用不同波形。

| 伤害类型 | 波形 | 说明 |
|----------|------|------|
| 锐器与穿刺 | 快速按捏 | 仙人掌、甜浆果丛、弓箭、三叉戟、钟乳石 |
| 钝器与撞击 | 连击 | 摔落、生物攻击、玩家攻击、撞墙、爆炸、烟花 |
| 高温与灼烧 | 燃烧 | 着火、火中、岩浆、岩浆块 |
| 挤压与窒息 | 压缩 | 墙内窒息、实体挤压、坠落方块、铁砧 |
| 环境与缺氧 | 溺水 | 溺水、冰冻 |
| 魔法与毒素 | 潮汐 | 魔法、凋零、龙息、饥饿 |

| 项目 | 说明 |
|------|------|
| 反馈通道 | A通道 (非同步模式) / A+B通道 (同步模式) |
| 强度计算 | 基于伤害值与最大生命值比例 |

#### 4. 强度渐变 (Fade)
当触发条件结束后，强度会平滑过渡到0。

| 阶段 | 说明 |
|------|------|
| 延迟 | 状态结束后2秒 |
| 渐变 | 2秒内从当前强度降为0 |
| 发送间隔 | 每0.5秒发送一次 |

### 连接方式

- 通过 DGLab App 扫描二维码连接
- 支持局域网自动发现
- 刷新二维码功能

### 强度渐变

- 状态结束后 2 秒开始渐变
- 2 秒内强度平滑降为 0

## 系统要求

- Minecraft 1.19.2
- Forge 43.5.0+
- Java 17+
- DGLab 设备 (支持 Socket 控制)

## 安装方法

1. 下载已构建的模组 JAR 文件
2. 将 JAR 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

## 使用说明

### 首次连接

1. 启动游戏，WebSocket 服务器会自动启动
2. 打开连接界面（点击 HUD 按钮或使用快捷键）
3. 使用 DGLab App 扫描屏幕上的二维码
4. 连接成功后即可使用

### 配置选项

在游戏内配置界面或配置文件中可以设置以下选项：

#### 基础设置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 全局强度上限 | 100% | A/B 通道的最大强度百分比 |
| HUD 显示 | 开启 | 在屏幕上显示连接状态和AB通道的强度和波形 |
| HUD 位置 | 左上 | HUD 显示位置 (左上/右上/左下/右下) |
| A/B 通道同步 | 开启 | 同步时双通道使用相同波形 强度独立计算 |

#### WebSocket 设置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 服务器端口 | 8877 | WebSocket 服务器监听端口 |
| 服务器启用 | 开启 | 是否启用 WebSocket 服务器 |

#### 心跳设置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 触发阈值 | 30% | 血量低于此值时触发心跳反馈 |
| 强度倍率 | 1.0x | 心跳强度倍率 |

#### 环境强度

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 下界强度 | 20% | 在下界时的强度上限 |
| 末地强度 | 20% | 在末地时的强度上限 |
| 传送门强度 | 20% | 在传送门内时的强度上限 |

#### 伤害倍率

可对不同类型的伤害设置单独的倍率：

| 伤害类型 | 默认倍率 | 伤害类型 | 默认倍率 | 伤害类型 | 默认倍率 |
|----------|----------|----------|----------|----------|----------|
| 仙人掌 | 1.0x | 甜浆果丛 | 1.0x | 弓箭 | 1.0x |
| 三叉戟 | 1.0x | 钟乳石 | 1.0x | 摔落 | 1.0x |
| 生物攻击 | 1.0x | 玩家攻击 | 1.0x | 撞墙 | 1.0x |
| 爆炸 | 1.0x | 烟花 | 1.0x | 着火 | 1.0x |
| 火中 | 1.0x | 岩浆 | 1.0x | 岩浆块 | 1.0x |
| 墙内窒息 | 1.0x | 实体挤压 | 1.0x | 坠落方块 | 1.0x |
| 铁砧 | 1.0x | 溺水 | 1.0x | 冰冻 | 1.0x |
| 魔法 | 1.0x | 凋零 | 1.0x | 龙息 | 1.0x |
| 饥饿 | 1.0x | | | | |

#### 波形设置

可在游戏内选择不同波形：

| 波形ID | 中文名称 | 波形ID | 中文名称 |
|--------|----------|--------|----------|
| heartbeat | 心跳节奏 | breath | 呼吸 |
| tide | 潮汐 | beat | 连击 |
| compress | 压缩 | bounce_gradual | 渐变弹跳 |
| grain_friction | 颗粒摩擦 | wave_ripple | 波浪涟漪 |
| rain_wash | 雨水冲刷 | variable_speed | 变速敲击 |
| signal_light | 信号灯 | tease1 | 挑逗1 |
| tease2 | 挑逗2 | burn | 燃烧 |
| fast_pinch | 快速按捏 | pinch_intensify | 按捏渐强 |
| rhythm_step | 节奏步伐 | drown | 溺水 |

### 快捷键

- `K` - 默认为k打开设置界面

## 编译方法

```bash
# 克隆项目
git clone https://github.com/your-repo/DGLab-Craft.git
cd DGLab-Craft

# 编译
./gradlew build

# 生成运行配置 (IDE)
./gradlew genEclipseRuns
# 或
./gradlew genIntellijRuns
```

编译完成后，JAR 文件位于 `build/libs/` 目录。

## 项目结构

```
src/main/java/com/lumoren/dglabcraft/
├── DGLabCraft.java          # 主类
├── config/
│   └── ModConfig.java       # 配置文件
├── events/
│   ├── HeartbeatHandler.java   # 心跳处理
│   ├── EnvironmentHandler.java # 环境处理
│   ├── DamageHandler.java      # 伤害处理
│   └── FadeManager.java        # 渐变管理
├── gui/
│   ├── MainScreen.java      # 主界面
│   └── ConnectionScreen.java # 连接界面
└── network/
    └── WebSocketServerManager.java # WebSocket 服务器
```

## 致谢

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB) - 参考实现
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE) - 官方 Socket 协议文档

## 许可证

GPL 3.0

## 作者

Lumoren

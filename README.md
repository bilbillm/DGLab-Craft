# DGLab Craft

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-43.5.0-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

DGLab Craft 是一个 Minecraft Forge 模组，通过 WebSocket 与 DGLab App 连接，将游戏内的事件转换为物理反馈发送到 DGLab 设备。

## 功能特点

### 反馈类型

| 类型 | 触发条件 | 反馈通道 |
|------|----------|----------|
| 心跳反馈 | 玩家血量低于阈值时 | B通道 |
| 环境反馈 | 处于下界/末地/传送门/雪地环境时 | B通道 |
| 伤害反馈 | 受到伤害时 | A通道 |

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

在游戏内配置界面可以设置：

- 心跳阈值 (默认 30%)
- 全局强度上限 (默认 100%)
- 波形选择
- 通道同步模式

### 快捷键

- `K` - 打开设置界面

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
- [DG-LAB](https://github.com/DG-LAB) - 官方 Socket 协议文档

## 许可证

MIT License

## 作者

Lumoren

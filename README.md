# DGLab Craft

[English Version](./README_EN.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen)
![Forge](https://img.shields.io/badge/Forge-47.2.0+-orange)
![Java](https://img.shields.io/badge/Java-17-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> 当前分支：`1.20.1`

DGLab Craft 是一个适用于 Minecraft Forge 的体感反馈模组。它通过本地 WebSocket 服务与 DGLab App 连接，并将游戏中的伤害、环境变化、低血量状态等事件转换成设备反馈。

## 分支定位

这个分支对应：

- Minecraft `1.20.1`
- Forge `47.2.0+`
- Java `17`

如果你要使用其他版本：

- `1.19.2` 请切换到 `1.19.2` 分支
- `1.21.1 NeoForge` 请切换到 `1.21.1-NeoForge` 分支

## 功能概览

- 低血量心跳反馈
- 环境反馈（下界、末地、传送门、细雪等）
- 伤害反馈（按伤害来源映射不同波形）
- A / B 通道同步或独立控制
- 二维码连接 DGLab App
- 强度渐变淡出

## 运行要求

- Minecraft `1.20.1`
- Forge `47.2.0` 或更高版本（同大版本）
- Java `17`
- DGLab 设备与手机 App

## 安装方法

1. 下载本分支对应的 `.jar`
2. 放入 `.minecraft/mods` 文件夹
3. 使用 Forge 1.20.1 启动游戏

## 快速使用

1. 进入单人世界或多人服务器
2. 按默认快捷键 `K` 打开设置界面
3. 在连接界面中生成二维码
4. 使用 DGLab App 的 SOCKET 控制扫描二维码
5. 确保手机和电脑位于同一局域网

## 分支现状

`1.20.1` 分支目前已经包含多轮兼容性修复，重点包括：

- 联机客机伤害反馈触发问题修复
- 伤害事件在复杂环境下改为基于真实掉血触发
- iOS 设备下波形指令格式兼容修复

如果你遇到问题，建议优先在 issue 中注明：

- 是否为整合包环境
- 是否为联机客机
- 是否为 iOS / Android 设备

## 主要配置

可在游戏内调整：

- 全局强度上限
- 心跳触发阈值
- 环境强度
- 各类伤害倍率
- HUD 显示与位置
- A/B 通道同步
- WebSocket 端口

## 开发与构建

```bash
git clone https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

如需 IDE 运行配置：

```bash
./gradlew genEclipseRuns
# 或
./gradlew genIntellijRuns
```

构建产物位于：`build/libs/`

## 已知说明

- 本分支只针对 Forge 1.20.1
- 文档不适用于 NeoForge 1.21.1
- 当前版本依然建议在复杂整合包环境中做实机验证，尤其是联机与设备连接链路

## 问题反馈

提交 issue 时请附带：

- 分支 / 模组版本
- Minecraft / Forge 版本
- Java 版本
- 设备平台（iOS / Android）
- `latest.log`
- 单人 / 联机 / 整合包环境说明

## 致谢

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## 许可证

GPL 3.0

## 作者

Lumoren

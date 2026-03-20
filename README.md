# DGLab Craft

[English Version](./README_EN.md)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.61+-orange)
![Java](https://img.shields.io/badge/Java-21-red)
![License](https://img.shields.io/badge/License-GPL_3.0-blue)

> 当前分支：`1.21.1-NeoForge`

DGLab Craft 是一个面向 Minecraft NeoForge 的体感反馈模组。它通过本地 WebSocket 服务与 DGLab App 连接，并将伤害、环境变化、低血量状态等游戏事件转换为设备反馈。

## 分支定位

这个分支对应：

- Minecraft `1.21.1`
- NeoForge `21.1.61+`
- Java `21`

如果你要使用其他版本：

- `1.19.2` 请切换到 `1.19.2` 分支
- `1.20.1` 请切换到 `1.20.1` 分支

## 分支特点

这是当前仓库中面向新版本生态的分支，主要特征包括：

- 从 Forge 迁移到 NeoForge
- Java 运行环境升级到 21
- GUI 与配置实现适配 1.21.1
- 已修复当前版本中的联机伤害反馈主链路问题

## 功能概览

- 低血量心跳反馈
- 环境反馈（下界、末地、传送门、细雪等）
- 伤害反馈（按伤害来源映射不同波形）
- A / B 通道同步或独立控制
- 二维码连接 DGLab App
- 强度渐变淡出

## 运行要求

- Minecraft `1.21.1`
- NeoForge `21.1.61` 或更高版本（同大版本）
- Java `21`
- DGLab 设备与手机 App

## 安装方法

1. 下载本分支对应的 `.jar`
2. 放入 `.minecraft/mods`
3. 使用 NeoForge 1.21.1 启动游戏

## 快速使用

1. 进入单人世界或多人服务器
2. 按默认快捷键 `K` 打开主界面
3. 打开连接界面并生成二维码
4. 使用 DGLab App 的 SOCKET 控制扫码连接
5. 确保手机和电脑位于同一局域网

## 当前分支说明

`1.21.1-NeoForge` 分支目前重点维护：

- NeoForge 版本兼容
- 连接链路稳定性
- 联机 / 整合包环境下的伤害反馈修复

如果你在这个分支提交 issue，建议说明：

- 是否为 NeoForge 原生环境还是整合包
- 是否为联机客机
- 设备平台（iOS / Android）
- Java 版本与启动器信息

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

这个分支使用 NeoForge / ModDevGradle 构建。

```bash
git clone https://github.com/bilbillm/DGLab-Craft.git
cd DGLab-Craft
./gradlew build
```

构建产物位于：`build/libs/`

## 已知说明

- 本分支不适用于旧版 Forge
- 运行时必须使用 Java 21
- 文档只针对 NeoForge 1.21.1 分支有效

## 问题反馈

提交 issue 时请附带：

- 分支 / 模组版本
- Minecraft / NeoForge 版本
- Java 版本
- `latest.log`
- 单人 / 联机 / 整合包环境说明

## 致谢

- [CaiJi-ikun/DG_LAB](https://github.com/CaiJi-ikun/DG_LAB)
- [DG-LAB-OPENSOURCE](https://github.com/DG-LAB-OPENSOURCE/DG-LAB-OPENSOURCE)

## 许可证

GPL 3.0

## 作者

Lumoren

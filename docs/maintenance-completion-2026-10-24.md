# 评论需求补齐与修复验收

日期：2026-10-24

## 本轮完成

- 修复 1.21.8 Fabric 五个界面的重复背景模糊调用，并加入回归约束。
- 为 19 个受影响版本补齐/修正注册日志与 K 键冲突诊断，菜单检测不再消耗进服提醒。
- 为旧 Forge 1.7.10/1.8.9/1.12.2 接入客户端实际掉血检测、会话重置、来源推断及反馈仲裁；1.16.5/1.18.2 等既有轮询同步修复跨世界基准。
- 为遗漏的 7 个 Fabric 分支补齐内嵌二维码及相关连接工具。
- 修复旧 Forge 环境倍率或上限为 0 仍抬为 1 的路径。
- 重写 19 个分支中英文安装/下载/兼容文档，固定 26.x Loom 构建版本，修正发布标签、产物名及 Client 元数据。

## 验证

19 个版本 Gradle build 通过；测试合计 1074 项、失败 0。另有 19 组生产按键诊断生命周期检查全部通过。

旧菜单检测实现对同一生命周期检查会失败；1.21.8 背景重复调用和旧轮询跨世界基准也经过先失败后修复。旧 Forge 报文测试调用真实仲裁和序列化入口，检查切换 clear→pulse→strength、低优先级抑制、续播不 clear、环境零值。

这些是编译、自动测试与产物验证；未对所有 MC/加载器组合启动游戏和连接实体设备，不承诺所有整合包伤害来源都能精确识别。

| 分支 | 自动测试 | 安装包 SHA-256 | 发布 |
| --- | ---: | --- | --- |
| 1.17.1-Fabric | 61 | `f8f42d35658acbf6a008d9bd3bb5eb113c16042a8e043a5eb5ab388abc2e9501` | [v1.1.4-1.17.1-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.17.1-Fabric) |
| 1.12.2-Forge | 4 | `ce74ca4860d4e18692a7ae7bacc41079047b6e530ec2db60699ce672b855ed77` | [v1.1.4-1.12.2-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.12.2-Forge) |
| 1.16.5-Forge | 62 | `10cd4bc905afd8be1fdb53205825b9788f1740a06b69bbd25a3419e77aa2096c` | [v1.1.4-1.16.5-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.16.5-Forge) |
| 1.18.2-Fabric | 60 | `92a7354472d8cfce3fd280095110e0b5a63fa46695333eb260d68f277360702c` | [v1.1.4-1.18.2-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.18.2-Fabric) |
| 1.18.2-Forge | 62 | `e5e9244d21116d4fda2396d5a80582b40b3cdc2166ba955022e0c86a8ff4c3ef` | [v1.1.4-1.18.2-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.18.2-Forge) |
| 1.19.2-Fabric | 61 | `590cced70903eb048f14ae152c3e701ab6b11cfd3ebbd43db37099e55ac7e957` | [v1.1.4-1.19.2-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.19.2-Fabric) |
| 1.20.1-Fabric | 109 | `1a85951bd24c7823a49ce95d2be61a191c96cd6b04cfca3c632e9f6d9da3b9f2` | [v1.1.4-1.20.1-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.20.1-Fabric) |
| 1.21.1-Fabric | 61 | `a396656a5b94568892a862ff1a33c1ac81852de0e04945a0bf4175fdb083b585` | [v1.1.4-1.21.1-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.1-Fabric) |
| 1.21.1-Forge | 66 | `45df3aae5881c67793e9097a71053cab4e288a72f98f5e4cc556756e56075daf` | [v1.1.4-1.21.1-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.1-Forge) |
| 1.21.11-Fabric | 62 | `94459a25e9330af8e2212829509cc6e174878c8e9fe0a82987569cd6b3590c01` | [v1.1.4-1.21.11-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.11-Fabric) |
| 1.21.11-NeoForge | 67 | `91698aae2c2c62c70e7bfccb4e83a9959af844509427c9c8da44dc741ae3591c` | [v1.1.4-1.21.11-NeoForge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.11-NeoForge) |
| 1.21.4-Fabric | 61 | `efeccb7cc1fc317be43cf1ee87c7f1e705eb3184cd2f6cc08bc06c0296fb4d04` | [v1.1.4-1.21.4-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.4-Fabric) |
| 1.21.8-Fabric | 62 | `f3a3b20fe2909d9c38383cbaa64b047e72b6786a185130e45a57c577d138f8f8` | [v1.1.4-1.21.8-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.21.8-Fabric) |
| 1.7.10-Forge | 4 | `199c2cfcbcb8d35630708c87a84f737acb1757674d7e8bb315464015d529d31d` | [v1.1.4-1.7.10-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.7.10-Forge) |
| 1.8.9-Forge | 4 | `2a6cdd2c5264570a8548b770b0f8bc48cd8ed2600dc4c4be5bf23b8c689549e3` | [v1.1.4-1.8.9-Forge](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-1.8.9-Forge) |
| 26.1-Fabric | 67 | `0c467243e89f96137b9557a59602bb6c822674877d3e18ed9d0a0796ef924049` | [v1.1.4-26.1-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1-Fabric) |
| 26.1.1-Fabric | 67 | `86c3bee503a50c4034a807b74b37a48ce77b2a2aa8698588a02265b3fa7c24d9` | [v1.1.4-26.1.1-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1.1-Fabric) |
| 26.1.2-Fabric | 67 | `040e18b08758a5928253fce64b2bf7c591b25e34e6f5375be151ad2b8c379394` | [v1.1.4-26.1.2-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.1.2-Fabric) |
| 26.2-Fabric | 67 | `1c156c4aaa437113c959be2d8b5ebe01b36dfe6dd12cf618e70b58e7ad46adf9` | [v1.1.4-26.2-Fabric](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.4-26.2-Fabric) |

## 后续架构工作

重构按 [分阶段计划](refactoring-plan-2026-10-24.md) 单独推进。全局零强度所有入口的统一、心跳任务释放、线程所有权、细雪重复请求、维度标识与 GUI 重建等架构审查项没有伪装成本轮已修复；先建立真实行为基线，再小步实施。

## 发布验收

默认分支 `1.20.1-Forge` 的中英文首页已同步完整版本表、兼容说明和正确安装包入口，保留该分支实际的 1.1.1 包。

19 个修复版本均已提交并推送，GitHub Release 与安装包附件已发布；19 组 release-postcheck 全部通过。GitHub 附件 digest 与本地 SHA-256 全部一致，19 次最终发布工作流的 CurseForge 上传步骤均为 success。平台后续审核状态未以此冒充已通过。

1.18.2/1.19.2 Fabric 首次上传被平台以缺少 Client 环境标签拒收，修正分支工作流后通过 workflow_dispatch 成功上传相同标签的已验证附件。没有改写已发布标签或替换功能产物。

逐分支证据见 [发布验证清单](maintenance-release-evidence-2026-10-24.json)。上传工作流使用现有附件；本轮实际编译与测试来自本地 19 分支构建，分别记录。

## 评论交付

B 站原视频 BV1ZUfvBkE9F 已在置顶反馈楼、1.21.11 版本请求和二维码问题下补充回复，署名“大肥鱼”。汇总说明版本下载、Loader 0.15.11、Fabric 二维码、K 键诊断、旧 Forge 联机掉血、界面修复及 App 3.x/4.0 边界，没有宣称完成所有整合包或实体设备实测。

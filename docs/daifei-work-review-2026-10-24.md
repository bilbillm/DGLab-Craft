# 大肥交付与技术审查报告复核

复核日期：2026-10-24（北京时间）

## 结论

这批有实质交付，发布记录真实；但用户指定的部分跨版本任务未完成，而且新发布的 1.21.8 Fabric 遗漏了与 #25 同类的界面崩溃。不能按“全部做好”验收。

Hyperplan 报告找到了若干真实的架构问题，适合作为重构议题清单；最终执行计划仍需修改，尤其是发送顺序、零值限幅、线程所有权与验证顺序。

## 核查依据与边界

- 已读取指定 DSH 会话 `session-627bb3e3-48ee-4abf-895b-d33333c77a7b`，包括用户的实际任务选择、大肥的完成说明、审查者往返意见和最终 planner 计划。
- 原始任务：完成“未实现”1—5（四个版本移植及 Loader 0.15.11 支持），以及“已有但有缺口”1/4/5/8（按键/加载诊断、Fabric 内嵌二维码、旧 Forge 联机伤害链路、下载与兼容说明）；提交并发布 GitHub/CurseForge，在 B 站回复相关评论；另修复 #25。
- 既有分支以本轮开始前的提交为基准核对；新增分支核查本轮移植提交。当前基准工作区为 `1.17.1-Fabric` 的 `f384673`。
- 采用 code-review skill 的两个独立审查轴：Standards 检查实现正确性及规范，Spec 检查需求覆盖。下列两组分别保留，不混成一个评分。
- 本次未修改功能代码、未提交、未发布、未回复外部评论。验证包含本地测试和 Minecraft 字节码核查，未启动实体设备或游戏做完整实机测试。

## 发布与验证事实

- [GitHub Releases](https://github.com/bilbillm/DGLab-Craft/releases) 中确有当天 12 个 Release 和安装包附件，对应 11 个版本/加载器组合；1.21.11 Fabric 先发 1.1.2，再发 1.1.3，所以 Release 数为 12。
- 12 次最终成功的发布工作流，其 `Upload jar to CurseForge` 步骤均为 `success`。可以确认上传流程成功，不能仅据此声称平台审核、所有客户端运行行为均已验证。
- 这些最终工作流的 `Build jar` 步骤均为 `skipped`，使用已准备的附件上传路径。它们能证明发布动作成功，不是独立 CI 构建验证。工作区存在相应构建/测试结果，但运行时覆盖仍不足。
- [#25](https://github.com/bilbillm/DGLab-Craft/issues/25) 已关闭，1.21.11 Fabric/NeoForge 的重复背景修复及回归测试均存在。
- 本次在 `1.17.1-Fabric` 和 `1.21.8-Fabric` 强制执行 `gradlew test --offline --rerun-tasks`，分别 54 项、59 项测试通过。1.21.8 的测试仍未覆盖下述界面崩溃。其他工作区已有测试结果不是本次重新运行，不能混称本次全量复测。
- 1.8.9 被如实标为 [prerelease/beta](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.8.9-Forge)，发布说明承认仅编译及 reobf、未实机冒烟。该分支 `build.gradle` 排除了 test 源集，不应把构建通过解释成测试覆盖充分。
- 大肥明确承认 B 站评论未发送。没有伪造发送成功，但这仍是原任务的未交付项。

示例发布证据：[1.21.11 Fabric 发布工作流](https://github.com/bilbillm/DGLab-Craft/actions/runs/40080424172)、[1.20.1 Fabric 发布工作流](https://github.com/bilbillm/DGLab-Craft/actions/runs/40080418357)、[1.8.9 发布说明](https://github.com/bilbillm/DGLab-Craft/releases/tag/v1.1.0-1.8.9-Forge)。

## Standards

未发现需要单列的明文规范违规或新增代码异味。以下是独立审查确认的实质缺陷：

### [P1] 1.21.8 Fabric 打开界面仍会触发重复模糊崩溃

位置：[MainScreen.java:102](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_wt/1.21.8-Fabric/src/main/java/com/lumoren/dglabcraft/gui/MainScreen.java:102)，其他四个自定义界面也保留显式背景调用。

当背景模糊强度至少为 1 时，Minecraft 的 `Screen.renderWithTooltip` 已经先绘制背景，再进入子类 `render`；本项目第二次调用背景模糊，会触发 `IllegalStateException: Can only blur once per frame`。此次 1.21.11 已删除同类重复调用，但新移植的 1.21.8 没同步。

已用本机缓存的 Minecraft 1.21.8 mapped jar，通过 `javap -c -p` 核实调用链：

`Screen.renderWithTooltip → renderBackground → renderBlurredBackground → GuiGraphics.blurBeforeThisStratum → GuiRenderState.blurBeforeThisStratum`。

对照过 1.21.4：它也有重复绘制，但使用不同模糊机制，不能据此声称它同样崩溃。本项只针对 1.21.8。

### [P2] 旧 Forge 新增轮询跨世界保留血量基准

位置：[1.18.2 DamageHandler.java:58](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_wt/1.18.2-Forge/src/main/java/com/lumoren/dglabcraft/events/DamageHandler.java:58)、[1.16.5 DamageHandler.java:58](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_wt/1.16.5-Forge/src/main/java/com/lumoren/dglabcraft/events/DamageHandler.java:58)。

退出世界时因玩家/世界为空直接返回，未清理 `lastHealth`、来源缓存及去抖状态。随后进入血量更低的另一个存档/服务器，会把两个会话之间的血量差误判为伤害，发送并非受伤导致的反馈。需要在退出世界、切换玩家或世界实例时清理状态并重建基准。

### [P2] 按键冲突提示在进入世界前就被消耗

位置：[ClientModEvents.java:47](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_main/src/main/java/com/lumoren/dglabcraft/ClientModEvents.java:47)。

启动后主菜单阶段的首次 client tick 会设置 `conflictChecked=true`；实际发送聊天提示却要求玩家存在。常规进入游戏流程中仅记日志，进世界后永久跳过，因此承诺的游戏内一次提示不会出现。应分开记录“扫描完成”与“提示已送达”，或等玩家存在后检查。

Standards 轴合计 3 项实质缺陷；该轴最高优先级为 1.21.8 Fabric 界面崩溃。

## Spec

以下按原始需求逐项对照，不与 Standards 缺陷合并计数。

1. **“有缺口4：Fabric 游戏内显示二维码”仅部分完成。** 13 个 Fabric 分支中，7 个仍使用外部图片路径：1.17.1、1.18.2、1.19.2，以及四个 26.x。当前目录 [ConnectionScreen.java:172](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_main/src/main/java/com/lumoren/dglabcraft/gui/ConnectionScreen.java:172) 仍调用 `renderQrPath`。1.16.5 Fabric 是既有实现，不是此次新增；此次补了 1.20.1/1.21.1/1.21.4/1.21.8/1.21.11。不能用概括性的“Fabric 已内嵌二维码”替代分支清单。
2. **“有缺口5：旧 Forge 联机伤害链路及调度”漏了两个版本。** 1.16.5、1.18.2 已改；1.7.10、1.12.2 未改，仍依赖伤害事件且直接发送波形。用户指定的清单明确列出四个版本，不能只验收后两个。
3. **“有缺口1：按 K 无反应诊断”存在文档与代码不符。** 诊断实现只加在 1.17.1 Fabric，却复制到其他多个 README。例如 [1.20.1 README:87](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_wt/1.20.1-Fabric/README.md:87) 承诺注册日志和冲突聊天提示，但该分支 [ClientModEvents.java:26](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_wt/1.20.1-Fabric/src/main/java/com/lumoren/dglabcraft/ClientModEvents.java:26) 只有按键注册。1.21.4/1.21.8 Fabric、1.8.9/1.16.5/1.18.2/1.21.1 Forge 也有同类问题；部分 Forge 文档还误导用户检查 Fabric API。
4. **“有缺口8：下载与兼容说明”仅部分完成。** 中文主 README 的分支名和矩阵已有改善，但仍未明确 App 3.x/4.0、设备代际兼容；[README_EN:48](C:/Users/lumoren/Documents/GitHub/DGLAB_Craft_main/README_EN.md:48) 仍指向旧 1.18.2 Forge 下载和安装说明。

四个指定版本移植、1.20.1 Fabric Loader 0.15.11 声明及 #25 修复均有对应代码与发布记录；未发现明显无关范围扩张。“有代码和发布”不抵消 Standards 轴发现的运行缺陷。

Spec 轴合计 4 项代码/文档覆盖缺口；该轴主要缺口是跨分支任务尚未补齐。B 站未回复另记为外部交付未完成。

## 对 Hyperplan 技术审查的复核

### 有证据、值得采纳的部分

- **会话和定时器生命周期。** `WebSocketServerManager.startHeartbeat()` 每次连接创建 `Timer`，未持有、未取消，且任务使用全局 `connectedClient` 而不是参数 socket；重连会遗留定时器。收敛 session、取消旧任务的方向正确。
- **关闭及零强度语义。** 多个 handler 的 `Math.max(1, ...)` 确实会把 0 抬为 1；世界为空时 `FadeManager` 直接返回，退出世界缺少统一停止/清理链路。
- **收敛发送接口。** 多条发送路径混合序列化、仲裁、状态更新，合并重复逻辑有价值；应保留同步/非同步和波形续播的行为差异。
- **GUI 重建问题。** `DGLabCraftScreen.java:547/582` 直接重调无参 `init()`，确有重复追加控件的风险。应在恢复默认或连接变化的触发序列下验证，而不是只静置 60 帧。
- **细雪重复触发及维度判定。** 两条细雪分支重复请求、使用维度类型 `toString()` 判断下界/末地，都是具体的行为问题，优先级高于泛化成大配置表。
- **清理真正死代码和失真测试。** `PulsePreviewState` 没有生产引用；`ModConfigTest` 测的是自行复制的算式，不是生产方法。删死代码、用真实生产行为替代该测试是合理方向。

### 执行计划必须修订的部分

| 原计划 | 问题 | 建议修订 |
| --- | --- | --- |
| A2 只按 `appA/BMaxStrength` clamp，却声称“0% → 0” | handler 已把 0 改成 1 时，App 上限 100 的终端 clamp 仍得到 1；没有处理全局比例和逐事件禁用 | 明确有效上限为设备上限乘全局比例，并在 handler/请求层保留禁用语义；覆盖设备上限 0、全局比例 0、事件倍率 0、上限降低及异常输入 |
| A5 写成 `chunk → clear → strength` | 若照顺序实现，会先排波形再清空刚排的队列 | 切换波形沿用 `clear → pulse → strength`；续播不要无条件 clear；用假传输记录精确报文序列 |
| A5 要求“同帧多来源仅发一次” | 这是新增批处理调度语义，不能仅靠合并方法或线程断言得到 | 与保持行为的接口收敛分成两步；如需批处理，明确收集请求、仲裁和统一 flush 的边界，并测试优先级、AB 同步及租约 |
| A6 session 后 A7 再由 socket 层单点发布快照 | 仅有不可变快照不会消除 callback、游戏 tick、定时器对同一状态的并发写；报告尚未确定谁拥有状态 | 先确定线程所有者，再令其他线程排队提交消息。可以由游戏线程维护反馈状态，传输层仅处理 socket；也可以采用串行 executor，但不能直接在其上读取游戏世界对象 |
| A3 “退服后两通道归零” | 若设备已经断连，无法保证远端收到归零；只检查本地字段清空也不足以验证发送 | 区分可连接时先发停止、断连后本地状态失效、重连后保持静默；覆盖 socket 代际和退出/重连序列 |
| C3 从 `Screen.init` 移走二维码，改显式刷新 | 若直接删除自动生成调用，会倒退到首次打开必须手动刷新，重现曾修复的首次使用问题 | 二维码生成改为幂等、缓存或延迟初始化；首次连接仍自动生成，host/会话变化失效，resize 不重复生成 |
| D1 到 A6 后才补 session 测试 | 状态拆分前没有行为约束，很难证明重构没有改变设备指令 | 在 session/发送面改动前先建立连接、重连、退出世界、0 强度和报文顺序的测试；每步实现后补增量测试 |
| 默认把 f384673 当长期架构基准 | 它是当前工作目录的 1.17.1 Fabric，不自动等于长期主维护版本 | 先明确主维护版本及移植策略，保留 MC API 适配边界；不要因当前目录而固定所有核心设计 |

另有定位失准：最终 planner 称 `WebSocketServerManager` 为 883 行，当前实际 1024 行；仲裁在 `requestEffect/requestSyncedEffect`，并不在 `start()`。行数本身不是质量标准，但说明计划需要按现有代码再校准一次。

`LivingEntityMixin` 是否有效、资源包波形是否属于未来能力，先调查再决定保留是合理的。只验证原版的一条运行路径不够证明所有模组环境下均无调用；删除前需检查真实调用与兼容要求。资源包加载路径当前未接入也不等于未来自定义波形需求可以被删除。

## 建议下一轮顺序

1. 先修新发布 1.21.8 的界面崩溃，以及新增轮询的跨世界误触发；核对其他版本是否存在同样的状态问题。
2. 按原需求补齐遗漏版本，修正文档中不存在的诊断能力与未更新的英文下载指引。
3. 建立真实状态转换和报文序列测试，覆盖断开、换世界、重连、0 强度和 A/B 独立上限。
4. 再实施小步的会话生命周期修复、单一线程所有权、发送面收敛和死代码清理。把纯重构与行为变化分开提交。

不要把本次审查当作重构实施授权；这份文件仅记录独立复核结果。

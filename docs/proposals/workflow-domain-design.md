# 工作流领域开发设计方案

更新日期：2026-09-26。状态：用户已授权实施，正在完成验证。本文保留研究与决策背景；当前业务规则见[工作流领域](../../smart-manage-server/domains/workflow/docs/process/workflow.md)和[请假样板](../../smart-manage-server/domains/demo/docs/office/leave.md)，完成情况见[实施进度](./workflow-implementation-progress.md)。实施状态不能等同于已完成运行验收。

最终方案：**Warm-Flow 后端能力 + 官方 iframe 设计器与流程图 + Smart Manage 必要的权限、事务和业务适配 + 原生 React 业务审批页面 + Smart Manage 消息中心与工作流任务能力。** 不自建完整设计器，不预建独立流程 DSL，不重复维护引擎已有的定义、版本与运行数据。排除 workflow 后，platform 必须仍能独立构建、启动和正常使用。

本文已取代早期“第一版必须使用原生 React 设计器”“完整 React 组件选型未完成才能继续”的研究结论。日常业务审批页面保持原生 React，iframe 的范围限于官方设计器与流程图。第 12 节保留决策演进，历史建议不得覆盖本节的最终决定。

## 1. 已确认边界与文档用途

- 后端采用 Warm-Flow，置于独立、可选的 `smart-manage-domain-workflow` 领域。
- 复用 Warm-Flow 的定义保存、发布、版本、流程执行、任务与历史能力；Smart Manage 服务端负责入口权限、对象授权、事务、并发协调及业务联动，不再平行建设一套引擎。
- 第一版通过 iframe 集成官方设计器和流程图。流程管理、业务审批、待办/已办等日常页面采用本项目原生 React。
- 未来官方若提供合适的 React 前端，主要替换 Designer/Viewer 集成边界；这不是已确认的上游计划，也不承诺未知接口和数据格式下零迁移。
- 平台允许为真实接入需求增加小而通用的扩展点；平台不得依赖 workflow，也不得为了不改平台而在 workflow 中积累绕行、复制或特殊处理。
- 审批页面采用顶部操作区、左侧单据详情、右侧“任务处理 / 审批记录”布局；复用公共页面与业务内容，具体审批能力由 workflow 提供。
- 新增独立请假样板，放入 `demo`；接受 `demo → workflow` 依赖，现有采购申请代码与业务行为保持不变。
- 保留 `definition / instance / runtime / task / assignment / engine` 职责分离，不建设运行时多引擎切换或 `engineType` 配置。
- 用户随后已明确授权按推荐方案实施，并确认撤回限制、受审计候选人维护和发布时分支互斥校验。下文研究阶段的候选建议以生效领域文档及已确认规则为准。

本文是已采纳的开发方向及待验收要求，仍位于 `docs/proposals/`，不把计划伪装成现行实现。后续实施前必须阅读本文；实现完成后，将实际生效约束同步到架构文档、平台相关模块文档及 workflow/demo 领域文档，并保留必要决策来源。

项目权威约束来自[后端架构](../architecture/backend.md)、[前端架构](../architecture/frontend.md)、[模块开发指南](../development/module-development-guide.md)、[聚合检查清单](../development/business-aggregate-checklist.md)、[数据库开发](../development/database.md)、[安全架构](../architecture/security.md)、[数据权限](../architecture/data-permission.md)、[功能与权限](../architecture/feature-and-permission.md)、[前端页面指南](../development/frontend-page-guide.md)和[质量验证](../development/verification.md)。

## 2. 证据基线与对上轮讨论的修正

| 来源 | 本次核对基线 | 可以证明什么 |
| --- | --- | --- |
| Smart Manage | `d276907b722b55589c72953b126ea59cead55c3c`，研究开始时工作区干净 | Boot 4.1.0 / Java 21；React 19；可选领域、Contract、事务和页签规则 |
| 用户提供的 React 参考项目 | `E:/Files/0926/temp/plus-ui-6.X-React` 本地文件快照，未将其认定为 RuoYi 官方项目 | 工作流业务页面实现；设计器和流程图实际为 iframe |
| Warm-Flow | 官方仓库 `c083ee8b647cf0df090fa4bbbe38c44d6e156b8a` | 后端 POM 标记 1.8.9；存在 Boot 4 / MyBatis-Plus starter；官方 Vue 设计器实现 |
| LogicFlow | 官方仓库 `698019f1ef6dd322afbbb0b4e82b9d31e198adac` | React 节点适配、画布与生命周期实现 |
| FlowGram | 官方仓库 `ba1a9630f80263a196d31993cd85fd1c873d9ddd` | 固定布局编辑器、嵌套 blocks 模型及官方示例 |
| codingapi/flow-frontend | `9decfc0abce94d3e1019f62a03b7238459f4f9f6` | React 完整审批前端的组件入口、API 和执行模型耦合；flow-design 源码标记 0.2.8 |

外部仓库研究副本位于 `E:/Files/0926/temp/` 下的 `warm-flow-research-20260926`、`logicflow-research-20260926`、`flowgram-research-20260926`。这些是分支源码快照，不能等同于已发布 npm/Maven 制品，也未证明在本项目中兼容运行。

### 2.1 React 参考项目的实际价值

`src/pages/workflow/processDefinition/design.tsx` 通过 iframe 加载 `/warm-flow-ui/index.html`；`src/components/workflow/FlowChart.tsx` 同样嵌入官方 UI。因此其“React 工作流”主要是流程管理、任务和审批页面，不能当作原生 React 设计器样板。

可以参考任务列表分类、审批记录、转办与干预交互、请假与任务联动。不能直接搬入 Smart Manage：

- 认证 token 被拼接到 iframe URL，而本项目使用 HttpOnly Cookie 与内存 CSRF Token。
- 设计器关闭消息只判断 `event.data.method`，没有核对消息来源和窗口。
- `formPath`、`formCustom`、数字节点类型、引擎状态直接进入页面接口；本项目应使用显式业务资源与页面白名单。
- 请假页面组织客户端 `variables` 发起流程；本项目的审批条件、申请人、组织和金额等事实必须由服务端从业务对象取得。
- 上述差异是集成边界的比较，不表示已对参考项目完成安全审计或运行验收。

### 2.2 官方 Vue 设计器并非框架无关设计器

源码已有 `initialJson`、数据提供接口和编辑事件，但 `UiAdapter` 引用了 Vue 的 `Component`、`Directive`；仿钉钉的 `baseNodeView.ts` 使用 `createApp` 挂载 `.vue` 节点。README 将 npm 集成标为实验性，设计器 package 版本与后端 POM 也不相同。

`common/js/tool.ts` 中确有 `json2LogicFlowJson`、`logicFlowJsonToWarmFlow`，适合研究定义映射；`mimic/js/mimic.ts` 中有结构插入、删除与布局逻辑，适合借鉴规则。它们不是已验证可直接移植的独立 SDK。

因此应修正两项判断：

1. 使用相同画布库不等于只换一层 UI，React 节点、属性面板、编辑状态、引用选择和页签生命周期都需要集成。
2. LogicFlow 与 FlowGram 都有编辑模型到审批模型的映射成本，不能仅用“多一层转换”排除 FlowGram。

### 2.3 完整 React 设计器的复用边界

补查 `codingapi/flow-frontend` 的上述固定提交后，确认它有较完整的 React 审批前端，但尚不能认定为可直接接入 Warm-Flow 的通用设计器：

- `flow-design/src/index.ts` 公开 DesignPanel、导入组件和插件；其包 exports 只有根入口，内部 FlowEditor 不能视为稳定公开接口。
- `DesignPanelProps` 提供 id/open/onClose 等属性，没有文档 value/onChange 或 API 注入入口；`createDesignContext` 直接构造 `new DesignPanelApiImpl()`。
- 默认 API 包含 workflow load/save/create-node 等后端路径；节点创建也依赖自身后端。
- `Workflow` 包含 form、operatorCreateScript、strategies；条件扩展的输入输出仍是 script/GroovyVariableMapping。替换弹框并不等于替换执行语义。
- 包内还依赖 flow-core、flow-types、flow-pc-ui、脚本引擎及 FlowGram 的材料组件。它的完整性有价值，但直接搬入会连带另一套前端状态、传输和脚本约定。
- `design-panel/store.tsx` 在模块级创建 `designStore`，`layout/index.tsx` 的 Provider 使用该共享实例；公开面板路径尚不能证明多个保活页签的编辑隔离。此项由 ChatGPT 补充，Codex 已复核源码，但未运行复现串页。

因此它只保留为历史研究候选，不属于第一版实施依赖；未来若重新考虑，须评估是否能通过公开接口或可上游维护的小范围扩展完成接入；若必须长期 fork 核心、改造脚本模型或仿制另一套后端协议，则不满足本项目控制维护成本的要求。本轮没有安装运行该项目，也没有证明其 React 19、多页签或构建兼容性。

[Warm-Flow 官方集成文档](https://www.warm-flow.com/master/primary/designerIntroduced.html)在本轮读取时 React 栏目仍为“待完善”。这不能证明没有任何社区 React 实现，也不能作为官方承诺将发布 React 组件的依据。性能应比较实际加载、内存及页签生命周期；本轮未测量 iframe 与原生实现的性能差异。

## 3. 官方 iframe 集成与未来替换边界

### 3.1 采用范围与成本判断

第一版直接复用官方设计器和只读流程图，不以 LogicFlow、FlowGram 或 codingapi 为基础重写完整工作流前端。流程定义列表、任务中心及业务审批页仍由 Smart Manage 实现原生 React 界面。管理人员进入设计器时才加载官方 UI，不让设计器进入普通页面的初始依赖链。

早期比较确认：画布库不能替代审批属性、人员规则、条件配置、图校验与编辑生命周期。LogicFlow 的 React 支持不意味着官方 Vue 设计器只需更换 UI；FlowGram 同样存在模型映射与产品配置成本。完整 React 候选还存在 API 注入、脚本语义和共享状态耦合。选择 iframe 是当前控制开发和上游兼容成本的决定，原生设计器不再是一期前置任务。

iframe 自带独立文档和运行环境，实际资源开销取决于加载时机、重复实例、监听与缓存；本轮未做性能对比，不能宣称 iframe 必然导致不可接受的性能问题，也不能宣称性能已通过验收。

### 3.2 集成契约与安全边界

由 workflow 的前端集成层封装 Designer/Viewer 的入口、定义或实例定位、只读模式、保存/关闭反馈与生命周期。具体属性名依上游公开能力确定，不为假设中的未来 React 版本设计庞大接口。

- 优先同源部署官方静态资源，统一应用认证；不把长期登录凭据拼入 iframe URL，不照搬参考项目的 token 传递方式。
- 官方设计器使用的 HTTP 入口必须纳入 Smart Manage 的认证、CSRF、功能权限和对象授权；内嵌页面不是可信客户端。不能为方便集成开放另一套绕过授权的保存、发布或运行写接口。
- 保存和发布经服务端适配调用 Warm-Flow；需要同时更新业务绑定或其他本项目元数据时，在同一事务内完成，不能先由 UI 保存引擎，再用第二次请求补关联。
- 使用跨窗口消息时校验来源 origin、来源窗口、消息类型和载荷；消息只能表达约定的界面事件，不传递任意页面路径或执行指令。
- 官方 UI 的人员查询、业务条件、可用节点与执行能力需适配本项目。只开放一期允许的能力；UI 隐藏不能代替服务端校验。
- iframe 的部署路径、CSP/frame 限制、CSRF 衔接、保存结果及脏数据反馈是否可通过所选版本公开能力实现，是实施前的技术验证项。本方案未声称已有现成无改造集成。
- 若确实缺少必需的上游接口，优先小范围适配或可维护的上游扩展；需要长期 fork 核心或大面积重写时，重新评估成本并说明实证，不静默扩大自研范围。

### 3.3 生命周期与兼容性

设计器按页签隔离，两个定义不共享编辑状态；保存失败保留修改，关闭前保护未保存内容。隐藏页签避免不必要的轮询、监听和动画；再次显示正确恢复尺寸，关闭后释放 iframe 及宿主监听。能否暂停上游后台活动须实际验证，不能仅靠隐藏容器推断。

流程图按实例实际使用的历史定义与轨迹显示，不能拿最新定义解释旧实例。固定并记录后端和 UI 的实际制品版本及兼容组合，升级时验证接口、定义往返、历史查看和生命周期，不把源码分支版本视为已发布制品。

未来换官方 React 前端时，目标修改范围是设计器/流程图包装、接口适配和必要格式迁移；业务审批面板、任务中心、业务绑定和授权契约应尽量保持稳定。设计器替换与引擎替换分别评估，不预先维护两套前端，也不承诺任意版本无损互换。

## 4. 领域、应用与模块边界

建议交付一个可选 Maven 模块，沿用项目的四层目录结构；不把六个职责各拆成 Maven artifact。

```text
smart-manage-server/domains/workflow/
  pom.xml                         # smart-manage-domain-workflow
  src/main/java/sm/domain/workflow/
    process/                      # 工作流应用，暂定名
      definition/                 # 定义管理入口、发布与业务类型绑定
      instance/                   # 实例台账、业务关联、查询和历史摘要
      runtime/                    # 发起/推进编排、事务参与与幂等
      task/                       # 当前用户任务、审批命令与权限
      assignment/                 # 人员规则解释与候选解析
      engine/                     # 本领域使用的引擎边界
        warmflow/                 # Warm-Flow API、模型、查询与状态转换
  src/main/resources/db/workflow/migration/
  src/main/resources/META-INF/smart-manage/migration.properties

smart-manage-web/src/domain/workflow/
  process/definition/
  process/instance/
  process/task/
  process/assignment/              # 有独立页面需求时才建完整页面模块
  process/designer/                # 官方 Designer/Viewer 集成边界

smart-manage-server/domains/demo/
  src/main/java/sm/domain/demo/office/leave/  # 新增请假样板，应用名暂定
smart-manage-web/src/domain/demo/office/leave/
```

这六个职责不是六份重复持久化层：`runtime` 是编排能力，`assignment` 是规则和解析，`engine` 是适配，不为目录齐全强行创建 Controller、Mapper 或数据表。

第三方 Java 类型、状态码与表查询限制在 `engine/warmflow`；其他模块消费项目自己的命令、查询结果和值对象。适配器按定义管理、运行命令、运行查询三组实际用例组织，避免一个巨大接口，也不按第三方每个 Service 镜像包装。

更换引擎的目标是缩小实现修改面，不是承诺运行中实例自动迁移。更换仍需处理定义语义差异、运行数据、历史展示、通知和数据库迁移；默认建议在旧实例结束后切换，运行中迁移另行立项。`engineType` 不解决这些问题。

### 4.1 复用引擎模型，保留必要领域边界

采用 Warm-Flow 自有定义和版本能力，不另造一套流程 DSL、编译器或定义/修订状态机。业务侧使用稳定的领域命令、定义引用、实例引用及业务资源标识，供应方完整载荷及格式细节限制在 Designer/Viewer 和 engine 适配边界。

稳定接口不等于每个身份都要再建映射表；能由引擎现有标识和必要业务元数据表达的，不重复存储。格式标识只在实际需要读取、导入或升级数据时保留，不作为运行时多引擎路由。

编辑和发布仍必须校验能力白名单、业务条件、人员规则、对象权限及并发版本；服务端不能因为载荷来自官方 UI 就信任它。不支持的执行字段明确拒绝，不能静默丢弃后声称保存成功。首版不提供任意监听器类名、脚本、SpEL、URL 或 Spring Bean 路径的配置入口。

如果开放导入，只接受所支持 Warm-Flow 格式中经过白名单校验的能力子集，不承诺任意官方 JSON 可直接导入。授权的服务端固定扩展与用户提交的任意执行表达式应明确区分。

### 4.2 platform 独立与扩展原则

这是用户明确确认的硬约束：移除 workflow 后，原 platform 必须可独立构建、启动、迁移和正常使用。平台可以因真实需求增加合理能力；判断依据是职责与整体复杂度，不以“平台零修改”为目标。

| 接入点 | platform / 前端公共层职责 | workflow 职责 |
| --- | --- | --- |
| 单据页面 | 顶部操作区、主体、可选侧栏、滚动和关闭保护 | 审批 Tabs、任务操作、意见与历史内容 |
| 消息中心 | 通知功能及可选任务入口的接入契约 | 待办/已办/我发起的、任务数量与查询 |
| 资源导航 | 显式注册、可校验的资源和页面入口 | 工作流入口及审批任务定位 |
| 用户与组织 | 各拥有模块的最小稳定查询 Contract | 审批规则和人员解析 |
| 通知投递 | 已有通知发布 Contract | Outbox、事件生成及重试 |
| 装配与迁移 | 通用领域选择、注册和迁移加载机制 | 自有依赖、配置、Mapper、迁移资源和权限目录 |

接口由使用扩展的宿主定义，workflow 提供实现，通过应用装配连接；沿用现有机制并增加最小契约，不预建热插拔、插件优先级或万能业务钩子框架。后端依赖保持 `workflow → platform`、`demo → workflow`，业务结果参与接口的运行时回调不改变编译依赖方向。

platform 不直接导入 workflow 的类或前端组件；把硬编码导入改成动态 import 再加开关仍不等于可裁剪。所选领域的装配层负责注册，平台仅消费契约。没有提供者是合法状态；重复注册、无效注册或已装配能力执行失败必须可诊断，不能用吞错或捕获 404 冒充“未安装”。

禁止为了避免修改 platform 而复制页面壳、改内部 DOM、跨域读表、反射找 Service 或伪造另一套 sys API。只有确实需要的能力进入平台，所有流程语义和 Warm-Flow 类型留在 workflow。

领域排除是构建/部署组合，不意味着运行时热卸载，也不意味着删除历史数据。平台独立检查必须实际移除领域源码/依赖后验证，不能只隐藏菜单。

## 5. 数据所有权、定义和实例生命周期

| 数据 | 事实来源 | 本项目需要增加的内容 |
| --- | --- | --- |
| 请假业务字段与业务状态 | 请假聚合 | 所属组织、申请人、日期/时长、附件与状态 |
| 引擎定义、保存、发布与版本 | Warm-Flow | 经 workflow 入口统一授权、校验、并发控制；不平行复制定义/修订表 |
| 业务类型与定义的绑定、产品启用策略 | workflow 必要业务元数据 | 仅补引擎不能表达的关联与规则，不复制引擎完整定义 |
| 引擎节点、跳转、活动任务、参与人、运行历史 | Warm-Flow | 通过 adapter 读取与执行，不平行复制一整套任务表 |
| 实例业务关联与并发协调 | workflow instance 台账 | `resourceType/resourceId`、发起轮次、固定修订、组织与申请人摘要、引擎实例 ID、锁定记录 |
| 通知可靠投递 | workflow outbox | 事件、幂等键、领取与重试状态 |
| 命令去重 | workflow runtime | 请求键、调用主体、目标、载荷摘要及已提交结果 |

实例运行状态以引擎为准，若台账为列表效率保存状态摘要，它是同一事务维护的投影，不允许独立改写造成两个状态机。人员解析快照优先利用引擎参与人存储；只有无法容纳必要审计事实时才增加独立记录。

本项目新增存储只围绕必要业务绑定、实例关联/协调、命令去重、Outbox 和无法由引擎表达的审计事实评估；不预设每项必须独立建表，不为替换引擎额外复制定义、修订、任务或历史表。官方 PostgreSQL 全量脚本当前有七张核心表，但不能据此承诺整个工作流领域只有七张表。当前 ORM 还出现表单相关映射，选用制品时应核对实际启用能力与 SQL，一期不接动态表单功能。

定义复用与生命周期约束（实现时核对选定版本的实际语义，缺口在适配层补齐）：

- 草稿允许不完整保存；发布必须通过结构、字段类型、人员规则及能力白名单检查。
- 保存、发布、停用是不同命令；发布串行化并保证同一绑定最多一个当前可发起修订。
- 已发布修订不可编辑，修改生成新草稿；每个实例固定关联启动时的修订。
- 停用阻止新发起，正在执行的实例继续办理；不要直接把引擎的挂起语义当作产品停用。
- 已被实例引用的定义不物理删除；历史仍能读取原图和当时的人员名称快照。
- 相同业务对象每轮审批单独建实例；同一对象同时最多一条活动审批，数据库唯一约束与事务检查共同保证。

本次核实 `InsServiceImpl.start` 按 `flowCode` 查询当前发布定义。建议发布和新发起使用同一产品定义的协调锁，启动后核对实际 `definitionId` 与选中修订一致，否则整笔事务失败；不假定上游已经提供按修订 ID 启动的接口。该协调锁应纳入全部入口的一致锁顺序，不能与业务行和实例行形成反向依赖。

请假领域应保存每轮提交内容及附件引用快照，历史审批详情读取对应轮次，不能展示重提后的最新业务内容。引擎“结束”与产品“审批通过”分开：拒绝、撤回、管理员终止也可能结束实例，产品结果需依据已授权命令与完整执行结果在同一事务内结算。

以上为实现必须保护的数据一致性边界；具体业务匹配规则仍需决策。跨组织差异流程、按有效日期匹配、发起时选流程和动态迁移均会扩大复杂度，第一版建议先使用业务类型的显式唯一绑定。

## 6. 事务、并发和引擎接入

Warm-Flow 当前 `TaskServiceImpl.skip(FlowParams, Task)` 在第 167 行附近仍有待办/实例同步的并发 TODO；静态阅读不能证明实际并发结果。不能把引擎 API 存在等同于项目所需的一致性已成立。

目标是同一 PostgreSQL 数据源、同一 Spring 事务管理器下的原子事务：

```text
提交请假：保存完整聚合 → 冻结本轮业务快照 → 启动引擎 → 记录关联/Outbox
办理任务：复核任务资格 → 推进引擎 → 业务结果参与写入 → 记录Outbox/命令结果
任一步失败：整个数据库事务回滚
事务提交后：Outbox 调用站内消息 Contract
```

遵守项目单一事务 owner：公开 Service 做业务入口与日志，包级 TxService 拥有事务；跨模块组合用不新建事务、无活动事务时拒绝执行的参与接口，禁止 TxService 互调。业务结果回写不是一个新开事务的回调 Service。外部 HTTP、邮件和站内通知投递均不放进审批事务。

建议以本项目实例台账行的 PostgreSQL 行锁串行化同一实例的全部变更，包括并行分支审批、撤回、转办及管理员干预。锁内重新查询引擎状态和办理人；前端禁用按钮、引擎 taskId 或 JVM 锁不能替代这一边界。跨实例允许并行。

业务行与实例行需要统一加锁顺序，避免“提交先锁业务，审批先锁实例”的死锁。设计参与接口时先确定业务引用、按统一顺序锁定并重校验，再执行引擎；所有入口和未来后台任务都必须遵守，不开放绕过编排层的引擎 HTTP 写接口。

命令幂等键必须绑定当前主体、操作目标和请求内容；相同键不同内容拒绝，相同命令在成功提交后返回原结果。超时重试不新建流程；过期审批版本不得误作用于下一轮任务。真实数据库测试必须证明锁、唯一约束和事务回滚，Mock 不足以验收。

Warm-Flow starter 需要重点验证 Jackson 3、MyBatis-Plus 版本收敛、Mapper 扫描、主键填充及事务参与。配置留在可选 workflow 的装配范围；不要为了引擎自动配置把 workflow 依赖加到 platform。

迁移采用 workflow 独立链与历史表；官方 SQL 纳入本项目 Flyway 并补齐备注和必要索引，不能要求部署人员手工执行、不能自动 repair、不能让引擎自行修改已发布结构。优先保留第三方表名并限定 adapter 访问，是否改前缀以已验证的 ORM 支持为准。

## 7. 人员、权限与业务联动

### 7.1 本项目已有能力和缺口

- `UserReferenceReader`：存在/启用校验、批量引用读取。
- `UserAssignmentReader`：用户任职校验，尚无组织负责人或角色成员查询。
- `OrgReferenceReader`：组织引用与可用性。
- `UserAssignmentEntity.isOrgLeader`：组织负责人关系的事实位于任职表。
- `UserRoleEntity` 包含 `userId/orgId/roleId`：角色规则必须带组织语义，不能按角色全局混合所有组织人员。
- 站内消息已有 `InboxNotificationPublisher`；现行[站内消息文档](../platform/message/inbox.md)已要求未来 workflow 自有 Outbox。

按确定的第一版规则，向拥有相应关系的 sys 模块增补最小查询 Contract；不要先造统一的用户/角色/组织万能门面。前端也不能为了选审批人直接静态导入 sys 的业务实现，应由 workflow 的候选查询和公共 RefSelector 组合。

建议人员规则优先选择指定用户、单据所属组织的指定角色、单据所属组织负责人。后两者的查询以单据组织为准，不以审批人当前切换的组织代替。角色成员是否必须有有效任职、多人如何会签、负责人为空如何处理，需要写成明确规则。

已确认节点激活时解析并冻结候选人，后续组织/角色变化不改写当前任务；办理时校验用户当前启用状态。新增成员不自动进入已有任务；独立权限的候选人维护记录原因、操作人和前后人员，不代审批。审批人等于申请人时允许手动审批，不自动跳过。空候选使激活事务失败，不自动通过、不默认交给管理员；前一审批不提交，修正规则所需的人员资料后重试。

### 7.2 任务资格不等于业务查看权限

权限分四层：流程配置权限、发起业务权限、任务办理资格、实例/业务内容查看权限。任务办理资格必须由服务端校验，客户端不能提交 `handler`、`permissionFlag` 或任意节点跳转来授权自己。

不能只复用采购的 `SELF=申请人`：跨组织审批人可能有任务却看不到单据。建议由请假领域提供受任务资格约束的审批详情与附件只读授权，不因此授予普通业务列表、任意修改或跨组织浏览权限。申请人、当前候选人、历史处理人和流程管理员的可见范围应分别明确；流程管理员也不天然拥有代审批权限。

### 7.3 业务接入接口

未来真实跨领域消费者仅依赖 workflow 的稳定 Contract：发起命令、实例引用、审批结果以及业务参与接口。业务实现负责提交校验、服务端变量快照、结果回写与业务访问规则；workflow 不认识请假 Entity/Mapper，不通过反射调用任意业务 Service。

本次已确定请假属于 demo，因此它将成为 workflow 的首个真实跨领域消费者。Contract 只围绕这条已确认用例提取，留在 workflow 模块内部，不另外拆 `workflow-contract` Maven artifact。结果参与接口由 workflow 发布、demo 实现；运行时通过显式注册调用，编译依赖仍只有 `demo → workflow`。

## 8. 原生审批页面、任务中心与领域裁剪

### 8.1 页面归属与组合

定义管理使用 LIST + EDIT，官方设计器为独立集成页面；任务与实例使用原生查询列表，业务审批详情复用业务页面内容及公共编辑页布局，请假为标准业务聚合 LIST + EDIT。页面类型按生效规范登记，不把整份业务详情变成临时 Drawer。

功能身份、页面键、权限、菜单保持显式关联；定义发布、任务办理和实例干预分别授权，不按路径前缀猜测。审批动作使用任务身份，业务内容使用业务资源及本轮快照，避免同一单据不同轮次或多个任务共用错误的页面状态。

页面由业务模块组合：demo 请假页面复用自身详情分区，并接入 workflow 的公开审批面板。workflow 不导入 demo 的业务页面、不理解请假 Entity/Mapper；前端跨域接入同样经明确公开契约。任务入口通过显式注册定位相应审批页，不允许任意 `formPath` 或组件路径。

### 8.2 已确认布局与交互

用户指定的金蝶 AI 星瀚页面已在 Chrome 只读观察：目标 div 为 `c5b6b392-b0ce-4359-ba1a-ae551a148698`，顶部操作区，左侧单据详情，右侧任务处理/审批记录，右侧底部有任务提交按钮。仅观察和切换 Tabs，没有提交业务操作。此观察仅证明参考布局，不是 Smart Manage 页面验收；文档不保存参考站点的单据或人员内容。

```text
┌─────────────────────────────────────────────┐
│ 顶部操作区：查看流程图、可用全局操作、关闭      │
├──────────────────────────────┬──────────────┤
│                              │ 任务处理     │
│ 业务单据详情                 │ 审批记录     │
│                              ├──────────────┤
│ 基本信息、明细、附件……       │ 决策与意见   │
│                              │ 或审批历史   │
│                              ├──────────────┤
│                              │ 提交审批操作 │
└──────────────────────────────┴──────────────┘
```

- 扩展公共 `EditPageShell` / `EditPage` 的可选侧栏内容入口；公共层只理解布局，不增加 `workflowId` 或引擎类型等业务属性。具体接口名在实施时确定。
- 一个页面壳、一组顶部操作区。复用业务内容和分区，不在审批容器中再嵌套一整个带工具栏的编辑页，不复制公共页面 CSS。
- 左侧默认只读，右侧是独立审批表单；侧栏位于业务 Form 及其禁用上下文之外，避免嵌套表单、校验混用或意见框随只读单据一起禁用。
- 左右区域分别滚动；右侧任务提交按钮保持可见。顶部是全局操作，右下是当前任务表单提交，不重复放置含义相同的提交入口。实施时同步说明这一布局在前端页面指南中的适用边界。
- 右侧一期包含“任务处理”和“审批记录”；不因参考页存在其他 Tab 而扩展产品范围。切换历史不丢失未提交意见，关闭页面纳入统一脏数据保护。
- 任务失效、被他人处理或权限变化后重新校验并刷新；审批记录只读，不能仅凭本地按钮状态授权办理。
- 单据加载失败时阻止提交；历史查询失败可单独重试，不能清空意见。审批成功后按实际影响刷新任务、计数与业务状态。
- 布局宽度、窄屏呈现及是否支持折叠在实施时遵循现有页面规范；公共布局应处理尺寸与滚动，不由 workflow 操纵内部 DOM。
- 普通编辑页不传侧栏时，原有布局、字段读写、关闭保护和滚动行为必须保持不变。

### 8.3 消息中心与任务接入

现有 sys 消息中心保留通知所有权。待办、已办、我发起的及其查询/计数属于 workflow，通过任务入口契约接入消息中心，复用宿主的导航与布局；“消息已读”不等于“任务已办”，不复制任务事实到通知表作为审批依据。

沿用所选领域生成注册的装配方式；sys 不直接静态或硬编码动态导入 workflow。任务扩展不存在时隐藏对应入口，不发任务 API 请求，普通消息列表和未读计数保持正常。扩展存在但请求失败时正常展示错误，不静默当作未安装。

业务导航采用 `resourceType/resourceId`、任务引用及显式注册入口。移除 workflow 后，既有通知仍可阅读；失去对应处理器的操作提示能力不可用，不加载已删除页面，不删除历史消息。后端详情、附件与办理均重新鉴权。

### 8.4 当前代码锚点与实施落点

以下是研究时已核实的现状，不代表所述扩展已经存在：

- [EditPageShell](../../smart-manage-web/src/domain/common/page/EditPageShell.tsx) 已提供操作区和主体，目前没有侧栏入口；[EditPage](../../smart-manage-web/src/domain/common/page/edit/EditPage.tsx) 已管理业务 Form、只读状态及关闭保护。
- [InboxCenter](../../smart-manage-web/src/domain/sys/message/inbox/InboxCenter.tsx) 和 [InboxPreviewDrawer](../../smart-manage-web/src/domain/sys/message/inbox/InboxPreviewDrawer.tsx) 已有任务占位；本轮未把占位改为真实任务能力。
- [领域选择](../../smart-manage-web/scripts/selected-domains.mjs) 与 [注册生成](../../smart-manage-web/scripts/gen-registry.mjs) 已按所选领域组装页面；不要手工修改生成产物接入 workflow。
- [领域 Maven 聚合](../../smart-manage-server/domains/pom.xml) 与 [Flyway 装配](../../smart-manage-server/platform/src/main/java/sm/infrastructure/persistence/FlywayMigrationConfig.java) 是后端可选装配落点；引擎配置和迁移放在 workflow 中。
- [站内消息文档](../platform/message/inbox.md) 已有发布契约和未来 workflow Outbox 边界，应复用而非另造平台通知入口。

## 9. 请假样板与第一版能力建议

用户已明确使用新增请假样板，不改变采购现状。字段为单据组织、申请人、请假类型、开始/结束、申请天数、事由及可选附件。天数人工填写，保留两位小数；不建设考勤、余额和工作日历。

样板放置比较及已确认选择：

| 归属 | 好处 | 代价 |
| --- | --- | --- |
| `workflow/example/leave` | workflow 可完整裁剪，现有 demo 不增加依赖 | 只能验证领域内部接入，不能证明跨领域 Contract 已经实战 |
| `demo/office/leave`（已选） | 有真实跨领域消费者 | demo 从此依赖 workflow，单独保留 demo 的发行方式改变 |
| 独立示例领域 | 可单独裁剪，能验证跨领域接入 | 增加模块、领域依赖声明和测试组合 |

当前领域迁移仅按 id 排序，没有领域依赖图。已接受的依赖需要在后续实施中同步更新装配、裁剪规则、文档及测试，不能只在 demo 的 Java 代码增加 import。

- 支持纯平台、平台 + workflow、平台 + workflow + demo；包含 demo 却排除 workflow 的组合明确报依赖缺失，不静默关闭请假或自动省略业务实现。
- 后端 Maven 与前端领域选择必须对同一依赖闭包达成一致；CI 仍保持通用，不将具体领域名写入工作流矩阵。
- 采购代码和行为不变，但 demo 整体依赖增加，这是用户已接受的交付变化。
- 优先让 demo 的迁移只创建自身请假结构与目录，不引用 workflow 迁移中的具体行或建跨领域外键。流程样板由有明确权限的运行时命令创建/发布，避免人为产生迁移顺序依赖。
- 如果最终确实要求迁移阶段绑定预置工作流定义，则需要显式领域迁移依赖与拓扑校验；不能依赖 `demo/workflow` 的字母顺序。没有这种需求时不预建通用迁移依赖框架。

建议第一版先验证“草稿 → 提交 → 审批中 → 通过/拒绝”的闭环，增加条件分支与只读轨迹。业务状态与流程状态分别维护：业务领域解释通过/拒绝后能做什么，引擎只负责运行事实。

用户已确认的一期范围：

- 核心：定义草稿与发布、审批任务、同意/拒绝、我发起的/待办/已办、条件分支、业务回写、站内通知。
- 一期包含顺序审批、或签、受限撤回及受审计的候选人维护；条件分支必须互斥，发布时校验并保留唯一默认分支。
- 后续再评估：会签、并行、转办、抄送、管理员终止、任意退回、拿回、委派、加减签、票签、超时调度、子流程、外部调用、脚本节点、动态表单。

拒绝结束与退回修改不是同义词，第一版不以一个“驳回”按钮同时承担两种行为。拒绝或撤回后重提生成新实例；本人被选中时允许手动审批；参与人只读取对应轮次；节点激活时冻结候选人，空候选阻止推进。具体规则以工作流领域文档为准。

特别注意 Warm-Flow 当前 `revoke` 会重新生成开始节点后的任务，不是简单终止本轮。已采纳的撤回通过 adapter 的终止接口实现，仅申请人且尚无人完成实际审批时允许；任务创建与候选人维护不计为审批。审批与撤回按同一锁顺序竞争，不能按 API 名称直连。

## 10. 实施顺序与验收基线

以下为验收要求，实际执行结果单独记录在实施进度。业务规则已由用户确认，不以本节检查清单代替运行证据。

| 阶段 | 输出 | 通过条件 |
| --- | --- | --- |
| A. 接入准备 | 固定 UI/后端制品、明确一期产品规则 | 接入契约与依赖组合明确；不再以寻找原生设计器为前置任务 |
| B. 技术验证 | 官方 iframe 接入 + 引擎事务/并发验证 | 认证/CSRF、保存发布、历史图、多页签、失败回滚与重复命令通过验证 |
| C. 可选领域与平台扩展 | 装配、迁移、必要 Contract、侧栏和任务入口 | 平台不依赖 workflow，旧页面行为不变，完整装配可用 |
| D. 请假闭环 | 提交、原生审批面板、结果回写、轨迹 | 单事务、任务资格、详情/附件授权及每轮快照通过验证 |
| E. 消息与发行 | Outbox、计数刷新、完整和平台版验证 | 通知幂等可重试，实际移除领域后平台仍正常 |

### 10.1 必须覆盖的业务与集成验证

1. PostgreSQL 实库验证新增直接提交、业务写失败、引擎写失败、回写失败的整体回滚。
2. 两个应用实例处理同一任务、重复提交、幂等重放；纳入一期的并行、撤回、转办等操作还要验证相互竞争和统一锁顺序。
3. 发布新版本后旧实例使用原定义；停用阻止新发起但不阻断旧实例；历史图与业务快照不受重提影响。
4. 默认分支、条件边界、未知字段、非法图、危险执行字段、未授权引擎写入口与导入拒绝。
5. 人员解析、跨组织任务、用户禁用、角色变化、空候选及详情/附件权限；按最终选定业务规则验证。
6. Outbox 成功投递后回写失败仍可幂等重试；已完成任务的迟到通知不能重新授权办理。
7. iframe 认证与 CSRF、消息来源校验、保存冲突、脏数据保护、双页签隔离、隐藏恢复、关闭释放及首次加载依赖。
8. 审批页左侧只读而右侧可填，切换记录保留意见，独立滚动和固定提交区；普通无侧栏编辑页不回归。
9. ArchUnit 保护 Warm-Flow 类型边界、跨领域 Contract、单一事务 owner；前端验证平台无 workflow 导入及装配一致性。
10. 所选制品升级时重验定义往返、历史版本、接口和页面生命周期；不能只验证新建流程。

### 10.2 platform 独立运行必须实证

| 场景 | 验收要求 |
| --- | --- |
| 纯 platform | 实际移除 workflow 及依赖它的 demo 源码/制品后，两端构建通过，后端启动、平台迁移与原有功能正常 |
| platform + workflow | 定义、设计器、任务和消息接入可用，workflow 不要求 demo 存在 |
| platform + workflow + demo | 请假完整闭环可用，采购原有业务行为不变 |
| demo 缺少 workflow | 两端装配明确报告依赖缺失，不静默跳过或产生半可用发行包 |
| 未装配 workflow 的前端 | 无工作流入口、相关产物和请求；普通消息、页面导航及编辑页继续正常 |
| 未装配 workflow 的后端 | 不要求 Warm-Flow 配置/Bean，不访问或执行工作流表及迁移 |
| 已有数据后排除 workflow | 平台在代表性已有数据库上启动与使用正常，保留原历史数据；失效消息操作明确提示 |
| 扩展故障 | 已注册能力的加载/请求失败正常报错，不伪装成未安装 |

空库和已有数据场景都要覆盖；移除领域不包含删除表或清除业务数据。CI 继续使用通用平台版/完整领域版入口，具体领域依赖留在装配声明和选择校验中，不把领域名称写进 CI 矩阵。

### 10.3 实施时同步文档与防遗忘清单

- 后端/前端架构：只记录真正落地的依赖和扩展契约；公共页面指南记录侧栏、表单隔离、滚动与操作区规则。
- 平台消息文档：记录任务入口契约、无提供者行为和失效资源操作；用户/组织模块记录实际新增的查询 Contract。
- workflow 领域文档：记录引擎及 UI 固定版本、定义所有权、权限、事务、任务和升级边界。
- demo 请假文档：记录业务状态、每轮快照、审批访问规则和已确认的时长等产品行为。
- 装配/移除指南：记录 demo 对 workflow 的依赖和支持组合；把可机械判断的边界落实到架构测试、装配检查及必要回归中，不能只留文字提醒。
- 回填本文实施状态和验收证据；不要用计划中的“应通过”替代真实结果。

按[质量验证](../development/verification.md)执行与实际改动匹配的门禁。研究阶段只检查文档；实施阶段必须完成两端构建、真实数据库、权限及浏览器验收，实际结果见[实施进度](./workflow-implementation-progress.md)。

## 11. 可复核的外部源码入口

- [React 参考项目设计器](E:/Files/0926/temp/plus-ui-6.X-React/src/pages/workflow/processDefinition/design.tsx)、[流程图](E:/Files/0926/temp/plus-ui-6.X-React/src/components/workflow/FlowChart.tsx)、[请假页面](E:/Files/0926/temp/plus-ui-6.X-React/src/pages/workflow/leave/leaveEdit.tsx)。
- [Warm-Flow Vue UI adapter](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-vue-designer/src/ui/uiAdapter.ts)、[Vue 节点](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-vue-designer/src/components/design/mimic/js/baseNodeView.ts)、[定义转换](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-vue-designer/src/components/design/common/js/tool.ts)。
- [Warm-Flow 任务推进](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-core/src/main/java/org/dromara/warm/flow/core/service/impl/TaskServiceImpl.java)、[定义发布](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-core/src/main/java/org/dromara/warm/flow/core/service/impl/DefServiceImpl.java)、[PostgreSQL 表结构](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/sql/postgresql/postgresql-warm-flow-all.sql)。
- [LogicFlow React 节点包](https://github.com/didi/LogicFlow/blob/698019f1ef6dd322afbbb0b4e82b9d31e198adac/packages/react-node-registry/package.json)、[Portal 实现](https://github.com/didi/LogicFlow/blob/698019f1ef6dd322afbbb0b4e82b9d31e198adac/packages/react-node-registry/src/portal.ts)。
- [FlowGram 固定布局包](https://github.com/bytedance/flowgram.ai/blob/ba1a9630f80263a196d31993cd85fd1c873d9ddd/packages/client/fixed-layout-editor/package.json)、[固定布局文档模型示例](https://github.com/bytedance/flowgram.ai/blob/ba1a9630f80263a196d31993cd85fd1c873d9ddd/apps/docs/components/fixed-examples/step-7/initial-data.ts)。
- [Warm-Flow 发起实例](https://github.com/dromara/warm-flow/blob/c083ee8b647cf0df090fa4bbbe38c44d6e156b8a/warm-flow-core/src/main/java/org/dromara/warm/flow/core/service/impl/InsServiceImpl.java)、[FlowGram 快捷键预设](https://github.com/bytedance/flowgram.ai/blob/ba1a9630f80263a196d31993cd85fd1c873d9ddd/packages/client/fixed-layout-editor/src/preset/fixed-layout-preset.ts)。
- [codingapi 公开入口](https://github.com/codingapi/flow-frontend/blob/9decfc0abce94d3e1019f62a03b7238459f4f9f6/packages/flow-design/src/index.ts)、[面板与模型](https://github.com/codingapi/flow-frontend/blob/9decfc0abce94d3e1019f62a03b7238459f4f9f6/packages/flow-design/src/components/design-panel/types.ts)、[API 绑定](https://github.com/codingapi/flow-frontend/blob/9decfc0abce94d3e1019f62a03b7238459f4f9f6/packages/flow-design/src/components/design-panel/hooks/use-design-context.ts)、[共享编辑状态](https://github.com/codingapi/flow-frontend/blob/9decfc0abce94d3e1019f62a03b7238459f4f9f6/packages/flow-design/src/components/design-panel/store.tsx)。

研究阶段没有复制第三方实现。实施采用固定版本官方 UI 构建，随制品保留上游许可与版权声明；不把第三方画布复制为项目自研代码。

## 12. 决策演进与后续使用规则

使用仓库 [codex-duet-github skill](../../.agents/skills/codex-duet-github/SKILL.md)，在原对话[工作流模块选型 Compare](https://chatgpt.com/c/6ab3986a-6654-83e8-a282-184042dc6561)中实际完成过两轮 Discussion。没有启用 Plan 或正式 Review，也没有把 ChatGPT 的源码判断当作本地测试结果。后续用户明确决定优先于历史讨论建议。

启动时 GitHub 连接账户为 `Jickfu`，权限元数据包含 push；本地与远端 main 基线为 `d276907b722b55589c72953b126ea59cead55c3c`。固定 SHA 读取检查通过，研究文档发布到 `codex/workflow-design-discussion-20260926`，未修改业务代码或主分支。本次文档整理记录用户后续确认，不声称新的界面方案已由对方做过源码评审或运行验收。

| 阶段 | 当时结论 | 最终处理 |
| --- | --- | --- |
| 初期原生设计器研究 | 比较 LogicFlow、FlowGram、完整 React 候选 | 保留源码证据；不作为一期实施方案 |
| 定义隔离讨论 | 考虑自有 DSL、独立产品修订 | 不预建 DSL，不复制定义/版本；仅保留必要业务元数据与适配 |
| 完整 React 候选审查 | 注入、脚本语义及共享 Store 障碍未解决 | 不再阻塞一期，改用官方 iframe 集成 |
| 用户确认整体方案 | Warm-Flow 后端 + iframe + 原生审批与消息中心 | 已采纳，见第 1、3、5、8 节 |
| 用户补充平台约束 | 排除 workflow 后 platform 独立；允许合理修改平台 | 已采纳，见第 4.2、10.2 节 |
| 用户指定审批布局 | 顶部操作、左侧详情、右侧任务/历史 | 已只读观察并采纳，见第 8.2 节 |
| 用户确认样板边界 | demo 内新增请假，接受 demo 依赖 workflow | 已采纳，采购代码与行为保持不变 |

后续开发不重新默认选择自研 React 设计器、不复制引擎状态机、不为避免修改平台而制造工作流侧绕行。发现选定版本不能满足关键契约时，先提供具体接口、源码或运行证据，再评估局部适配和方案调整。

产品规则已经由用户确认并落实到生效领域文档；具体制品兼容性由实施验证。未纳入一期的引擎能力不能因上游支持而自动开放。

文档检查与发布结果由本轮交付报告记录。事务、权限和部分页面交互已有本地验证，完成项及待验收项以[实施进度](./workflow-implementation-progress.md)为准；仓库 Quality Gate 的事件条件不能因文档通过本地检查就被视为远端 CI 已通过。

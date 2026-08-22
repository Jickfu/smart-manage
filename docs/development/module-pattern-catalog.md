# 模块样板目录

本目录帮助开发者和 AI 在实现新模块前选择真实、当前的仓库样板。样板只证明对应维度的实现方式，不代表其中每一处代码都能无条件复制；具体业务语义以目标模块文档为准。

## 样板选择原则

1. 先按模块类型选择主样板，再按树、引用选择、敏感配置、状态命令等横切能力选择辅助样板。
2. 至少检查主样板的后端 Service/TxService、前端页面、页面注册、迁移和风险测试，不能只复制一个组件。
3. 样板与当前 `AGENTS.md` 或架构文档冲突时，以当前规则为准，并记录样板需要演进的地方。
4. 不把当前模块的偶然字段、权限码、CSS 类名或业务状态复制为通用设计。

## 主样板

| 场景 | 推荐样板 | 重点观察 |
| --- | --- | --- |
| 标准主从业务聚合 | 后端 `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition`、前端 `smart-manage-web/src/domain/scm/procurement/purchaseRequisition`、`docs/domains/scm/procurement/purchase-requisition.md` | 主从保存/提交、乐观锁、编号、附件、LIST/EDIT 页签生命周期 |
| 独立配置 LIST/EDIT | 后端 `smart-manage-api/src/main/java/sm/domain/sys/base/sysparam`、前端 `smart-manage-web/src/domain/sys/base/sysParam`、`docs/domains/sys/base/system-parameter.md` | Feature 归属、标准 `EditPage`、列表树和详情回显 |
| 带独立状态命令与敏感配置 | 后端 `smart-manage-api/src/main/java/sm/domain/sys/message/email`、前端 `smart-manage-web/src/domain/sys/message/email`、`docs/domains/sys/message/email.md` | 启停不进保存 Form、凭据安全状态、RefSelector、记录详情和外部副作用 |
| 树形主数据 | 后端 `smart-manage-api/src/main/java/sm/domain/sys/base/org`、前端 `smart-manage-web/src/domain/sys/base/org`、`docs/domains/sys/base/organization.md` | 树语义、父级引用、组织约束；页面形态不能脱离目标模块需求照搬 |
| 用户列表与组织树 | `smart-manage-web/src/domain/sys/base/user/UserListPage.tsx`、`smart-manage-web/src/domain/sys/base/user/refSelector/useUserRefSelector.ts` | 组织根节点、默认范围、用户引用选择和批量选择 |
| 调度配置与执行记录 | `smart-manage-web/src/domain/sys/scheduler/job`、`smart-manage-web/src/domain/sys/scheduler/execution`、`docs/domains/sys/scheduler/job.md` | 配置 LIST/EDIT、状态命令、只读执行详情和运行态组装 |

## 横切能力样板

| 能力 | 参考位置 |
| --- | --- |
| 页面注册 | `smart-manage-web/src/domain/scm/procurement/purchaseRequisition/pageRegistration.ts` |
| 标准列表 | `smart-manage-web/src/domain/scm/procurement/purchaseRequisition/PurchaseRequisitionListPage.tsx` |
| 标准编辑 | `smart-manage-web/src/domain/sys/base/sysParam/SysParamEditPage.tsx` |
| 编辑页明细操作区 | `smart-manage-web/src/domain/sys/base/numberRule/NumberRuleEditPage.tsx`、`smart-manage-web/src/domain/scm/procurement/purchaseRequisition/PurchaseRequisitionEditPage.tsx`；观察通过 `detailExtra` 将新增、删除、排序等操作归集到明细折叠面板标题栏右侧，表格只使用选择列确定操作目标 |
| 实体引用选择器 | 公共交互见 `smart-manage-web/src/domain/common/component/RefSelector.tsx`；树表选择见 `smart-manage-web/src/domain/sys/base/org/refSelector/useOrgRefSelector.ts`；领域多选与组织范围见 `smart-manage-web/src/domain/sys/base/user/refSelector/useUserRefSelector.ts`；账号选择见 `smart-manage-web/src/domain/sys/message/email/refSelector/useEmailAccountRefSelector.ts` |
| 通用业务弹框 | `smart-manage-web/src/domain/common/component/AppModal.tsx` |
| 操作确认 | `smart-manage-web/src/domain/common/component/useOperationConfirm.ts`、`OperationConfirmProvider.tsx`、`OperationConfirmModal.tsx`；观察风险类型、遮罩行为、危险操作键盘限制和统一按钮区 |
| 操作结果反馈 | `smart-manage-web/src/domain/common/component/useOperationFeedback.tsx`、`operationFeedbackPolicy.ts`；观察默认自动关闭、可选常驻与关闭按钮、语义颜色、最大宽度和稳定错误码分类 |
| 公开 Service 与 TxService | `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition/service` |
| 权限常量 | `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition/constant/PurchaseRequisitionPermission.java` |
| 架构边界测试 | `smart-manage-api/src/test/java/sm/architecture/SystemDependencyBoundaryTests.java` |
| 状态、事务与并发测试 | `smart-manage-api/src/test/java/sm/domain/scm/procurement/purchaserequisition/service/PurchaseRequisitionTxServiceTests.java` |
| 页面注册生成与校验 | `smart-manage-web/scripts/gen-registry.mjs`、`smart-manage-web/scripts/verify-permissions.mjs` |

## 常见反模式和安全路径

| 反模式 | 为什么不符合项目 | 安全路径 |
| --- | --- | --- |
| 未分析样板就直接生成完整模块 | 容易只满足技术栈而偏离项目交互和生命周期 | 先声明模块类型、主样板和差异点 |
| 独立主数据或配置使用一个 CUSTOM 页面拼接列表和表单 | 绕过标准页签、脏数据保护和详情生命周期 | 默认建立共享 Feature 的 LIST + EDIT 页面 |
| 列表为了执行单记录命令而使用单选框 | 把命令约束错误建模成选择约束 | 使用复选框，仅在恰好选择一条时启用命令 |
| 在编辑页保存启停、封存等状态 | 容易误改状态并扩大保存 Form 权限 | 使用列表专用命令和最小参数接口 |
| 详情只返回外键 ID，前端再查列表回显 | 形成额外请求并破坏引用选择器边界 | 详情返回引用对象，保存只提交 ID |
| 使用普通 Select 重做已有实体选择 | 搜索、分页、树筛选和回显行为不一致 | 复用或新增 `use*RefSelector` |
| 使用 `remark`、`note` 或“说明”表达通用描述 | 同义字段重复建模 | 使用 `description` 和“描述” |
| 给组织树人工增加没有业务语义的“全部组织”根节点 | 改变默认查询范围并偏离已有页面 | 复用真实顶级组织和现有默认选择语义 |
| 为标准表单复制 `EditPageShell + Form` | 重复布局、校验、操作区和只读逻辑 | 优先使用 `EditPage`，只扩展无法表达的部分 |
| 将编辑页明细操作放进折叠面板内容区或为明细表格增加“操作”列 | 操作位置不统一，并绕过标题操作区和批量选择约定 | 使用 `detailExtra` 或 `EditSectionCollapse` 分区的 `extra` 将操作归集到标题栏右侧，表格使用选择列确定目标；只有明确的特殊交互要求才允许偏离并说明原因 |
| 顶部普通命令使用默认按钮，或将危险命令混排在常规命令中 | 页面主操作视觉不一致，危险操作难以识别且容易误触 | 普通命令统一使用 `primary`；上下文控件在前、主要命令和辅助命令居中、`danger` 危险命令最后 |
| CUSTOM 页面自行实现 Modal、分页或固定表格高度 | 容易出现间距、滚动和操作位置不一致 | 复用 `AppModal`、通用分页和 flex 高度链 |
| 页面直接使用 `Modal.confirm`、`Popconfirm` 或 `message.*` | 操作风险、遮罩行为、反馈级别和关闭方式不一致 | 确认操作使用 `useOperationConfirm`，结果反馈使用 `useOperationFeedback` |
| 在 TSX 中使用内联样式快速修补布局 | 难以维护且绕过项目 CSS 约定 | 使用以 `sm-` 开头的 CSS 类 |
| 通过权限前缀或组件路径推断 Feature | 稳定身份被命名偶然性绑架 | 在迁移和页面注册中显式关联 `featureKey` |
| 外部调用放在数据库事务中并默认重试 | 可能造成长事务、重复副作用和不可判定结果 | 事务提交后调用或使用持久任务，并明确超时、幂等和补偿 |

## 样板维护

当新的纵向模块比现有样板更完整时，可以更新本目录的指向，但必须同时确认对应领域文档和自动化测试已经生效。不得因为一次局部修正就增加一条全局规则；只有稳定、可复用且会改变后续实现选择的经验才进入本目录或模块开发指南。

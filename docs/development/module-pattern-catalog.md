# 模块样板目录

本目录帮助开发者和 AI 在实现新模块前选择真实、当前的仓库样板。样板只证明对应维度的实现方式，不代表其中每一处代码都能无条件复制；具体业务语义以目标模块文档为准。

## 样板选择原则

1. 先按模块类型选择主样板，再按树、引用选择、敏感配置、状态命令等横切能力选择辅助样板。
2. 按实际变更维度检查样板与相关测试：后端看 Service/TxService 和涉及的迁移，前端看页面、注册和生命周期；跨层契约再补读关联部分。
3. 样板与当前 `AGENTS.md` 或架构文档冲突时，以当前规则为准，并记录样板需要演进的地方。
4. 不把当前模块的偶然字段、权限码、CSS 类名或业务状态复制为通用设计。

## 主样板

| 场景 | 推荐样板 | 重点观察 | 不可照搬的边界 |
| --- | --- | --- | --- |
| 标准主从业务聚合 | 后端 `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition`、前端 `smart-manage-web/src/domain/scm/procurement/purchaseRequisition`、`docs/domains/scm/procurement/purchase-requisition.md` | 主从保存/提交、乐观锁、编号、附件、LIST/EDIT 页签生命周期 | 采购字段、状态与组织角色规则 |
| 独立配置 LIST/EDIT | 后端 `smart-manage-api/src/main/java/sm/domain/sys/base/sysparam`、前端 `smart-manage-web/src/domain/sys/base/sysParam`、`docs/domains/sys/base/system-parameter.md` | Feature 归属、标准 `EditPage`、列表树和详情回显 | 系统参数树和配置语义 |
| 带独立状态命令与敏感配置 | 后端 `smart-manage-api/src/main/java/sm/domain/sys/message/email`、前端 `smart-manage-web/src/domain/sys/message/email`、`docs/domains/sys/message/email.md` | 启停不进保存 Form、凭据安全状态、RefSelector、记录详情和外部副作用 | 凭据与投递状态、外部副作用 |
| 树形主数据 | 后端 `smart-manage-api/src/main/java/sm/domain/sys/base/org`、前端 `smart-manage-web/src/domain/sys/base/org`、`docs/domains/sys/base/organization.md` | 树语义、父级引用、组织约束；页面形态不能脱离目标模块需求照搬 | 组织树与父子约束 |
| 用户列表与组织树 | `smart-manage-web/src/domain/sys/base/user/UserListPage.tsx`、`smart-manage-web/src/domain/sys/base/user/refSelector/useUserRefSelector.ts` | 组织根节点、默认范围、用户引用选择和批量选择 | 组织默认范围和任职关系 |
| 调度配置与执行记录 | `smart-manage-web/src/domain/sys/scheduler/job`、`smart-manage-web/src/domain/sys/scheduler/execution`、`docs/domains/sys/scheduler/job.md` | 配置 LIST/EDIT、状态命令、只读执行详情和运行态组装 | 调度执行状态与集群机制 |

## 横切能力样板

前端源码路径以下均相对 `smart-manage-web/src/`；后端路径相对仓库根目录。页面使用约束见[前端页面指南](./frontend-page-guide.md)，本表只索引实现。

| 场景 | 样板路径 | 观察重点 | 不可照搬的边界 |
| --- | --- | --- | --- |
| 页面注册 | `domain/scm/procurement/purchaseRequisition/pageRegistration.ts` | 显式稳定身份；协议见[页面注册约定](../architecture/page-registration-convention.md) | 不复制 Feature 和页面键 |
| 标准列表与选择 | `domain/scm/procurement/purchaseRequisition/PurchaseRequisitionListPage.tsx`、`domain/common/page/list/useListSelection.ts` | 查询与选择派生 | 清空选择时机由领域决定 |
| 标准编辑 | `domain/sys/base/sysParam/SysParamEditPage.tsx` | 页签生命周期与保存后同步 | 保存参数和 Query Key 留在领域 |
| 字段布局 | `domain/common/page/edit/FormFieldLayout.tsx`、`domain/common/page/edit/EditFormFields.tsx`、`domain/sys/base/user/UserProfileFields.tsx`、`layouts/PersonalCredentialModal.tsx` | 标准、复合与窄弹框布局；见[字段布局](./frontend-page-guide.md#表单字段布局) | 不复制宽度和断点 CSS |
| 可编辑明细 | `domain/common/component/EditableDetailTable.tsx`、`domain/sys/base/numberRule/NumberRuleEditPage.tsx`、`domain/scm/procurement/purchaseRequisition/PurchaseRequisitionEditPage.tsx`、`domain/sys/base/user/UserAssignmentTable.tsx` | 动态字段、聚合明细与引用关系；分区示例见下文 | 领域只维护业务列、控件与校验 |
| 实体引用 | `domain/common/component/RefSelector.tsx`、`domain/sys/base/org/refSelector/useOrgRefSelector.ts`、`domain/sys/base/user/refSelector/useUserRefSelector.ts`、`domain/sys/message/email/refSelector/useEmailAccountRefSelector.ts` | 树表、多选与候选范围；见[实体引用选择](./frontend-page-guide.md#实体引用选择) | 组织和用途必须按目标业务定义 |
| 业务弹框 | `domain/common/component/AppModal.tsx` | 标准弹框行为 | 独立详情页不能照搬成弹框 |
| 应用首页 | `domain/common/home/HomeCardGrid.tsx`、`domain/sys/monitor/home/MonitorHome.tsx`、`domain/sys/base/home/BaseHome.tsx` | 真实与示例数据；见[应用首页](./frontend-page-guide.md#应用首页) | 示例卡不能冒充真实数据，快捷入口按需选用 |
| 确认与反馈 | `domain/common/component/useOperationConfirm.ts`、`domain/common/component/OperationConfirmModal.tsx`、`domain/common/component/useOperationFeedback.tsx`、`api/errorPresentation.ts` | 风险与反馈策略；见[交互规范](./frontend-page-guide.md#弹框表格和视觉) | 不复制公共弹框或错误码映射 |
| 事务入口 | `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition/service` | 公开 Service 与 TxService | 不照搬状态机 |
| 复用事务内写能力 | `smart-manage-api/src/main/java/sm/domain/sys/base/user/service/UserWriter.java`、`smart-manage-api/src/main/java/sm/domain/sys/base/user/service/UserImportTxService.java` | 多入口共享 Writer | 不创建第二个事务 owner；跨领域须使用提供方 Contract |
| 权限常量 | `smart-manage-api/src/main/java/sm/domain/scm/procurement/purchaserequisition/constant/PurchaseRequisitionPermission.java` | Controller 引用常量 | 不复制权限码 |
| 架构与并发测试 | `smart-manage-api/src/test/java/sm/architecture/ArchitectureContractTests.java`、`smart-manage-api/src/test/java/sm/domain/scm/procurement/purchaserequisition/service/PurchaseRequisitionTxServiceTests.java` | 类型依赖、状态和乐观锁 | Mock 测试不能替代真实 PostgreSQL 验证 |
| 生成与权限校验 | `smart-manage-web/scripts/gen-registry.mjs`、`smart-manage-web/scripts/verify-permissions.mjs` | 生成输出与目录一致性 | 本地生成和 CI 判定见[质量验证](./verification.md#页面注册生成) |

### 明细分区示例

当前 `EditPage` 接收 `sections: EditPageSection[]`，类型及字段卡片构造器见 [editPageSection.tsx](../../smart-manage-web/src/domain/common/page/edit/editPageSection.tsx)。采购申请真实调用方将明细内容与操作区组合为同一个分区，简化示意如下（回调由领域页面实现）：

```tsx
const entrySection: EditPageSection = {
  key: 'entries',
  label: '明细信息',
  content: renderEntries,
  extra: renderEntryActions,
};
```

`content` 和 `extra` 均接收 `editable`；领域回调据此隐藏写操作，`EditPage` 在折叠时隐藏分区操作区。不要把该回调参数误认为 `EditSectionCollapse` 的展开状态。完整组合参考 [PurchaseRequisitionEditPage.tsx](../../smart-manage-web/src/domain/scm/procurement/purchaseRequisition/PurchaseRequisitionEditPage.tsx)，布局与选择约束见[编辑页](./frontend-page-guide.md#编辑页)。

## 样板维护

当新的纵向模块比现有样板更完整时，可以更新本目录的指向，但必须同时确认对应领域文档和自动化测试已经生效。不得因为一次局部修正就增加一条全局规则；只有稳定、可复用且会改变后续实现选择的经验才进入本目录或模块开发指南。

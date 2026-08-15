# 采购申请模块

采购申请是用于验证标准业务单据架构的可移除供应链模块，不属于系统内核。

## 当前定位

该模块已经完成人工纵向验收和关键后端、前端风险测试；可重复端到端验收仍在[项目路线图](../roadmap.md)中跟踪。它是当前唯一的标准业务聚合样板，不代表已经进入多模块并行扩展阶段。

## 模块边界

- 后端：`sm.domain.scm.procurement.purchaserequisition`
- 前端：`src/domain/scm/procurement/purchaseRequisition`
- 数据库：`t_scm_purchase_requisition`、`t_scm_purchase_requisition_entry`
- Flyway：结构与标准数据已收敛至当前 `V1`/`V2` 基线；后续变更只新增迁移。
- 云：`scm`
- 应用：`procurement`
- 组件键：`scm/procurement/purchase-requisition`
- 权限前缀：`scm:procurement:purchase-requisition`

采购申请使用标准单据字段 `biz_date` 记录业务日期；需求日期仍作为采购申请的领域专属日期保留。

采购申请编号在首次保存或直接提交的业务事务中由公共编号生成器产生，前端不预取、不占号，也不能修改。
编号引用键为 `scm/procurement/purchase-requisition.number`，内置规则键为
`scm/procurement/purchase-requisition`，默认按 `org_id` 和业务日期每天独立流水，格式为
`PR-{bill.bizDate:yyyyMMdd}-{seq:5}`。格式可以加入受控变量 `org.number`，其值由组织 ID 对应组织的
`number` 解析；流水始终使用稳定组织 ID 分段，历史号码不回收或重编。
数据库按 `(org_id, number)` 保证最终唯一性。

采购申请已注册附件资源类型 `scm.procurement.purchase-requisition`，并通过通用编辑页的独立“附件”折叠面板接入，不在采购模块重复维护上传组件和附件请求。编辑页上传的附件先以 TEMP 状态归属当前上传人和上传会话；保存或提交时与采购申请聚合在同一数据库事务中确认并绑定，已提交单据不允许继续维护附件。读取、下载和删除权限继承采购申请详情/保存权限与单据状态。文件大小、扩展名、MIME 和临时有效期统一由系统“附件配置”维护，不在采购申请模块单独配置。

系统内核不得反向依赖本模块。模块可以依赖用户、组织、权限、日志、异常和通用页面能力。

## 移除方式

仅在相关迁移从未进入共享历史时，才可以删除采购申请前后端目录，并通过新的迁移设计移除其专属菜单、权限、规则和数据表，随后重新生成前端组件注册表。Flyway 版本允许存在空缺。

数据库已经执行任一相关迁移后，禁止删除或修改历史迁移。应新增迁移，按“菜单与权限数据、明细表、主表、应用、云”的顺序卸载，并在确认没有业务数据后执行。

# smart-manage-web 前端代码审查报告（2026-08-06）

- **审查日期**：2026-08-06
- **复核日期**：2026-08-06
- **审查范围**：`smart-manage-web` 基础设施、领域页面、状态管理、安全与样式代码，并与后端接口和权限实现交叉核验
- **审查依据**：`smart-manage-web/AGENTS.md`、`docs/architecture/frontend.md`、`docs/architecture/security.md`

> 本文记录当前代码状态。修复任务统一维护在 [sys-domain-service-remediation-2026-08.md](./sys-domain-service-remediation-2026-08.md)。
>
> **整改更新（2026-08-06）**：本文确认的 F1-F9 已完成实现；组件键已集中、权限目录检查已进入 CI，并通过 V32 补齐检查发现的 7 个后端权限。下文保留审查时证据用于追溯，最终状态以整改清单为准。

## 总体结论

前端三层页面边界、Query Key Factory、工作台页签、白名单注册、CSP 测试和状态职责整体执行良好。未发现明确的 XSS 注入点或凭据直接泄露。

真实问题主要是保存期间继续编辑可能导致 dirty 状态被错误清除、组件键存在重复字符串来源、权限目录缺少单向一致性校验、少量样式规范违反和表格重复 key。原报告中“Modal 回显触发 dirty”“每次键入重注册关闭回调”“存在内存泄漏”等结论经复核不成立。

## 一、确认需要整改的问题

### F1. 保存期间继续编辑可能丢失 dirty 状态

**优先级**：P2  
**位置**：`src/domain/common/page/EditPage.tsx`

`setFieldsValue` 不触发 `onValuesChange`，因此详情回显不会误标 dirty；`ModalEditPage` 也没有 dirty 状态，原报告关于 Modal reset/回显的判断不成立。

真实风险是 `onSave`/`onSubmit` 为异步调用。如果请求期间表单仍可编辑，用户的新输入可能先把 dirty 设为 true，随后旧请求完成又执行 `setDirty(false)`。

**整改标准**：保存开始时记录表单修订号或值快照，成功后仅在期间没有新修改时清除 dirty；或者保存期间禁止编辑。不能简单把 dirty 重置移到领域 Mutation，因为 dirty 是 `EditPage` 内部状态。

### F2. beforeClose 回调可做稳定化优化

**优先级**：P3  
**位置**：`src/domain/common/page/EditPage.tsx`、`src/stores/workbench.ts`

`dirty` 从 false 变为 true 时 effect 会清理并重新注册一次；后续重复 `setDirty(true)` 不会导致每次键入都重注册。React effect 已返回注销函数，workbench 删除页签和销毁 workspace 也会清理回调，目前没有内存泄漏证据。

使用 ref 读取最新 dirty 可以减少注册变化，但属于维护性优化。

**整改标准**：若实施 ref 方案，应覆盖页签关闭、临时页签替换和 workspace 销毁后的回调清理测试；不得以“已确认内存泄漏”为依据扩大改造。

### F3. 权限集合每次渲染重建

**优先级**：P3  
**位置**：`src/domain/common/page/usePermissionAccess.ts`

相同 Query Key 已由 TanStack Query 去重并共享缓存，不存在每个按钮都发起独立网络请求的问题。`new Set` 可用 `useMemo` 稳定化，但应先结合实际列表规模判断收益。

### F4. 页面组件键存在重复字符串来源

**优先级**：P2  
**位置**：User、Menu、Role、App、SysParam、Job、Script 等列表/导航页面

导航页面和 `pageRegistration.ts` 分别维护相同字符串，改名可能只在运行时暴露。

**整改标准**：组件键放入独立 `componentKeys.ts` 或由注册生成器产出，注册表和页面共同依赖它。不要让页面反向导入会加载页面组件的 `pageRegistration.ts`，避免循环依赖。

### F5. 权限使用缺少一致性校验

**优先级**：P2  
**位置**：前端 access 声明、后端权限常量/注解、数据库最终权限目录

前端、后端和数据库承担不同职责，不要求三方集合全量相等：数据库可能包含无前端按钮的 API 权限，后端也可能存在仅服务端使用的能力。

**整改标准**：

- 前端实际使用的权限必须存在于权威权限目录；
- Controller 使用的权限必须存在于权威权限目录；
- 优先校验迁移执行后的最终数据或结构化权限清单，不直接解析并要求全部历史 Flyway SQL 与前端全量相等；
- 长期如需单一来源，应另行设计权限清单生成机制。

### F6. 内联 style 违反前端规范

**优先级**：P3  
**位置**：`SqlLogPage.tsx`、`ExecutionListPage.tsx`、`JobListPage.tsx`

三处 Select 宽度使用内联 style，违反前端 AGENTS.md。应迁移到 `sm-` 前缀 CSS 类。

### F7. SQL/脚本执行按钮未按权限隐藏或禁用

**优先级**：P2  
**位置**：`SqlConsolePage.tsx`、`ScriptConsolePage.tsx`

后端仍是最终安全边界，当前不是鉴权绕过；但页面壳层负责权限与按钮区，高风险按钮应按对应权限控制展示，避免普通用户点击后才收到 403。

### F8. 采购申请明细使用非唯一 rowKey

**优先级**：P2  
**位置**：`PurchaseRequisitionEditPage.tsx`

`rowKey="name"` 在重名物料下会产生重复 React key。应使用 Form.List 提供的稳定字段 key、持久化明细 ID 或专用客户端 UUID；不建议仅用可变化的数组索引处理可增删排序列表。

### F9. 命令成功提示与后续刷新失败未区分

**优先级**：P3  
**位置**：`src/domain/common/page/useCommandMutation.ts`

服务端命令成功后提示成功在业务上是真实的；`onSuccess` 中的缓存失效或重新查询失败属于后续界面同步失败。简单交换 `message.success` 和 `await onSuccess` 会在刷新失败时掩盖已经成功写入的事实。

**整改标准**：区分“命令执行失败”和“命令成功但页面刷新失败”；不得让同一次操作先后出现互相矛盾的成功/失败提示。

## 二、确认项和无需单独整改项

| # | 结论 | 说明 |
|---|---|---|
| F10 | 未发现明确 XSS 注入面 | 未发现 `dangerouslySetInnerHTML`、`eval`、`new Function`；动态内容由 React/Ant Design 渲染，CSP 有防漂移测试 |
| F11 | token 存 localStorage 是当前生效设计 | 与安全文档一致；风险由 CSP、编码约束和 401 清理共同控制，不在本次迁移 Cookie |
| F12 | 行内 `PermissionActions` 可接受 | Query 共享且去重，没有证据表明当前列表规模形成实际性能瓶颈；与 F3 一并观察，不单列整改 |
| F13 | `scriptId` 使用 string 正确 | 避免后端 Long/雪花 ID 在 JavaScript 中丢失精度 |
| F14 | 前端暂无通用附件组件是现状 | S3/MinIO 是后端共享存储要求；前端通用附件组件仍按真实上传、归属、下载和删除场景建设，不能只因存储类型变化增加空壳抽象 |

## 三、与后端策略需要统一的事项

### 重置密码后的会话处理

当前前端文案说明重置密码会让已有登录状态失效，与当前后端行为一致。后端若拆分缓存失效与会话终止，必须先确定安全策略，再同步调整前端文案和测试，不能仅按“减少下线”改变行为。

### 附件权限

前端按钮权限只能控制能力展示，不能替代后端附件创建人、上传会话、临时状态和业务单据归属校验。前端不得自行声明对象键、访问范围或把临时附件标记为公开；正式附件上传和绑定使用业务服务签发的上传会话或服务端确认的资源上下文。附件下载漏洞应先在后端最小修复；通用上传/下载组件属于后续业务能力。

## 四、建议实施顺序

1. F8 重复 rowKey、F7 高风险按钮权限展示。
2. F1 dirty 异步竞态、F4 组件键来源。
3. F5 权限目录校验。
4. F6 样式清理、F2/F3/F9 小型质量优化。

所有前端改动应执行 `pnpm lint`、`pnpm format:check`、`pnpm test` 和 `pnpm build`；涉及注册表时还应验证生成结果无差异。

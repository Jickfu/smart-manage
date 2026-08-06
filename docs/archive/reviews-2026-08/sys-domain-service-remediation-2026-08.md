# sys.domain Service 与前端整改清单（2026-08-06）

> 本清单基于 [后端复核报告](./sys-domain-service-review-2026-08.md) 和 [前端复核报告](./web-frontend-review-2026-08.md)，只纳入当前代码和已生效架构能够证明的问题。
>
> 目标部署架构已明确为 Nginx 负载均衡后的多应用实例，共享 PostgreSQL、Redis、S3/MinIO 和 Quartz JDBC JobStore。本清单同时维护当前缺陷和实现该目标架构所需的改造，但基础设施改造不得阻塞可独立完成的 P0 安全修复。

## 优先级与状态

- P0：已存在的越权、敏感数据泄露或不可接受的安全风险。
- P1：正确性、并发、安全策略或外部副作用风险。
- P2：中等风险、性能、契约一致性或重要可维护性问题。
- P3：低风险质量和优化事项。
- 待决策/待验证：证据或业务契约不足，不能直接实施预设方案。

状态使用：`待处理`、`待决策`、`待验证`、`已处理`、`已处理/待环境验收`、`无需处理`。其中“待环境验收”表示代码、迁移和部署配置已经完成，仅剩必须依赖真实双实例及共享基础设施的运行验证。

## 一、立即安全整改

| # | 优先级 | 问题 | 位置 | 验收标准 | 状态 |
|---|---|---|---|---|---|
| S1 | P0 | 附件下载缺少对象级授权 | `AttachmentController`、`AttachmentService` | 当前模型先校验临时附件创建人、正式附件业务归属和已注册资源策略；目标模型中 TEMP 同时校验上传者和上传会话，ACTIVE 通过 `BusinessResourceRegistry` 继承业务对象读权限；未知 bizType 默认拒绝；补越权自动化测试 | 已处理 |
| S2 | P1 | 缓存失效和会话终止职责耦合 | `AuthorizationStateHelper`、`UserService`、`RoleService` | 拆分授权状态失效和会话终止；禁用、管理员重置密码和安全事件终止全部会话；启用、分配角色和修改角色权限只刷新授权状态；前端文案一致 | 已处理 |
| S3 | P2 | 客户端 IP 缺少可信代理边界 | `ClientIpResolver`、部署配置 | 只信任来自配置代理网段/跳数的转发头；入口代理覆盖客户端伪造值；非受信来源使用 remoteAddr；`X-Real-IP` 不能作为单独修复 | 已处理 |

> S1 不依赖存储策略、S3/MinIO、附件分页或模型重构，应先按当前模型完成最小安全修复。

## 二、后端正确性与性能

| # | 优先级 | 问题 | 位置 | 验收标准 | 状态 |
|---|---|---|---|---|---|
| B1 | P1 | 菜单编辑无乐观锁 | `MenuEntity`、`MenuSaveForm`、`MenuTxService`、迁移 | 新增 Flyway 迁移为菜单增加 version；编辑必须携带版本；并发旧版本更新返回数据冲突；自定义全字段更新包含 version 条件和递增 | 已处理 |
| B2 | P1 | 菜单删除不校验子节点 | `MenuTxService.deleteById` | 存在子菜单时明确拒绝删除；无子节点时正常删除；补树约束测试 | 已处理 |
| B3 | P2 | 基础资料列表和父链查询效率低 | `BasicDataService` | `listPage` 批量预加载分类；`parentOptions` 基于一次加载的父索引判断后代；查询次数不随结果行数线性增长 | 已处理 |
| B4 | P2 | 后代路径更新重复线性扫描 | `BasicDataTxService` | 为分类节点构建 ID 索引或父子索引；路径级联结果与现有语义一致；补移动、改名、改编码和深层树测试 | 已处理 |
| B5 | P2 | Redis 专用异常分支不可达 | `ExceptionResultResolver` | Redis 连接和序列化异常在宽泛 `DataAccessException` 之前处理；响应映射符合错误语义；补异常继承顺序测试 | 已处理 |
| B6 | P2 | Arthas 会话资源无上限 | `ArthasService` | 限制并发会话数和单会话输出；结束、异常或空闲超时后从 Map 回收；线程和连接释放有测试 | 已处理 |
| B7 | P2 | 临时附件清理目录硬编码 | `CleanTempFileJob` | 从附件元数据记录的存储类型和对象键清理共享存储；配置切换不误用当前目录解释历史对象；失败可幂等重试 | 已处理 |
| B8 | P2 | 附件 promote 补偿没有记录原位置 | `AttachmentTxService` | 采用“不移动 TEMP 对象”的目标模型；promote 只更新数据库状态和归属，消除跨存储移动补偿 | 已处理 |
| B9 | P2 | 复杂树组装使用嵌套 Stream | `BasicDataService.categoryTree` 等 | 批量加载关联数据并建立显式索引；使用普通 for 循环组装树；不得在嵌套 Stream 中反复扫描集合；结果顺序和内容保持一致 | 已处理 |

## 三、多实例与对象存储架构整改

| # | 优先级 | 事项 | 位置 | 验收标准 | 状态 |
|---|---|---|---|---|---|
| M1 | P1 | 缓存和登录状态跨实例一致 | JetCache、Sa-Token、验证码、改密票据 | 业务共享状态使用 Redis；业务 LOCAL 缓存已移除；敏感文件配置不缓存；事务提交后失效 | 已处理/待环境验收 |
| M2 | P1 | Quartz JDBC 集群 | Quartz 配置、调度同步、执行日志 | 共享 PostgreSQL JobStore、集群检查、AUTO 实例 ID 和既有 Quartz 基线表已启用；故障切换留待真实双实例验证 | 已处理/待环境验收 |
| M3 | P1 | 任务互斥跨实例生效 | `ManagedJobDispatcher`、`mutexKey` | JVM 锁替换为 PostgreSQL 会话级 advisory lock；连接即所有者、异常断连自动释放；争锁失败记录 SKIPPED | 已处理/待环境验收 |
| M4 | P1 | S3/MinIO 共享附件存储 | `FileStorageService`、附件服务、配置与迁移 | 已增加 S3/MinIO 实现、私有 Bucket 短时签名 URL、注册项派生随机对象键和 S3 配置迁移；跨节点读写待环境验收 | 已处理/待环境验收 |
| M5 | P1 | 对象存储安全与生命周期 | 附件模型、业务资源注册、存储配置 | 已实现显式注册 fail-fast、完整状态机、上传会话、过期清理、内容校验和对象级授权；TEMP 不移动，旧路径字段已迁移删除 | 已处理 |
| M6 | P2 | Nginx 与可信代理边界 | 部署配置、`ServletUtil` | 已提供 TLS/双上游/头覆盖/上传限制/限流参考配置并实现可信代理解析；无粘性双实例调用待环境验收 | 已处理/待环境验收 |
| M7 | P2 | 节点本地能力治理 | Arthas、脚本 Semaphore、LOCAL 缓存 | Arthas 会话显式返回并校验实例 ID；脚本许可改为数据库跨实例锁；业务 LOCAL 缓存已移除 | 已处理 |
| M8 | P2 | 多实例可观测性与发布 | 健康检查、日志、连接池 | 日志模式含实例 ID/Trace ID，启用存活与就绪探针、优雅停机，生产连接池默认值按实例总量核算 | 已处理/待环境验收 |

## 四、后端契约与质量

| # | 优先级 | 问题 | 位置 | 验收标准 | 状态 |
|---|---|---|---|---|---|
| Q1 | P2 | 登录失败与待改密审计契约不完整 | `LoginService`、`AuthListener`、登录模块文档 | 正式会话记录 LOGIN_SUCCESS；凭据正确但待改密记录 PASSWORD_CHANGE_REQUIRED；失败记录 LOGIN_FAILURE；认证时生成不可变快照；不记录票据、密码或验证码 | 已处理 |
| Q2 | P2 | 强制下线事件监听不完整 | `AuthListener` | 分别记录 LOGOUT、SESSION_KICKED、SESSION_REPLACED、ACCOUNT_DISABLED、PASSWORD_RESET_TERMINATED；禁止统一记为 logout | 已处理 |
| Q3 | P2 | 验证码字符集与比较策略不匹配 | `CaptchaUtil`、`LoginService` | 明确目标状态空间；若统一大小写，应用长度补偿强度；排除易混淆字符；保留现有有效期，且不把字符集调整当作频率限制替代品 | 已处理 |
| Q4 | P3 | Argon2/SM2 使用静态配置状态 | `Argon2Helper`、`SM2Helper` | 复核未发现运行期配置变更或已复现并发错误；本次不为假设扩大密码链路重构范围 | 无需处理 |
| Q5 | P3 | SM2 Helper 保留打印密钥的开发入口 | `SM2Helper` | 删除 `main`、密钥打印和只用于本地生成的遗留方法；仓库代码和测试输出不打印密钥 | 已处理 |
| Q11 | P1 | 浏览器 SM2 密文偶发解密失败并穿透为 500 | `SM2Helper`、`LoginService`、安全架构文档 | 浏览器库省略 C1 的 `04` 前缀；后端专用入口无条件按该协议补齐，避免原始坐标以 `02/03/04` 开头时被 Hutool 误判；无法解密统一返回参数异常并写登录失败审计 | 已处理 |
| Q6 | P3 | 编辑用户重复写回 username | `UserTxService.save` | 编辑分支校验不可修改后不再重复赋值；保留表单非空校验 | 已处理 |
| Q7 | P3 | like 写法和通配语义不明确 | Cloud/Job 等查询 | 统一 MyBatis-Plus 写法；逐接口决定 `%`/`_` 是通配还是字面字符；只有要求字面搜索时才实现显式转义 | 已处理 |
| Q8 | P3 | 常量类和列长度常量不统一 | `UserConstant`、日志相关类 | 常量类 final + 私有构造；只收敛有明确数据库/协议含义的长度常量，不增加无价值抽象 | 已处理 |
| Q9 | 待验证 | 脚本超时关闭可能存在输出竞态 | `ScriptExecutor` | 高频输出直到超时的压力测试未复现竞态；保留现有 Context 关闭逻辑，不增加无证据的同步复杂度 | 已处理 |
| Q10 | P1 | 任务执行日志落库失败策略 | `JobExecutionListener`、调度模块文档 | RUNNING 写入 fail closed；结果写入失败告警并尽力标记 UNKNOWN；记录实例 ID、fire instance ID、Trace ID | 已处理/待环境验收 |

## 五、前端整改

| # | 优先级 | 问题 | 位置 | 验收标准 | 状态 |
|---|---|---|---|---|---|
| F1 | P2 | 保存期间继续编辑可能错误清除 dirty | `EditPage.tsx` | 保存前记录修订号，只有响应返回时修订号未变化才清除 dirty | 已处理 |
| F2 | P3 | beforeClose 回调可稳定化 | `EditPage.tsx`、`workbench.ts` | dirty 使用 ref，回调不随每次编辑重注册；沿用 Store 现有销毁和 key 迁移清理 | 已处理 |
| F3 | P3 | 权限 Set 每次渲染重建 | `usePermissionAccess.ts` | 权限集合按 Query 数据使用 `useMemo` 稳定化 | 已处理 |
| F4 | P2 | 页面组件键存在硬编码副本 | 各列表页、`pageRegistration.ts` | 独立 `componentKeys.ts` 由注册和导航共同引用，无页面反向依赖注册表 | 已处理 |
| F5 | P2 | 权限使用缺少目录校验 | access 声明、Controller 权限、最终权限目录 | 新增权限目录校验脚本并接入 CI；同时通过 V32 补齐首次校验发现的 7 个缺失权限 | 已处理 |
| F6 | P3 | 三处内联 style 违反规范 | SqlLog、ExecutionList、JobList | 已改为 `sm-` 前缀 CSS 类 | 已处理 |
| F7 | P2 | SQL/脚本执行按钮未按权限控制展示 | SqlConsole、ScriptConsole | 执行按钮按 execute 权限隐藏，后端鉴权保持不变 | 已处理 |
| F8 | P2 | 采购申请明细 rowKey 不唯一 | `PurchaseRequisitionEditPage.tsx` | 使用 Ant Design Form.List 稳定 `field.key`，删除时映射回实时索引 | 已处理 |
| F9 | P3 | 命令成功与刷新失败提示未区分 | `useCommandMutation.ts` | onSuccess 刷新失败时提示“操作已成功，但页面数据刷新失败”，不进入命令失败提示 | 已处理 |

## 六、无需处理或已确认项

| # | 原结论 | 处理结论 |
|---|---|---|
| C1 | 公开 Service 的 `@CacheInvalidate` 缺少事务提交钩子 | 无需处理。当前 TxService 在公开 Service 返回前提交；通过架构约束防止未来增加外层事务 |
| C2 | 所有批量启停必须携带 version | 无需处理。幂等目标状态命令不同于 Job 状态机转换，具体聚合有额外规则时单独设计 |
| C3 | `MenuService.detail` 是 N+1 | 无需处理。固定最多两次查询，不随结果集增长 |
| C4 | 高风险 Controller 权限码缺失 | 已处理。当前 Controller 权限码 + Service administrator 复核符合安全文档 |
| C5 | 必要唯一索引缺失 | 无需处理。当前迁移已有必要约束，新增聚合继续逐项检查 |
| C6 | Modal 回显触发 dirty | 无需处理。`setFieldsValue` 不触发 `onValuesChange`，且 Modal 当前无 dirty 状态 |
| C7 | beforeClose 每次键入重注册并已造成内存泄漏 | 结论不成立，仅保留 F2 的可选稳定化优化 |
| C8 | 行内 `PermissionActions` 必须优化 | 无需单列。Query 已共享并去重，无实际性能证据 |
| C9 | token 应立即从 localStorage 迁移 Cookie | 不在本次范围，当前安全文档明确保留 localStorage 方案 |
| C10 | 前端暂无通用附件组件可以直接推导组件设计 | 结论不成立。S3/MinIO 已是后端目标架构，但前端组件仍需按真实业务交互设计 |

## 七、实施顺序

```text
第一批（立即安全）：S1
第二批（正确性）：B1 → B2 → S2（决策后）→ B5
第三批（共享基础）：M1 → M4 → M5 → M6
第四批（调度集群）：M2 → M3 → M7 → M8
第五批（性能与资源）：B3 → B4 → B9 → B6 → B7 → B8
第六批（前端正确性）：F8 → F7 → F1 → F4 → F5
第七批（契约与质量）：Q1/Q2/Q10（决策后）→ Q3 → Q4-Q9 → F2/F3/F6/F9
```

依赖原则：

- S1 不依赖附件存储重构。
- B1 涉及迁移，完成后执行 Flyway 空库验证。
- S2、Q1、Q2、Q10 已按模块文档明确策略，实施不得改变事件和 fail-closed/UNKNOWN 语义。
- F4 使用独立键模块，不从页面导入注册表。
- S1 的当前模型安全修复不依赖 M4/M5；对象存储改造完成后必须复用同一对象级授权规则。

## 八、验证要求

- 后端改动执行 `mvn test`；迁移改动执行 Flyway 空库验证。
- 前端改动执行 `pnpm lint`、`pnpm format:check`、`pnpm test`、`pnpm build`。
- 涉及页面注册时执行注册生成命令并确认生成结果无差异。
- 安全、事务、乐观锁、状态策略、文件补偿和资源回收必须补风险驱动测试。
- 待验证项应记录测试条件和结论；未复现的问题不得伪装成已修复缺陷。
- 多实例验收至少使用两个应用实例，覆盖跨节点缓存可见性、登录态、同一 Trigger 唯一执行、mutexKey 互斥、对象上传后跨节点下载以及单实例退出后的故障恢复。

## 九、本次实施记录

已完成的自动化验证：

- 后端全量单元测试与编译；
- 前端 ESLint、Prettier、Vitest、TypeScript 与生产构建；
- 前后端 81 个实际使用权限到 Flyway 权威目录的单向校验；
- Flyway V1-V32 PostgreSQL 16 空库迁移；
- S3 对象键格式和路径穿越边界、附件授权/生命周期、菜单乐观锁、Redis 异常、可信代理、脚本超时高频输出、任务互斥与执行日志策略测试。

以下验证依赖目标环境，不以本地单进程测试替代，发布前必须执行：

1. 两个应用实例使用不同 `SMART_MANAGE_INSTANCE_ID`，共同连接 PostgreSQL、Redis 和同一私有 MinIO Bucket；
2. 经 Nginx 轮询连续登录、刷新权限和读取基础资料，确认不需要会话粘性；
3. 节点 A 修改共享配置后由节点 B 立即读取，事务回滚场景不得出现新值；
4. 同一 Trigger 和相同 `mutexKey` 在两节点并发触发时，只允许一个业务执行，另一条日志为 `SKIPPED`；
5. 节点 A 上传对象后由节点 B 完成授权下载；TEMP 过期和 PENDING_DELETE 可由任一节点清理；
6. 执行中停止一个实例，验证 Quartz 故障恢复、业务幂等、UNKNOWN 对账、就绪探针摘流和优雅停机。

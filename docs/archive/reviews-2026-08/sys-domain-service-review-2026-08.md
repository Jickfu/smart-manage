# sys.domain Service 代码审查报告

- **审查日期**：2026-08-06
- **复核日期**：2026-08-06
- **审查范围**：`smart-manage-api` 中 `sm.domain.sys` 的公开 Service、TxService、Converter、Mapper 及相关基础设施
- **审查方式**：结合当前架构、模块文档、迁移、测试和实现逐项复核

> 本文记录当前代码状态。后续代码变化后，引用结论前必须重新核对对应实现。
>
> **整改更新（2026-08-06）**：本文识别的可实施问题已按[整改清单](./sys-domain-service-remediation-2026-08.md)完成代码、迁移与文档整改；下文保留审查时证据用于追溯，不再代表整改后的现状。真实双实例、共享 MinIO 和故障切换项目仍按清单标记为“已处理/待环境验收”。

## 总体结论

项目的公开 Service、包级 TxService、业务日志和聚合组织方式整体符合当前架构。需要优先处理的真实问题是附件对象级越权、菜单并发与父子约束、基础资料查询与组装可读性、Redis 异常分类顺序和 Arthas 会话资源上限。目标部署架构现已明确为 Nginx 负载均衡后的多应用实例，还需要系统治理共享缓存、对象存储、Quartz 集群和节点本地状态。

原始审查中的部分结论经复核不成立或严重度失真：公开 Service 上的 `@CacheInvalidate` 当前在 TxService 事务提交后执行；通用幂等启停命令不需要机械套用 Job 状态机的 version；`MenuService.detail` 不是 N+1；Redis 连接异常实际先被 `DataAccessException` 捕获；任务执行日志不能套用通用异步日志规则。

## 一、确认需要整改的问题

| # | 优先级 | 问题 | 位置 | 复核结论 |
|---|---|---|---|---|
| 1 | P2 | 登录审计契约不完整 | `LoginService.login` | 用户名/密码失败返回失败 VO，验证码失败抛异常；首次改密票据没有对应审计事件。应先明确失败响应和“待改密”事件契约，再统一实现 |
| 2 | P3 | 编辑用户时重复写回 username | `UserTxService.save` | 已验证 username 不可修改后仍写回同值，无正确性风险，属于可读性清理 |
| 3 | P1 | 菜单缺少乐观锁，删除不校验子节点 | `MenuEntity`、`MenuSaveForm`、`MenuTxService` | 可修改菜单未使用 version；删除父菜单可能留下悬挂节点，违反可修改聚合与树结构约束 |
| 4 | P1 | 缓存失效与会话终止职责耦合 | `AuthorizationStateHelper` | 启用、禁用、分配角色和重置密码统一执行 logout，无法表达不同安全策略；应拆分清缓存与终止会话，由具体命令决定 |
| 5 | P2 | 附件 promote 补偿目标硬编码 | `AttachmentTxService` | `move(promotedPath, "temp")` 在当前存储实现下可回到临时目录，但没有记录原路径，后续策略变化时不稳健 |
| 6 | P3 | Argon2/SM2 Helper 使用静态配置状态 | `Argon2Helper`、`SM2Helper` | 当前配置仅在启动期写入，未证明存在运行期并发错误；问题主要是生命周期、测试性和 Spring 依赖语义不清 |
| 7 | P3 | like 写法不统一 | `CloudService`、`JobService` 等 | 手工拼 `%` 冗余；改用 `qw.like` 不会自动转义用户输入中的 `%`/`_`，是否允许通配必须由查询契约决定 |
| 8 | P2 | 基础资料查询和树判断效率低 | `BasicDataService`、`BasicDataTxService` | `listPage` 按分类查询、`parentOptions` 循环查父节点、`isDescendant` 重复线性扫描均成立；应分别按 DB 查询次数和内存复杂度治理 |
| 9 | P3 | 常量类与列长度常量不统一 | `UserConstant`、`AuthListener`、`LogWriteService` 等 | 属于维护性问题，不影响当前正确性 |
| 10 | P2 | 验证码字符集与忽略大小写策略不匹配 | `CaptchaUtil`、`LoginService` | 大小写混合生成但忽略大小写比较会降低有效状态空间；若统一大小写，应通过增加长度等方式保持目标强度 |
| 11 | P0 | 附件下载缺少对象级授权 | `AttachmentController.download`、`AttachmentService` | 当前仅要求登录，知道附件 ID 的用户可尝试下载其他业务对象的附件。正式附件应通过轻量 `BusinessResourceRegistry` 继承所属业务对象读权限，不采用创建人独占或逐附件授权；该注册表不是动态模型平台 |
| 12 | P2 | 高风险路径测试不足 | Menu、BasicData、Attachment、AuthorizationStateHelper 等 | 应补菜单并发与树约束、路径级联、附件越权与补偿、会话失效策略测试，不以机械覆盖率为目标 |
| 13 | P2 | 复杂树组装使用嵌套 Stream | `BasicDataService.categoryTree` 等 | 虽然部分链路只做内存转换，但嵌套扫描降低可读性并隐藏复杂度；应批量加载后建立索引，使用普通 for 循环显式组装 |

## 二、补充发现

| # | 优先级 | 问题 | 位置 | 复核结论 |
|---|---|---|---|---|
| N1 | P3 | 登录日志异步读取用户缓存快照 | `AuthListener` | 可能记录变更前的昵称等展示信息；审计主体应优先使用认证时快照或明确缓存一致性要求 |
| N2 | P2 | 下线事件监听处理不完整 | `AuthListener` | `doKickout`、`doReplaced`、`doDisable` 为空；应先定义需记录的事件类型，避免把强制下线误记为主动退出 |
| N3 | 待验证 | 脚本超时关闭与输出并发 | `ScriptExecutor` | 现有代码未证明 `context.close(true)` 会并发写输出流；先补超时压力测试，有复现后再决定是否同步 |
| N4 | P2 | Arthas 会话输出和会话表无上限 | `ArthasService` | 持续输出会无限增长；自然结束或异常结束的会话仍可能留在 Map，应增加输出上限、并发上限和回收策略 |
| N5 | P2 | 临时附件清理目录硬编码 | `CleanTempFileJob` | 配置修改后清理任务可能继续操作旧目录，应从存储配置或附件策略读取 |
| N6 | P3 | SM2 Helper 保留密钥打印开发入口 | `SM2Helper` | `main` 和私有生成方法会打印密钥，不应保留在拟开源生产代码中 |
| N7 | P2 | Redis 专用异常分支不可达 | `ExceptionResultResolver` | `RedisConnectionFailureException` 是 `DataAccessException` 子类，当前先落入持久化错误分支；应调整分支顺序并补测试 |
| N8 | P2 | 客户端 IP 缺少可信代理边界 | `ServletUtil` | 不能仅把 `X-Forwarded-For` 改成 `X-Real-IP`；必须只信任受控代理来源和代理覆盖后的转发头 |
| N9 | 待决策 | 任务审计写入失败时是否继续任务 | `JobExecutionListener` | 调度执行实例不是通用异步操作日志。应明确 fail-closed/fail-open 策略；若继续执行，必须有告警和审计补偿，不能静默忽略 |

## 三、经复核不成立或无需整改的原结论

### 1. `@CacheInvalidate` 事务提交时序

公开 Service 按架构不声明事务，调用的包级 TxService 在返回前已经完成事务提交。因此 `UserService`、`SysParamService`、`UiConfigService`、`FileConfigService` 上的失效注解当前不会出现“事务回滚但缓存已清”的路径。

只有在事务内部直接操作缓存时，才需要 `TransactionUtil.afterCommit`。应通过架构约束防止未来给公开 Service 增加外层事务，而不是重复增加事务钩子。

### 2. 通用批量启停必须使用 version

`EnabledCommandUtil` 表达幂等的“设置为目标状态”命令，只更新 enabled 字段。`JobTxService.updateStatus` 表达受限状态机转换，两者语义不同。没有具体模块规则时，不应要求所有批量启停携带 version。

### 3. `MenuService.detail` 是 N+1

该方法最多查询当前菜单和一个父菜单，查询次数固定，不是随结果集增长的 N+1。基础资料列表和父链查询仍需优化。

### 4. 权限校验策略不统一

SQL、脚本、缓存、任务等高风险 Controller 已有权限码，公开 Service 再校验 administrator，符合安全文档的双重校验要求。本项维持已处理。

### 5. 唯一索引缺失

当前迁移已为用户、角色、任务、系统参数、权限等需要唯一性的字段建立数据库约束。本项无需整改；新增聚合仍必须逐项检查。

## 四、建议实施顺序

1. **立即安全修复**：附件对象级授权及越权测试，不等待存储模块重构。
2. **正确性**：菜单 version/子节点约束、会话失效策略拆分、Redis 异常顺序。
3. **资源与性能**：BasicData 查询、Arthas 上限与回收、临时目录配置。
4. **多实例基础设施**：共享缓存及跨节点失效、S3/MinIO、Quartz JDBC 集群、分布式互斥和节点状态治理。
5. **契约与质量**：登录审计、验证码策略、Helper 静态设计、常量和测试补齐。

多实例和对象存储已经成为目标架构要求，但当前实现尚未满足。实施时仍须先完成可独立修复的附件越权，不能让基础设施改造延迟 P0 安全修复。

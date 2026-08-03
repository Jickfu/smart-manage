# 后端架构

## 分层边界

- `sm.framework`：CORS、Sa-Token、JSON、Redis、MyBatis-Plus、请求加解密和 Trace ID 等第三方框架配置。
- `sm.system`：自定义注解、全局异常、实体基类、拦截器、监听器、存储抽象和工具等系统公共能力。
- `sm.domain.{领域}`：领域模块。
- `sm.domain.{领域}.{应用}`：领域内应用。
- `sm.domain.{领域}.{应用}.{单据}`：业务单据或子模块。
- 领域或应用内的公共能力放在对应 `common` 包。

公共层不得吸收具体领域语义。可选业务领域可以依赖系统公共能力，系统内核禁止反向依赖可选业务领域。

## 接口基础设施

所有接口返回 `Result<T>`，包含 `code`、`msg`、`data` 和 `traceId`。成功使用 `Result.success(data)`，异常由 `GlobalExceptionHandler` 统一转换。

接口访问级别按注解判定：

- `@SaIgnore`：公开接口；
- `@SaCheckPermission`：权限接口；
- 无上述注解：由全局过滤器执行登录校验。

分页入参继承 `PageForm`，分页结果使用 `PageData<T>`。Controller 只依赖公开 Service，禁止直接依赖 Mapper 或 TxService。

## Service 与事务

公开 `*Service` 是单据唯一业务入口，负责查询、业务命令、权限补充校验、操作日志入口和业务组装。

写操作委托给同目录、包级可见的 `*TxService`。TxService 使用类级 `@Transactional(rollbackFor = Exception.class)`，Controller 和其他单据不能直接依赖它。事务内的存在性、唯一性和状态检查直接使用 Mapper，避免绕过事务边界调用 Service 缓存方法。

资源不存在、状态非法、无权限、参数不合法或持久化结果异常时必须抛出明确异常，禁止用 `null` 或静默忽略表示失败。

## 操作日志

`@BizLog` 只标注在公开 Service 的业务命令方法：

- 保存、提交、审核、删除、启停和高风险执行等命令需要记录；
- 列表、详情、选择和默认值查询不记录；
- Controller 和 TxService 禁止标注；
- 同一业务调用链只记录一次；
- 登录和退出使用独立认证日志。

日志不得记录令牌、密码、验证码、私钥、密钥或存储凭据。

## 映射与组装

模块内 `*Converter` 使用 MapStruct 完成 Entity 到列表、选择、明细和基础详情 VO 的纯字段映射。Converter 不得依赖 Mapper、Service、缓存、安全上下文或外部资源。

需要查询、权限判断、默认值、状态规则、树结构或主从聚合的转换属于业务组装，保留在公开 Service 并使用 `assemble*` 命名。MyBatis 联表查询可以直接投影 VO。

## 缓存、文件和任务

- 高频低变更配置可以使用 Caffeine 本地缓存；缓存名称集中管理，写操作在事务提交后失效。
- 用户认证状态使用 Redis。
- 缓存监控只通过公开 `CacheService` 提供业务入口；Redis 原始访问属于模块内部技术能力，不形成第二个公开 Service。
- `FileStorageService` 抽象 Local、FTP 等存储实现；领域负责提供配置并解密敏感字段。
- 数据库事务和外部文件存储不能原子提交，写操作必须定义补偿语义。
- 异步日志、线程池和 Quartz 执行必须显式传播并清理 Trace ID。
- 当前只声明单节点运行边界；多节点部署前必须重新验收缓存失效、Quartz 集群、共享文件和故障切换。

强制编码规则由 [后端 AGENTS.md](../../smart-manage-api/AGENTS.md) 定义。

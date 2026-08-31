# 后端架构

本文档是后端分层、依赖、接口、Service/事务、数据实现和技术边界的权威来源。具体业务状态与不变量归对应领域文档，验证命令归[质量验证](../development/verification.md)，本文档不重复维护。

内建监控的 Host/Instance 边界、唯一采样链、状态机和 Redis/PostgreSQL 职责见[内建监控架构](./monitoring.md)。
监控 collector 按职责局部降级，当前遥测缺失通过明确状态表达，不使用业务异常伪装为页面整体失败。

## 分层边界

- Java 根包为 `sm`。
- `sm.infrastructure`：第三方技术、外部设施和技术适配，例如 CORS、JSON、Redis、MyBatis-Plus、数据源与出站 HTTP 客户端配置。
- `sm.system`：不属于任何具体业务领域、供多个领域共享的 Smart Manage 系统内核能力，例如统一响应、异常体系、安全上下文、认证授权公共能力、DataScope Contract、通用事务辅助及稳定 SPI。
- `sm.domain.{领域}`：领域模块。
- `sm.domain.{领域}.{应用}`：领域内应用。
- `sm.domain.{领域}.{应用}.{模块}`：应用内最小的内聚能力模块，可以是业务聚合、主数据、配置、查询记录或监控能力。
- 领域或应用内的公共能力放在对应 `common` 包。

`sm.domain.sys` 是系统管理业务领域，与 `sm.domain.scm` 同级；用户、组织、角色、权限、菜单、应用、功能、调度、消息、基础资料和系统参数等具有完整业务生命周期的模块都保留在该领域。不得因名称中的 `sys` 将这些业务迁入 `sm.system`。

顶层依赖方向为 `domain -> system -> infrastructure`：Domain 可以依赖 System；System 可以依赖 Infrastructure；Infrastructure 不得依赖 System 或 Domain，System 不得依赖 Domain。Domain 不得任意依赖 Infrastructure，目前只开放 `sm.infrastructure.mapping.SmMapperConfig` 这一明确技术 Contract。

## 跨领域协作原则

本文中的 `A -> B` 表示 A 依赖 B。不同顶级 Domain 默认相互隔离，不允许直接依赖其他 Domain 的内部实现。`sm.domain.sys` 与其他顶级 Domain 适用完全相同的规则，不具有特殊的公共领域地位。

当真实业务首次产生跨领域协作需求时，由能力提供方基于该实际用例按需暴露最小稳定 Contract，调用方只能依赖目标 Domain 的 `contract` 包。Contract 是领域对外发布的稳定业务语言，不是 Service 的模型镜像或形式上的代理接口；可以包含公开接口、当前用例必要的 Command、Query、Reference、Result、稳定枚举和值对象，不使用面向 Controller 的 Form、VO，也不得暴露具体 Service、Mapper、Entity、TxService、持久化 Wrapper 或 MyBatis 类型。

```text
Domain A
    -> Domain B.contract
```

Smart Manage 遵循：**先实现领域，后发现协作，再提取 Contract。** Contract 由真实消费者和真实用例塑造，而不是由对未来需求的猜测产生。真实消费者可以位于当前仓库，也可以位于已经存在且用例可核实的独立扩展项目；不得仅因某能力未来可能复用而提前建设 Contract。没有真实跨领域消费者的 Domain 不需要 `contract` 包；同一顶级 Domain 内的应用和模块也不因潜在复用而提前升级为独立边界。

当前附件和编号规则存在采购领域这一真实消费者，用户引用与状态校验也存在已核实的独立业务领域消费者，因此其最小跨领域接口和边界模型位于 `sm.domain.sys` 对应模块的 `contract` 包。消费者项目只用于证明用例，不反向成为 Smart Manage 的架构或业务事实来源。

`sm.system` 中不属于任何业务领域的稳定系统机制可以由所有 Domain 直接依赖；`sm.domain.sys` 仍是系统管理业务领域，不因基础性或通用性获得跨领域直连 Service 的例外。提供方模块内部可以使用 Mapper、内部协作者或 Service；同一顶级 Domain 的其他应用需要领域内部能力时可以依赖职责明确的公开 Service，只需要已发布 Contract 的相同语义时优先使用 Contract；其他顶级 Domain 必须使用目标 Domain 的 Contract，禁止自行查询目标领域数据表、复制状态判断或维护目标领域数据缓存。

公开 `*Service` 按清晰、内聚的业务职责划分；同一模块可以有多个不同语义的公开 Service，例如用户管理、当前用户资料、认证和授权边界。不形成独立业务入口的技术协作者不得命名为 `*Service`，应按职责使用 `*Accessor`、`*Gateway` 等名称并尽量保持包级可见。`sm.system.storage` 只能通过 `FileStorageConfigProvider` 获取配置，不得依赖 `sm.domain.sys` 的实体或 Service。

DataScope 的角色配置、Entity、Mapper 和规则解析实现保留在 `sm.domain.sys.base.datascope`；跨领域消费的 `DataScope` 与 `DataScopeResolver` 位于 `sm.system.datascope`，由 `DataScopeService` 实现。业务领域只依赖该系统内核 Contract。

当前技术类归属如下：

| 原职责 | 正式位置 |
| --- | --- |
| CORS 配置与属性 | `sm.infrastructure.web` |
| Druid、MyBatis-Plus 配置 | `sm.infrastructure.persistence` |
| Redis 配置 | `sm.infrastructure.cache` |
| Jackson 配置与 Long 序列化 | `sm.infrastructure.json` |
| MapStruct 公共配置 | `sm.infrastructure.mapping` |
| 出站 HTTP 客户端配置 | `sm.infrastructure.http` |
| Sa-Token 配置、浏览器请求安全 | `sm.system.security` 子包 |

## 接口基础设施

所有接口返回 `Result<T>`，包含 `code`、`msg`、`data` 和 `traceId`。成功使用 `Result.success(data)`，异常由 `GlobalExceptionHandler` 统一转换。

接口访问级别按注解判定：

- `@SaIgnore`：公开接口；
- `@SaCheckPermission`：权限接口；
- 无上述注解：由全局过滤器执行登录校验。

系统功能、菜单和权限的归属必须遵守[功能、菜单与权限模型](./feature-and-permission.md)。权限直接归属 `Feature`，应用由功能推导；菜单与入口权限必须属于同一功能，写入时必须校验该约束。

Controller 中的 `@SaCheckPermission` 必须引用所属模块 `constant` 包内的权限常量，禁止直接书写权限码字符串。JSON 反序列化、ID 转换和持久化结果不得静默吞错。

分页入参继承 `PageForm`，分页结果使用 `PageData<T>`。Controller 只依赖公开 Service，禁止直接依赖 Mapper 或 TxService。

## Service 与事务

公开 `*Service` 是其所属业务职责的入口，负责该职责内的查询、业务命令、权限补充校验、操作日志入口和业务组装。同一模块的多个公开 Service 必须保持边界明确，禁止出现职责重叠或为绕过内部边界而拆分的空壳 Service。

写操作委托给同目录、包级可见的 `*TxService`。TxService 使用类级 `@Transactional(rollbackFor = Exception.class)`，可以由同模块内不同职责的公开 Service 共享，Controller 和其他模块不能直接依赖它。事务内的存在性、唯一性和状态检查直接使用 Mapper，避免绕过事务边界调用 Service 缓存方法。

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

## 数据与实现约定

- 主键使用 `IdType.ASSIGN_ID`；可修改聚合使用 `@Version` 乐观锁。
- 业务单据继承 `BaseBillEntity`，统一使用 `org_id` 表达单据所属组织；申请人、经办人等非通用角色字段保留在具体聚合。
- 查询条件优先使用 `LambdaQueryWrapper` 和方法引用，禁止裸表名或字段名字符串。
- Stream 只用于链路简单、无副作用的集合筛选、映射和归约；不得在流内执行数据库查询、缓存访问、外部调用或修改流外可变状态。关联查询应先批量加载；树结构、多层分组和路径遍历应建立显式索引并使用清晰的普通循环。
- XML Mapper 的主表别名为 `a`，JOIN 表按出现顺序使用 `b`、`c`、`d`。
- 具有独立业务身份的主数据和业务单据使用 `number` 作为业务编码。
- 明细使用 `*Entry`，只保留 Entity、Mapper、Form 和 VO，通过 `parent_id` 关联主表；删除主单时先显式删除明细，不使用数据库级联删除。
- `*Util` 是不依赖 Spring 的纯静态工具类；`*Helper` 是依赖 Spring 注入或配置的组件。

## 外部 HTTP 调用

- 后端同步 HTTP 调用统一使用 `HttpClientHelper`；业务侧按需使用 `*Gateway` 封装第三方协议，Controller 禁止直接调用。
- 外部请求必须设置合理超时，默认不重试，且原则上不得放在数据库事务中。Cookie、令牌、密钥及敏感请求响应正文不得写入代码、日志或异常，不得将该能力暴露为接收用户任意 URL 的通用代理接口。

## 依赖治理

- Spring Boot Parent / BOM 已管理的依赖版本不得重复显式声明。
- Spring Boot 未管理、由项目主动选择的第三方依赖版本必须统一声明在 `<properties>` 中，并在 dependency 或插件配置中通过属性引用，禁止直接硬编码版本号；同一组件族共用同一版本属性。
- 只有存在明确兼容性、安全或功能原因时才允许覆盖 Spring Boot 管理的版本，覆盖必须显式且说明原因。Maven Plugin 或 annotation processor 需要显式版本时，优先复用已有版本来源。

## 缓存、文件和任务

- 业务正确性依赖的共享缓存和会话状态使用 Redis。进程内缓存只允许保存可丢弃、可重建且允许节点间短暂不一致的数据，并必须定义版本、短 TTL 或跨节点失效机制。
- 用户认证状态使用 Redis。
- 缓存监控只通过公开 `CacheService` 提供业务入口；Redis 原始访问属于模块内部技术能力，不形成第二个公开 Service。
- `FileStorageService` 抽象 Local、FTP、S3/MinIO 等存储实现；单实例生产可以使用具备持久化、备份和恢复能力的 Local，生产多实例部署必须使用所有实例可访问的共享对象存储。
- 附件上传大小、扩展名、MIME 和临时有效期由 `t_sys_attachment_config` 单例统一管理；业务资源注册只负责稳定资源身份、上传权限和对象级授权，禁止各单据复制全局文件限制。
- 数据库事务和外部文件存储不能原子提交，写操作必须定义补偿语义。
- 异步日志、线程池和 Quartz 执行必须显式传播并清理 Trace ID。
- Quartz 使用共享 PostgreSQL JobStore 和集群锁；应用级互斥不得使用 JVM 锁或本地 `Semaphore` 冒充分布式互斥。
- 应用实例位于 Nginx 等受信反向代理之后时，只能按部署配置解析转发头；不得无条件信任客户端提供的 IP 头。
- 多实例详细边界、当前差距和验收要求见[部署与多实例架构](./deployment.md)。

## 通用业务资源

附件、评论和审计等通用能力通过轻量 `BusinessResourceRegistry` 识别业务对象。业务模块以 Spring Bean 显式注册稳定资源类型和 `BusinessResourceAccessPolicy`；系统公共层只负责注册、重复校验和授权路由，不扫描 Entity、Controller 或数据表推断业务语义。

- 资源类型使用 `{domain}.{application}.{resource}` 稳定编码，不随 Java 包名或页面名称任意变化。
- 授权动作按读取、绑定、解除和删除分别表达，未知类型、资源不存在或策略异常时默认拒绝。
- 业务模块负责资源存在性、状态、数据范围和当前用户权限；公共层禁止直接查询可选业务模块的数据表。
- 该机制不是动态模型平台，不提供动态字段、动态表单、动态建表或配置化业务权限。

新增或显著扩展模块的实现流程见[模块开发指南](../development/module-development-guide.md)，编译、测试和风险匹配要求见[质量验证](../development/verification.md)。

## 自动化架构契约

后端 `mvn test` 通过 ArchUnit 对编译后的生产类强制执行以下 Java 架构边界，不建立历史违规基线，也不忽略真实依赖：

- `domain -> system -> infrastructure` 的顶层依赖方向，禁止遗留 `sm.framework` 包；
- Domain 对 Infrastructure 仅允许依赖显式开放的技术 Contract，当前为 `SmMapperConfig`；
- 任意不同顶级 Domain 之间只能依赖目标 Domain 的显式稳定 `contract` API，所有 Domain 一视同仁，禁止跨领域实现级耦合；
- Contract 只容纳稳定接口和 Command、Query、Reference、Result 等边界模型，禁止放入 Controller Form、VO、Entity、Mapper 或 TxService；
- DataScope 跨领域消费者只依赖 `sm.system.datascope` Contract，解析实现与角色配置仍属于系统管理领域；
- Controller 不得依赖 Mapper 或 TxService；
- Service 和 TxService 只能依赖所属模块的 Mapper；跨模块读取或写入必须调用能力提供方发布的 Contract 或职责明确的公开 Service；
- 公开 Service 不得声明事务，`@BizLog` 只允许标注公开 Service 的公开方法；
- TxService 位于模块 `service` 包、保持包级可见、声明类级
  `@Transactional(rollbackFor = Exception.class)`，且只允许同包公开 Service 调用；
- 领域 Controller、Service、TxService、Mapper、Converter、Entity、Form 和 VO 的包位置；
- Controller、模型和常量不得依赖 Mapper，Converter 不得依赖 Mapper、Service、缓存、安全上下文或外部资源；
- Entity、Form、VO 不得反向依赖 Controller、Service 或 Mapper；
- 公开 Service 的标准详情方法使用 `detail`，不得暴露通用 `getById` 入口。

这些规则集中在 `sm.architecture.ArchitectureContractTests`，随 Maven Surefire 自动进入本地测试和 CI quality gate。文件、配置、Flyway、文档、前端源码和生成文件等仓库级约束继续由专用测试或脚本承担。高风险公开入口使用运行时 `@AdministratorOnly`，由统一切面在进入目标方法前校验真实 `administrator` 身份；ArchUnit 只约束注解位置并禁止领域代码散落直接校验，切面的拒绝顺序和运行时语义由行为测试验证，不读取 Java 源码或用正则匹配方法体。

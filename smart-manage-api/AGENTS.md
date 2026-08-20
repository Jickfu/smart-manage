# 后端开发规则

本文件适用于 `smart-manage-api`。同时继承根目录 `AGENTS.md`。

## 分层与依赖

- 根包为 `sm`。
- `sm.framework` 只放第三方框架配置；`sm.system` 只放系统公共基础设施；业务代码按 `sm.domain.{领域}.{应用}.{模块}` 组织。
- 领域或应用公共能力放在对应 `common` 包，不额外拆分 Application、Domain、Infrastructure 层。
- Controller 禁止依赖 Mapper 或 `*TxService`。
- 每个模块只有一个公开 `*Service`；包级可见的 `*TxService` 只允许同模块 Service 委托调用。
- 不形成独立业务入口的技术协作者不得命名为 `*Service`；应按职责使用 `*Accessor`、`*Gateway` 等名称，并尽量保持包级可见。
- `sm.system.storage` 只能通过 `FileStorageConfigProvider` 获取配置，禁止依赖 `sm.domain.sys` 的实体或 Service。

## 接口与业务语义

- 所有接口返回 `Result<T>`；业务异常由 `GlobalExceptionHandler` 统一处理。
- `@SaIgnore` 表示公开接口，`@SaCheckPermission` 表示权限接口，其余接口由全局过滤器执行登录校验。
- Controller 中的 `@SaCheckPermission` 必须引用所属模块 `constant` 包内的 `*Permission` 常量，禁止直接书写权限码字符串；权限常量统一供接口鉴权和权限审计使用。
- 分页入参继承 `PageForm`，返回 `PageData<T>`。
- 公开业务方法禁止用 `return null` 表示业务失败；可空辅助方法必须通过命名和注释明确语义。
- JSON 反序列化、ID 转换和持久化结果禁止静默吞错。
- 标准单据接口使用 `listPage`、`detail`、`createNewData`、`save`、`submit` 和 `delete` 语义。
- `save` 只负责新增或暂存修改，不推进单据状态；`submit` 接收完整聚合，在同一事务内保存并推进状态，不要求先执行 `save`。

## 事务、日志与映射

- 公开 Service 禁止声明 `@Transactional`。
- 写操作委托给包级可见的 `*TxService`；该类使用类级 `@Transactional(rollbackFor = Exception.class)`。
- `@BizLog` 只标注在公开 Service 的业务命令方法；Controller 和 TxService 禁止标注，同一调用链只记录一次。
- 纯字段 `Entity → VO` 映射使用模块内 MapStruct `*Converter`。
- Converter 禁止依赖 Mapper、Service、缓存、安全上下文或外部资源。
- 查询、权限、状态规则、树结构和聚合转换保留在公开 Service，并使用 `assemble*` 命名。
- MyBatis 联表查询可以直接投影 VO。

## 数据与命名

- 主键使用 `IdType.ASSIGN_ID`；可修改聚合使用 `@Version` 乐观锁。
- 业务单据继承 `BaseBillEntity`，统一使用 `org_id` 表达单据所属组织；申请人、经办人等非通用角色字段保留在具体聚合。
- 查询条件优先使用 `LambdaQueryWrapper` 和方法引用，禁止裸表名或字段名字符串。
- Stream 只用于链路简单、无副作用的集合筛选、映射和归约；禁止在流内执行数据库查询、缓存访问、外部调用，或修改流外可变状态。
- 需要关联查询时先批量加载数据。树结构组装、多层分组关联、路径遍历和其他包含复杂中间状态的处理，应先建立显式索引，再使用具有清晰变量名的普通 `for` 循环；禁止使用嵌套 Stream 反复扫描集合或隐藏遍历复杂度。
- XML Mapper 的主表别名为 `a`，JOIN 表按出现顺序使用 `b`、`c`、`d`。
- 具有独立业务身份的主数据和业务单据使用 `number` 作为业务编码字段。
- 明细使用 `*Entry`，只保留 Entity、Mapper、Form 和 VO；使用 `parent_id` 关联主表。
- 删除主单时先显式删除明细，再删除主表，不使用数据库级联删除。
- `*Util` 是不依赖 Spring 的纯静态工具类；`*Helper` 是依赖 Spring 注入或配置的组件。

## 外部 HTTP 调用

- 后端同步 HTTP 调用统一使用 `HttpClientHelper`；业务侧按需使用 `*Gateway` 封装第三方协议，Controller 禁止直接调用。
- 外部请求必须设置合理超时，默认不重试，且原则上不得放在数据库事务中。
- Cookie、令牌、密钥及敏感请求响应正文禁止写入代码、日志或异常；不得将该能力暴露为接收用户任意 URL 的通用代理接口。

## 验证

- 修改后端代码至少执行 `mvn test`。
- 只有纯文档修改或确认不影响测试代码的简单改动才可以仅执行 `mvn compile`。
- 实体、Mapper、配置或迁移变更必须确认 MyBatis-Plus 相关代码正常编译。
- 安全、事务、状态流转、乐观锁、文件补偿和架构边界应补充风险驱动测试。

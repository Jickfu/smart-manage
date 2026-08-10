# 架构总览

## 项目定位

Smart Manage 是按领域、应用和单据组织的模块化单体。当前阶段重点是形成可验证、可维护的架构内核和工程质量，不以批量增加业务模块为目标。

系统仍处于架构搭建阶段，不承诺旧接口、旧数据结构或废弃实现的兼容性。新增设计应保持最小充分范围，不为理论复用建立没有真实使用方的抽象。

## 代码组织

```text
smart-manage/
├── db/migration/       # 数据库结构和必要初始化数据
├── smart-manage-api/   # Spring Boot 后端
├── smart-manage-web/   # React 前端
└── docs/               # 架构、开发、模块、提案和历史文档
```

后端业务按 `sm.domain.{领域}.{应用}.{单据}` 组织，前端业务按 `src/domain/{领域}/{应用}/{单据}` 组织。非系统内核业务集中在独立领域，系统内核禁止反向依赖可选业务领域。

## 已生效的架构结论

| 事项 | 当前结论 |
| --- | --- |
| 总体架构 | 保留领域模块化单体，不额外拆分 Application、Domain、Infrastructure 层 |
| 事务边界 | 公开 Service 负责查询和命令入口，包级 TxService 负责事务写入 |
| 数据库 | 根目录 Flyway 迁移是结构和必要初始化数据的唯一权威来源 |
| 并发控制 | 可修改聚合使用 `version` 和 MyBatis-Plus 乐观锁 |
| 接口基础设施 | 统一响应、全局异常、权限注解、操作日志和 Trace ID |
| 权限 | 功能权限使用权限码，高风险能力额外校验 `administrator`；数据权限按独立规范演进 |
| 对象映射 | 纯字段映射使用模块内 MapStruct Converter，业务组装留在 Service |
| 前端状态 | TanStack Query 管理服务端状态，Zustand 管理跨页面客户端状态，Form 管理编辑状态 |
| 页面架构 | 组件注册白名单、ERP 双页签和通用列表/编辑/自定义页面 |
| 部署架构 | 单实例可使用持久化 Local 存储；多实例共享 PostgreSQL、Redis、S3/FTP 文件存储和 Quartz JDBC 集群状态 |
| 纵向样板 | 采购申请验证标准主从单据、状态、乐观锁和页签生命周期 |

## 请求与页面主链路

后端请求主链路：

```text
CorsFilter（跨域处理）
→ SaServletFilter（登录校验和权限校验）
→ TraceIdInterceptor（建立并回传请求 Trace ID）
→ Controller
→ BizLogAspect 环绕公开 Service（业务入口和操作日志）
→ TxService（事务写入）
→ Mapper
```

前端页面遵循三层边界：

```text
页面壳层：布局、加载、错误、权限、按钮区
基础组件：字段、过滤器、表格、引用选择器
领域页面：状态流转、Mutation、明细聚合、业务命令
```

## 质量策略

测试采用风险驱动策略，优先保护架构边界、安全逻辑、状态流转、事务、并发、外部存储补偿和关键前端生命周期。不设置机械覆盖率目标，不为 Getter、Setter、普通 CRUD、简单 Converter 或框架生成代码增加低价值测试。

详细规则：

- [后端架构](./backend.md)
- [部署与多实例架构](./deployment.md)
- [前端架构](./frontend.md)
- [安全架构](./security.md)
- [质量验证](../development/verification.md)
- [日志数据生命周期](./log-lifecycle.md)
- [新增业务聚合检查清单](../development/business-aggregate-checklist.md)

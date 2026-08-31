# 用户管理

## 模块定位

用户模块维护系统账号、基础资料、启停状态、任职、角色关系以及认证和授权所需的用户侧业务规则。它属于 `sm.domain.sys` 系统管理业务领域，不属于 `sm.system` 系统内核；其他顶级 Domain 不得直接依赖用户 Service、Mapper、Entity、缓存或数据表。

## 公开职责

- `UserService` 负责后台用户管理、任职组装和启停命令。
- `UserProfileService` 负责当前用户资料。
- 认证、凭据和授权使用各自职责明确的公开 Service，不通过用户管理 Service 暴露。
- 跨顶级 Domain 的业务引用只依赖 `sm.domain.sys.base.user.contract`。

## 跨领域用户引用

用户领域发布 `UserReferenceReader` 和 `UserReference` 作为最小只读 Contract。`UserReference` 只包含 `id`、`number`、`name` 和 `enabled`，不暴露用户名、联系方式、密码状态、任职、角色、权限或用户 Entity。

- `require` 要求用户存在，但允许返回已禁用用户，用于仍然有效的历史引用读取。
- `requireEnabled` 要求用户存在且启用，用于新增或修改单个业务引用。
- `findByIds` 批量返回仍然存在的用户，允许缺失，用于查询组装；结果按去重后的输入顺序排列。
- `requireEnabledByIds` 使用一次批量查询校验全部用户，任何用户不存在或禁用都整体失败。
- 空批量输入返回空结果；非空输入中的空用户 ID 属于参数错误。

通用的“可作为新业务引用”只表示用户存在且 `enabled=true`。任职、组织、角色、权限、管理员身份或调用者数据范围不隐式进入该 Contract，应由拥有相应语义的领域能力或具体业务用例校验。

业务实体默认只持久化用户 ID，查询时通过 Contract 组装当前引用信息。需要保留发生时姓名等历史快照时，由具体业务聚合显式建模；用户 Contract 不承诺历史快照，也不替代业务领域对用户删除后展示语义的设计。

## 数据边界

用户联系方式遵守[数据脱敏架构](../../../architecture/data-masking.md)，密码、验证码、令牌和其他凭据不得进入普通查询或跨领域 Contract。用户禁用、删除和凭据重置后的会话终止与授权缓存刷新遵守[安全架构](../../../architecture/security.md)。

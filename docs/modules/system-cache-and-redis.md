# 应用缓存与 Redis 管理

## 模块边界

- “应用缓存”只管理服务端明确登记的 JetCache 缓存，支持逐个清理和全部清理；
- “Redis 管理”提供当前 Redis DB 的实时状态、游标扫描、受限 Value 预览和 Key 删除；
- 不提供 `KEYS *`、`FLUSHDB`、Value 修改或历史指标持久化；
- LOCAL 缓存的统计和清理只作用于当前应用节点，当前项目仍按单节点部署声明。

## 安全边界

- Redis 管理的全部接口同时要求权限码和 `administrator` 身份；
- 应用缓存清理要求权限码和 `administrator` 身份；
- Sa-Token、验证码、密码修改票据等安全 Key 只允许查看元数据，不返回 Value；
- Value 只按 Redis 原始类型只读预览，不反序列化为 Java 对象；二进制内容使用 Base64；
- 单次 Value 最多读取 100 项或 64 KiB，单次最多删除 100 个 Key；
- 操作日志不得记录 Redis Value 或被删除 Key 的完整请求内容。

## 权限

| 能力 | 权限码 |
| --- | --- |
| 应用缓存查询 | `sys:monitor:cache:listPage` |
| 应用缓存清理 | `sys:monitor:cache:clear` |
| 全部应用缓存清理 | `sys:monitor:cache:clearAll` |
| Redis 查询 | `sys:monitor:redis:listPage` |
| Redis Value 预览 | `sys:monitor:redis:value` |
| Redis Key 删除 | `sys:monitor:redis:delete` |

## 图表边界

页面只展示 Redis 当前实时快照。Apache ECharts 作为系统公共图表基础设施按需加载，当前不声明历史趋势、采样周期或指标保留能力。

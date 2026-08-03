# 缓存监控

## 模块边界

- “缓存状态”只读展示 Redis 实时状态和 JetCache 当前统计周期的命中、未命中、失败、QPS 与平均耗时；
- “缓存管理”按统一列表展示当前节点本地缓存条目和当前 Redis DB 的 Key，不向使用者暴露两个独立操作入口；
- 缓存管理使用标准分页、关键字搜索、存储位置与应用缓存筛选，支持受限 Value 预览、单项删除和批量删除；
- “清空全部应用缓存”只清理服务端明确登记的 JetCache 缓存，不等同于 `FLUSHDB`；
- 不提供 `KEYS *`、`FLUSHDB`、Value 修改或历史指标持久化；
- LOCAL 缓存的统计和清理只作用于当前应用节点，当前项目仍按单节点部署声明。

## 安全边界

- 缓存条目查询、Value 预览、删除和应用缓存清理同时要求权限码和 `administrator` 身份；
- Sa-Token、验证码、密码修改票据等安全 Key 只允许查看元数据，不返回 Value；
- Redis Value 只按原始类型只读预览，不反序列化为 Java 对象；二进制内容使用 Base64；
- 文件存储配置等敏感本地缓存只展示元数据，不返回 Value；
- 单次 Value 最多读取 100 项或 64 KiB，单次最多删除 100 个 Key；
- 操作日志不得记录 Redis Value 或被删除 Key 的完整请求内容。

## 权限

| 能力 | 权限码 |
| --- | --- |
| 缓存状态与条目查询 | `sys:monitor:cache:listPage` |
| 应用缓存清理 | `sys:monitor:cache:clear` |
| 全部应用缓存清理 | `sys:monitor:cache:clearAll` |
| 缓存 Value 预览 | `sys:monitor:cache:value` |
| 缓存条目删除 | `sys:monitor:cache:delete` |
| Redis 查询 | `sys:monitor:redis:listPage` |
| Redis Value 预览 | `sys:monitor:redis:value` |
| Redis Key 删除 | `sys:monitor:redis:delete` |

## 图表边界

页面只展示 Redis 与 JetCache 当前实时快照。Apache ECharts 作为系统公共图表基础设施按需加载，当前不持久化历史趋势；JetCache 统计会随应用重启或其定时统计周期重置。

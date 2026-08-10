# 慢 SQL 监控

## 定位

慢 SQL 监控复用主数据源的 Druid `StatFilter`，按应用实例查看当前 JVM 启动以来的 SQL 聚合统计。该能力用于临时定位性能问题，不是逐次 SQL 审计、历史监控库或数据库控制台。

统计只存在于目标实例内存，实例重启或管理员清空后不可恢复。首版不持久化、不关联 Trace ID、不跨实例聚合，也不开放 Druid StatViewServlet。

## 接口和权限

- 快照：`GET /sys/monitor/slow-sql/snapshot?instanceId=...`；
- 调整阈值：`POST /sys/monitor/slow-sql/threshold`；
- 清空统计：`POST /sys/monitor/slow-sql/clear`；
- 权限分别为 `sys:monitor:slow-sql:access`、`sys:monitor:slow-sql:config` 和 `sys:monitor:slow-sql:clear`；
- 三项能力均由公开 Service 复核当前账号为 `administrator`；
- 浏览器只提交实例 ID，目标地址由在线实例注册表解析，目标节点再次执行鉴权。

阈值调整和清空只作用于指定实例。阈值合法范围为 100～60000 毫秒，并通过操作日志审计；SQL 正文和统计结果不进入操作日志。

## 数据与交互边界

页面展示 Druid 合并后的参数化 SQL、执行次数、成功次数、错误次数、总耗时、平均耗时、最大耗时、最大并发、事务内次数、更新行数、读取行数和最近执行时间。仅展示最大耗时达到当前阈值的记录。

页面默认手动刷新，支持实例、SQL 关键字和排序筛选。页面必须明确提示数据属于所选实例的内存聚合统计，阈值变化不追溯重算逐次慢 SQL；清空操作必须二次确认。

生产环境启用 `StatFilter` 和 SQL 合并，关闭 Druid 慢 SQL 日志输出与 StatViewServlet，避免日志重复及 SQL 参数泄露。

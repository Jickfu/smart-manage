# 运行监控

## 定位

运行监控是 Smart Manage 开箱即用的内建监控体系。Actuator 提供健康状态，Micrometer/JDK 管理接口提供应用指标，OSHI 提供跨平台主机指标；Redis 保存在线注册和当前快照，PostgreSQL 保存主机、实例目录及固定结构历史。它不要求 Prometheus、Grafana 或 exporter，也不试图成为通用时序数据库。

领域模型明确区分 `Host` 与 `Application Instance`，关系为一对多。`instanceId` 在集群内唯一；`hostId` 表示运行主机身份，不表示硬件资产序列号。同一 OS Host 上运行的多个实例必须解析出相同 `hostId`。

## 访问边界

- 实例列表与快照读取都必须经过后端功能授权；
- 响应必须携带 `instanceId` 和采样时间；
- 浏览器只能提交实例 ID；内部地址由服务端注册表解析，禁止接收浏览器提供的目标 URL；
- 目标节点必须使用共享登录态重新执行权限校验。

## 数据边界

允许返回 CPU、内存、磁盘、线程统计、GC、连接池摘要、JVM/OS 版本以及健康组件名称和状态。

禁止在普通运行快照中返回 JVM 启动参数、Java 安装路径、环境变量、系统属性全集、数据库连接地址、健康详情、完整线程栈或其他可能包含凭据和部署拓扑的信息。完整线程栈只能通过独立的管理员线程诊断能力获取。

后台默认每 10 秒采样并将当前快照以 TTL 写入 Redis，每分钟将固定结构历史 UPSERT 到 PostgreSQL。前端刷新频率只影响展示，不承担采样职责。历史默认保留 7 天并定时清理，查询范围必须受限并按范围聚合。

每个实例只由本机唯一采样器推进 Host 和 Instance 快照；Host 观测按 `{hostId}:{instanceId}` 写入独立 TTL source key，Instance key 只保存 JVM/应用 Snapshot。两类采集独立降级，单个 OSHI/JVM collector 失败不阻断其他指标。历史通过 `(host_id, sample_bucket)` 与 `(instance_id, sample_bucket)` 唯一约束收敛多实例并发。

重要 Collector 明确携带可用性：内存、线程、连接池、健康、IO 和整体文件系统采集失败时为 `UNAVAILABLE`，对应数值为未知而非零。主机静态元数据采集失败仍保留 `hostId` 与采样时间，应用快照也始终保留 `instanceId`、`hostId` 与采样时间。历史未知值写为 SQL `NULL`，前端实时区显示“-”，趋势图保留断点。

本地 Snapshot Store 按 `smart-manage.domain.sys.monitor.sampling.snapshot-ttl-seconds` 检查采样时间，语义与 Redis TTL 一致；过期快照不会继续用于告警或被历史任务重复写入。

Host 规则和 Host Current Telemetry 不读取本地 Snapshot Store，而共同读取 `sm:monitor:snapshot:host-source:{hostId}:{instanceId}` 候选观测。系统按 metric 过滤 Collector 未知和过期 source，再选择 `sampleTime` 最新的有效值；不取最大值或平均值。读取时校验 source key 对应的 `hostId / instanceId`、值内身份和统一 TTL，只有所有新鲜 source 对该 metric 均不可用时才按未知处理。Instance 规则仍读取当前 JVM 的本地新鲜 Instance Snapshot。Host 历史保持各 JVM 本地采样写入 PostgreSQL，并由 `(host_id, sample_bucket)` UPSERT 保留该分钟最新采样。

Host Catalog 是历史目录；Current Topology 只展示仍有 `ACTIVE` 实例的 Host。实例选择来自持久化目录，明确区分在线、离线和已退役；正常在线状态由实例选择器和拓扑表表达，不额外占用整行提示，离线或退役状态则明确提示实时遥测不可用但历史趋势仍可查询。历史聚合定义见[历史趋势口径](#历史趋势口径)。历史图图例固定在绘图区上方，不能覆盖趋势数据。

当关联实例全部离线时，只能表达“主机遥测不可用”，不能断言物理主机宕机。整个 Smart Manage 集群全部不可达时无法自我告警，该场景属于外部 HTTP 可用性监控职责。

开发环境未提供 `SMART_MANAGE_INSTANCE_ID` 时，实例 ID 默认为 `instance1`。增加实例时必须显式配置唯一且连续易读的 ID，例如 `instance2`、`instance3`；系统不自动分配编号，避免并发启动时产生重复身份。生产环境必须显式配置 `SMART_MANAGE_INSTANCE_ID`。

## 历史趋势口径

历史趋势与完整数据表共用当前对象、查询范围的一份结果；展示规则遵守[图表完整数值访问](../../design/charts.md#完整数值访问)。表格原值是历史查询聚合结果，不是所有实时采样明细；完整时间保留返回时区，时间表示聚合桶起点。

| 查询范围 | 时间桶 |
| --- | --- |
| 1 小时 | 1 分钟 |
| 6 小时 | 5 分钟 |
| 24 小时 | 10 分钟 |
| 7 天 | 30 分钟 |

查询从当前时间向前截取范围，再按固定时间桶聚合；首尾桶可能不完整。接口不补齐完全没有记录的时间桶。前端绘图按当前粒度补齐中间缺桶并用空值断线，避免跨停采时段连线；完整数据表仍展示接口返回的统计点，不增加虚构记录。

| 指标 | 桶内口径 | 返回单位 |
| --- | --- | --- |
| 主机／进程 CPU、内存、Heap、连接池利用率 | 有效历史值的算术平均；内存、Heap、连接池先逐条计算比例再平均 | 比例，1 表示 100% |
| 最高文件系统使用率 | 最大值；挂载点对应最高使用率，同值取较新记录 | 比例 |
| 磁盘及网络 IO | 速率的算术平均，不是累计流量 | B/s |
| HTTP 请求及 5xx | 速率的算术平均，不是请求总数 | req/s |
| HTTP P95／P99 | 历史记录中对应百分位的最大值，不是整个查询桶或查询范围重新计算的百分位 | ms |
| 线程、阻塞线程及连接池等待 | 数量的算术平均，允许小数 | 个 |

聚合忽略 NULL；桶内该指标全部未知时返回 NULL，不能当作零参与平均。分母为零的比例为未知。图表将比例乘以 100 显示百分比，表格保留接口原值和单位，不截断精度；图例显隐不改变表格范围。

当前真实数据库测试 `MonitorPersistencePostgresTests.historyQueryUsesNumericRatiosMatchingWorstMountAndWorstMinuteLatency` 覆盖比例计算、最高使用率与挂载点对应、P95/P99 最大值及四种查询范围；前端 `trendDataAccess.test.tsx` 验证共享数据、原值与单位、零值和缺失值，以及刷新失败和权限拒绝行为。

## 数据源监控

项目当前只有一个主要 Druid 数据源，连接池摘要合并在运行监控中。生产关闭 Druid StatViewServlet，不保留独立“数据源监控”菜单，也不代理 Druid Web 控制台。

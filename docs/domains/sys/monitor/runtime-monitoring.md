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

每个实例只由本机唯一采样器推进 Host 和 Instance 快照；Host Redis key 只保存 Host Snapshot，Instance key 只保存 JVM/应用 Snapshot。两类采集独立降级，单个 OSHI/JVM collector 失败不阻断其他指标。历史通过 `(host_id, sample_bucket)` 与 `(instance_id, sample_bucket)` 唯一约束收敛多实例并发。

Host Catalog 是历史目录；Current Topology 只展示仍有 `ACTIVE` 实例的 Host。实例选择来自持久化目录，明确区分在线、离线和已退役；离线或退役实例的实时遥测显示不可用，但历史趋势仍可查询。较长范围的历史 P95/P99 取查询桶内最差一分钟值。

当关联实例全部离线时，只能表达“主机遥测不可用”，不能断言物理主机宕机。整个 Smart Manage 集群全部不可达时无法自我告警，该场景属于外部 HTTP 可用性监控职责。

开发环境未提供 `SMART_MANAGE_INSTANCE_ID` 时，实例 ID 默认为 `instance1`。增加实例时必须显式配置唯一且连续易读的 ID，例如 `instance2`、`instance3`；系统不自动分配编号，避免并发启动时产生重复身份。生产环境必须显式配置 `SMART_MANAGE_INSTANCE_ID`。

## 数据源监控

项目当前只有一个主要 Druid 数据源，连接池摘要合并在运行监控中。生产关闭 Druid StatViewServlet，不保留独立“数据源监控”菜单，也不代理 Druid Web 控制台。

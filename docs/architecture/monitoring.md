# 内建监控架构

## 定位

Smart Manage 内建监控是运维中心的固定业务能力，使用 OSHI、JDK Management、Micrometer 和 Actuator，不依赖 Prometheus、Grafana 或通用时序数据库。Redis 是在线注册和当前快照的权威来源；PostgreSQL 保存目录、历史、规则、事件和邮件通知发件箱。

## 身份与生命周期

- `Host` 与 `Application Instance` 是 1:N，`hostId` 是部署主机稳定身份，`instanceId` 在集群内唯一。
- 每个 JVM 生成不持久化的 `registrationToken`。Redis Lua 脚本原子保证“无占用则注册、同 token 则心跳、异 token 且 TTL 有效则启动失败”。
- 持久化实例生命周期为 `ACTIVE / RETIRED`。管理员可退役离线实例；同 ID 进程再次有效心跳时恢复 `ACTIVE`。
- Redis 心跳默认 10 秒，普通心跳不访问 PostgreSQL；目录仅在启动和低频刷新时 UPSERT，生命周期重新激活只在启动注册时执行。
- Host Catalog 保存历史出现过的主机；Current Topology 只包含至少一个 `ACTIVE` 实例的主机。退役实例仍保留在 Instance Catalog，供历史追溯使用。

## 唯一采样链

```text
OshiHostMetricsProvider + ApplicationMetricsProvider
                         ↓
              MonitorSnapshotSampler
                         ↓
        Independent HostSnapshot / InstanceSnapshot
             ↓             ↓
      Redis current       History / Alert
```

`MonitorSnapshotSampler` 是唯一调用指标 Provider 的组件。CPU ticks、IO 计数器和 HTTP 计数器差值只在该链路推进。历史、告警和 Controller 只读取已发布快照，不触发 OSHI 或 JVM 重新采集。

Host Snapshot 只包含 OS、Host CPU/内存/交换区、文件系统、磁盘/网络 IO 和主机 uptime，写入 `sm:monitor:snapshot:host:{hostId}`。Instance Snapshot 只包含 JVM、进程 CPU、堆/非堆、线程、GC、HTTP、连接池和健康状态，写入 `sm:monitor:snapshot:instance:{instanceId}`。两类快照独立推进，一个 Provider 失败不能阻断另一类快照。所有 API 时间使用 ISO-8601。

Host 作用域告警统一读取 `sm:monitor:snapshot:host:{hostId}` 作为同一 Host 的权威当前观测，采用最后写入获胜，并校验快照内 `hostId`、`sampleTime` 与统一 TTL；键值身份不一致、过期、缺失或损坏均视为指标未知。Instance 作用域告警继续读取当前 JVM 的本地新鲜 Instance Snapshot。Host 历史仍由各 JVM 持久化本地 Host Snapshot，通过一分钟唯一键 UPSERT 和采样时间比较保留最新样本；当前告警观测与历史写入职责不得混用。

CPU、内存、文件系统、磁盘、网络、JVM、线程、GC、HTTP、连接池和健康组件按 collector 隔离。局部失败只令对应字段未知或集合缺失；同一 collector 的重复异常日志按时间限频，并携带 collector 与 Host/Instance 身份。

HTTP P95/P99 来自无业务标签的实例级聚合 Timer，不对各 URI/status Timer 的分位数取最大值。

## 历史与文件系统

历史按一分钟固定结构 UPSERT，查询限定 `1h / 6h / 24h / 7d` 并返回已计算的 ratio/速率/毫秒强类型字段。ratio 在 PostgreSQL 中显式转为 numeric。主机最高文件系统使用率与挂载点取自同一条样本，并排除 overlay/tmpfs/squashfs 等虚拟挂载。较长查询桶中的 P95/P99 表示“桶内最差一分钟的 P95/P99”，不是整个区间的真实分位数。

## 告警状态与并发

- `PENDING` 只在持续满足 trigger condition 时累计 duration；触发条件消失则 `CLOSED(PENDING_CLEARED)`。
- `FIRING` 才使用 recovery threshold/hysteresis；高值规则在 `value <= recoveryThreshold` 时 `RECOVERED`。
- `RECOVERED` 表示已触发故障后的业务恢复；`CLOSED` 表示因条件消失、规则停用或实例退役结束，不发恢复通知。
- `INSTANCE_OFFLINE` 只评估 `ACTIVE` 实例。退役关闭该实例全部活动事件，规则停用关闭其全部活动事件。
- 活动事件条件唯一索引、事务行锁和通知唯一键共同收敛多实例并发。告警通知仅通过邮件发件箱。

预定义规则通过 `valueKind / displayUnit / min / max / recommendedThreshold` 定义值语义，服务端按 rule code 元数据校验；BOOLEAN 规则固定触发 1、恢复 0。

## 可用性边界

Redis 是 Smart Manage 基础设施：启动时无法注册必须 fail-fast。运行中 Redis 断连时进程保留，健康状态转 DOWN、心跳持续重试；只要 PostgreSQL 与 SMTP 可用，`REDIS_HEALTH_DOWN` 仍可通过持久化事件和邮件任务通知。

PostgreSQL 是规则、事件、通知、用户收件人、SMTP 账号和邮件任务的共同权威来源。数据库完全不可用时，不可能可靠读取规则或创建邮件任务，因此系统不提供 `DB_HEALTH_DOWN` 自依赖邮件规则，也不缓存解密 SMTP 凭据另建第二套告警系统。数据库宕机必须由 PostgreSQL 自身、云数据库监控或外部可用性监控承担；数据库恢复后，正常采样、历史和告警链路随连接池恢复继续工作。

采集值严格区分 `UNKNOWN` 与真实的零值。重要 Collector 携带可用性标记，整体文件系统采集也有独立可用性；不可用字段以 `null` 持久化和传输，不得使用 primitive 默认值参与告警或图表。健康 Collector 不可用表示未知，不等同于健康组件 `DOWN`。

JVM 本地最新快照与 Redis 当前快照使用同一 `snapshot-ttl-seconds` 新鲜度边界。超过 TTL 的本地快照不再参与当前遥测、告警评估或历史持久化。指标未知时，`FIRING` 保持原状且不恢复、不重复通知；`PENDING` 关闭为 `METRIC_UNAVAILABLE`，打断连续异常计时，恢复采样后从新的周期重新累计。

规则停用和实例退役在事务层与告警评估锁定同一规则或实例目录行，提交时重新验证启用状态与实例生命周期。非恢复性关闭会将未发送的 FIRING/REPEAT 发件箱标记为 `SKIPPED`，投递器发送前还会再次核对事件状态；已成功创建到邮件系统的任务属于既有 best-effort 边界。

当一台 Host 的全部实例离线时只能表达“遥测不可用”，不能断言物理主机宕机。整个 Smart Manage 集群不可达时无法自我告警，必须使用外部 HTTP/进程可用性监控。

离线或已退役实例没有当前快照时返回明确的 `UNAVAILABLE` 遥测状态，不作为页面级错误；目录、历史趋势和既有告警仍可查询。

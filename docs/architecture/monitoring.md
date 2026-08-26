# 内建监控架构

## 定位

Smart Manage 内建监控是运维中心的固定业务能力，使用 OSHI、JDK Management、Micrometer 和 Actuator，不依赖 Prometheus、Grafana 或通用时序数据库。Redis 是在线注册和当前快照的权威来源；PostgreSQL 保存目录、历史、规则、事件和邮件通知发件箱。

## 身份与生命周期

- `Host` 与 `Application Instance` 是 1:N，`hostId` 是部署主机稳定身份，`instanceId` 在集群内唯一。
- 每个 JVM 生成不持久化的 `registrationToken`。Redis Lua 脚本原子保证“无占用则注册、同 token 则心跳、异 token 且 TTL 有效则启动失败”。
- 持久化实例生命周期为 `ACTIVE / RETIRED`。管理员可退役离线实例；同 ID 进程再次有效心跳时恢复 `ACTIVE`。
- Redis 心跳默认 10 秒；目录仅在启动、元数据/生命周期变化和低频刷新时 UPSERT。

## 唯一采样链

```text
OshiHostMetricsProvider + ApplicationMetricsProvider
                         ↓
              MonitorSnapshotSampler
                         ↓
        Atomic HostSnapshot + InstanceSnapshot
             ↓             ↓
      Redis current       History / Alert
```

`MonitorSnapshotSampler` 是唯一调用指标 Provider 的组件。CPU ticks、IO 计数器和 HTTP 计数器差值只在该链路推进。历史、告警和 Controller 只读取已发布快照，不触发 OSHI 或 JVM 重新采集。

Host Snapshot 只包含 OS、Host CPU/内存/交换区、文件系统、磁盘/网络 IO 和主机 uptime，写入 `sm:monitor:snapshot:host:{hostId}`。Instance Snapshot 只包含 JVM、进程 CPU、堆/非堆、线程、GC、HTTP、连接池和健康状态，写入 `sm:monitor:snapshot:instance:{instanceId}`。所有 API 时间使用 ISO-8601。

HTTP P95/P99 来自无业务标签的实例级聚合 Timer，不对各 URI/status Timer 的分位数取最大值。

## 历史与文件系统

历史按一分钟固定结构 UPSERT，查询限定 `1h / 6h / 24h / 7d` 并返回已计算的 ratio/速率/毫秒强类型字段。主机历史保存最高文件系统使用率及挂载点，并排除 overlay/tmpfs/squashfs 等容易重复统计的虚拟挂载。

## 告警状态与并发

- `PENDING` 只在持续满足 trigger condition 时累计 duration；触发条件消失则 `CLOSED(PENDING_CLEARED)`。
- `FIRING` 才使用 recovery threshold/hysteresis；高值规则在 `value <= recoveryThreshold` 时 `RECOVERED`。
- `RECOVERED` 表示已触发故障后的业务恢复；`CLOSED` 表示因条件消失、规则停用或实例退役结束，不发恢复通知。
- `INSTANCE_OFFLINE` 只评估 `ACTIVE` 实例。退役关闭活动离线事件，规则停用关闭其全部活动事件。
- 活动事件条件唯一索引、事务行锁和通知唯一键共同收敛多实例并发。告警通知仅通过邮件发件箱。

预定义规则通过 `valueKind / displayUnit / min / max / recommendedThreshold` 定义值语义，服务端按 rule code 元数据校验；BOOLEAN 规则固定触发 1、恢复 0。

## 可用性边界

Redis 是 Smart Manage 基础设施：启动时无法注册必须 fail-fast。运行中 Redis 断连时进程保留，健康状态转 DOWN、依赖请求失败、心跳持续重试，本机已采样数据仍可用于 PostgreSQL 历史和邮件告警。

当一台 Host 的全部实例离线时只能表达“遥测不可用”，不能断言物理主机宕机。整个 Smart Manage 集群不可达时无法自我告警，必须使用外部 HTTP/进程可用性监控。

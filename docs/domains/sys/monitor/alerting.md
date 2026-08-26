# 监控告警

## 定位与边界

监控告警基于内建监控快照评估预定义规则类型，不提供 PromQL、表达式语言或通用规则 DSL。告警事件独立持久化；通知第一版仅支持邮件，不建设站内消息、短信、Webhook 或第三方 IM 渠道。

规则支持启用状态、严重程度、触发阈值、持续时间、恢复阈值、重复通知间隔、邮件开关和接收用户。接收人引用系统用户，保存只提交用户 ID；启用邮件时，接收用户必须启用并已配置邮箱。通知渠道固定为邮件。

## 状态与并发

`PENDING` 只在持续满足触发条件时累计；触发条件消失则 `CLOSED(PENDING_CLEARED)`。`FIRING` 才使用恢复阈值，高值规则在当前值小于等于恢复阈值时 `RECOVERED`。规则停用和实例退役使用 `CLOSED`，不伪装成故障恢复；实例退役会关闭该实例全部 PENDING/FIRING 事件。

指标 Collector 不可用时不会把默认零值送入状态机：`FIRING` 保持原状态且不发送恢复或重复邮件，`PENDING` 关闭为 `CLOSED(METRIC_UNAVAILABLE)`，从而打断“连续满足阈值”的计时。健康采集未知不等同于组件 DOWN，实例整体离线仍只由独立的 `INSTANCE_OFFLINE` 规则判断。

活动事件通过 `(rule_id, scope_type, scope_id)` 条件唯一索引保证同一对象同一规则只有一个 PENDING/FIRING 事件，状态评估在事务内锁定活动事件。多实例同时评估时由数据库唯一约束和行锁收敛，不使用 JVM 锁、分布式选主或 Redis 锁。

Host 规则不使用各 JVM 的本地 Host Snapshot，而按 `hostId` 读取共享 Redis canonical Snapshot，并校验快照身份、采样时间和 TTL。同一 Host 上的多个 JVM 因而基于同一观测值参与数据库并发收敛；canonical Snapshot 不可用时进入既有指标未知语义。Instance 规则仍使用当前 JVM 的本地新鲜 Instance Snapshot。

状态事务在创建新事件前以共享锁重新验证规则仍启用；INSTANCE 作用域同时验证实例仍为 ACTIVE。规则停用与实例退役的写锁因此能和已经读取旧状态的 Evaluator 收敛，最终不会留下新的活动事件。

## 邮件可靠性

告警状态事务只写 `t_sys_monitor_alert_notification` 发件箱。后台投递器使用 `FOR UPDATE SKIP LOCKED` 语义原子领取，事务提交后通过消息应用的 `EmailNotificationSender` 创建 `monitor.alert` 邮件任务。通知 ID 是稳定幂等键；失败会受限重试，卡死领取会回收。SMTP 发送、重试和投递记录继续由既有邮件任务基础设施负责。

未配置邮件接收人、默认发信账号不可用或收件用户配置失效时，不回滚告警状态；错误保留在通知发件箱中供排查。

规则停用、实例退役或 PENDING 非恢复性关闭后，尚未投递的 FIRING/REPEAT 通知会被标记为 `SKIPPED`；投递器领取后、调用邮件能力前再次检查 Incident 状态，避免关闭后的故障邮件继续入队。已经成功创建的 EmailTask 不在监控事务内撤回。

投递前的事件状态复核与实际创建 EmailTask 之间仍存在极小的非原子窗口，这是 PostgreSQL 监控事务与邮件应用之间的既有 best-effort 边界；本阶段不引入跨模块分布式事务或补偿撤回协议。

告警摘要、事件和邮件使用同一个指标展示格式：ratio 展示百分比，count/rate/duration 携带单位，boolean 展示“异常/正常”。数据库仍保存原始标准值。

## 可观测性物理边界

应用实例离线通过 Redis 心跳 TTL 判断。Host 遥测与 JVM 同生命周期：关联实例全部离线时只能判断“主机遥测不可用”，不能断言物理服务器宕机。整个 Smart Manage 集群全部不可达时无法自我告警，必须由外部 HTTP 可用性监控承担。

PostgreSQL 同时承载告警规则、事件、通知、收件人、SMTP 账号和邮件任务。数据库完全不可用时无法可靠兑现数据库宕机邮件，因此系统不提供自依赖的 `DB_HEALTH_DOWN` 规则；该故障由数据库平台或外部监控承担。Redis 不可用但 PostgreSQL 与 SMTP 可用时，Redis 健康告警仍走正常持久化邮件链路。

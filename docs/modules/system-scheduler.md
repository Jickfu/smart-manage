# 任务调度

## 模块边界

任务调度是“系统服务”云下的独立应用，不属于系统监控。它负责管理可执行的定时任务定义和不可变的执行实例记录：

- 定时任务：编码、名称、分组、Spring Job 实现、Cron、参数、启停状态。
- 执行实例：任务每次实际执行产生的开始时间、结束时间、结果、错误和 Trace ID。

后端领域位于 `sm.domain.sys.scheduler`，前端领域位于 `src/domain/sys/scheduler`。接口、权限和页面组件键统一使用 `sys/scheduler`，不提供已废弃的 `sys/monitor/job` 兼容入口。

## 聚合与状态

定时任务是可变业务聚合，使用 `version` 乐观锁：

- 新建任务固定进入 `PAUSED`，保存接口不能改变状态。
- `ENABLED` 只能通过列表批量暂停命令转换为 `PAUSED`。
- `PAUSED` 只能通过列表批量恢复命令转换为 `ENABLED`。
- 保存、暂停、恢复和删除必须携带当前版本号。
- 系统内置任务不可修改执行类及分组，也不可删除。
- 删除任务定义时保留执行实例，确保历史可审计。

执行实例状态为 `RUNNING`、`SUCCESS`、`FAILED`、`SKIPPED`，由 Quartz 全局监听器写入，业务接口只读。`SKIPPED` 表示共享资源互斥键已被其他任务占用，本次触发未执行业务代码。

不同任务可配置相同的 `mutexKey` 来声明对同一共享资源的互斥访问。互斥键为空时不限制并发；互斥范围不是任务类或 Quartz JobKey，因此同一执行类的不同参数任务仍可独立调度。当前互斥实现遵循本模块的单节点部署边界，多节点部署前必须替换为分布式锁。

## 安全边界

- 所有写命令同时执行 Controller 权限校验和公开 Service 管理员校验。
- 任务执行类必须是 Spring 容器中注册的 `org.quartz.Job` Bean，禁止按请求类名动态加载任意类。
- 任务参数只接受 JSON 对象。
- Cron 表达式在写入数据库前校验。

## Quartz 一致性

当前部署边界是单节点 Quartz JDBC JobStore。数据库中的任务定义是权威来源：

1. 数据库事务提交任务定义。
2. 提交后同步对应 Quartz Job 和 Trigger。
3. Quartz 临时故障时保留数据库期望状态，由“重新同步”命令恢复。
4. 全量同步会重建期望任务，并清理不存在或 Quartz Key 已与数据库不一致的受管任务。

任务改名时先建立新调度项，再移除旧调度项，降低调度空窗；异常残留由全量同步清理。

## 页面

- `sys/scheduler/job`：定时任务列表。
- `sys/scheduler/job/edit`：定时任务新增和编辑。
- `sys/scheduler/execution`：执行实例列表。
- `sys/scheduler/execution/detail`：执行实例只读详情。

任务状态统计和近七日执行趋势展示在任务调度应用固有的“应用首页”页签，不占用定时任务列表空间，也不为统计另增菜单。首页查询接口为 `GET /sys/scheduler/home/summary`。新增任务通过引用选择器选择带 `SchedulerJobDefinition` 元数据的执行类，选择后可自动带出用途说明和 JSON 参数模板；Cron 支持常用频率和未来五次服务端时间预览。

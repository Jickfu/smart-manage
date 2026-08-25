-- Smart Manage 内建监控：运行拓扑、固定结构历史与轻量告警。

CREATE TABLE t_sys_monitor_host (
    id bigint PRIMARY KEY,
    host_id varchar(100) NOT NULL,
    host_name varchar(200) NOT NULL,
    os_name varchar(100),
    os_version varchar(200),
    arch varchar(50),
    first_seen_time timestamptz NOT NULL,
    last_seen_time timestamptz NOT NULL
);
CREATE UNIQUE INDEX uk_sys_monitor_host_host_id ON t_sys_monitor_host(host_id);
COMMENT ON TABLE t_sys_monitor_host IS '监控主机目录';
COMMENT ON COLUMN t_sys_monitor_host.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_host.host_id IS '运行主机稳定标识';
COMMENT ON COLUMN t_sys_monitor_host.host_name IS '主机名称';
COMMENT ON COLUMN t_sys_monitor_host.os_name IS '操作系统名称';
COMMENT ON COLUMN t_sys_monitor_host.os_version IS '操作系统版本';
COMMENT ON COLUMN t_sys_monitor_host.arch IS '系统架构';
COMMENT ON COLUMN t_sys_monitor_host.first_seen_time IS '首次发现时间';
COMMENT ON COLUMN t_sys_monitor_host.last_seen_time IS '最后发现时间';

CREATE TABLE t_sys_monitor_instance (
    id bigint PRIMARY KEY,
    instance_id varchar(100) NOT NULL,
    host_id varchar(100) NOT NULL,
    application_name varchar(200) NOT NULL,
    application_version varchar(100),
    first_seen_time timestamptz NOT NULL,
    last_seen_time timestamptz NOT NULL,
    last_start_time timestamptz NOT NULL
);
CREATE UNIQUE INDEX uk_sys_monitor_instance_instance_id ON t_sys_monitor_instance(instance_id);
CREATE INDEX idx_sys_monitor_instance_host ON t_sys_monitor_instance(host_id);
COMMENT ON TABLE t_sys_monitor_instance IS '应用实例监控目录';
COMMENT ON COLUMN t_sys_monitor_instance.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_instance.instance_id IS '应用实例稳定标识';
COMMENT ON COLUMN t_sys_monitor_instance.host_id IS '运行主机稳定标识';
COMMENT ON COLUMN t_sys_monitor_instance.application_name IS '应用名称';
COMMENT ON COLUMN t_sys_monitor_instance.application_version IS '应用版本';
COMMENT ON COLUMN t_sys_monitor_instance.first_seen_time IS '首次发现时间';
COMMENT ON COLUMN t_sys_monitor_instance.last_seen_time IS '最后发现时间';
COMMENT ON COLUMN t_sys_monitor_instance.last_start_time IS '最后启动时间';

CREATE TABLE t_sys_monitor_host_history (
    id bigint PRIMARY KEY,
    host_id varchar(100) NOT NULL,
    sample_bucket timestamptz NOT NULL,
    sample_time timestamptz NOT NULL,
    cpu_usage numeric(8,6), load_average numeric(12,4),
    memory_total bigint, memory_used bigint, swap_total bigint, swap_used bigint,
    filesystem_total bigint, filesystem_used bigint,
    disk_read_rate numeric(20,2), disk_write_rate numeric(20,2),
    network_receive_rate numeric(20,2), network_transmit_rate numeric(20,2)
);
CREATE UNIQUE INDEX uk_sys_monitor_host_history_bucket ON t_sys_monitor_host_history(host_id, sample_bucket);
CREATE INDEX idx_sys_monitor_host_history_time ON t_sys_monitor_host_history(host_id, sample_time DESC);
COMMENT ON TABLE t_sys_monitor_host_history IS '主机监控历史';
COMMENT ON COLUMN t_sys_monitor_host_history.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_host_history.host_id IS '运行主机稳定标识';
COMMENT ON COLUMN t_sys_monitor_host_history.sample_bucket IS '采样时间桶';
COMMENT ON COLUMN t_sys_monitor_host_history.sample_time IS '采样时间';
COMMENT ON COLUMN t_sys_monitor_host_history.cpu_usage IS 'CPU 使用率';
COMMENT ON COLUMN t_sys_monitor_host_history.load_average IS '系统负载均值';
COMMENT ON COLUMN t_sys_monitor_host_history.memory_total IS '物理内存总字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.memory_used IS '物理内存已用字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.swap_total IS '交换区总字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.swap_used IS '交换区已用字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.filesystem_total IS '文件系统总字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.filesystem_used IS '文件系统已用字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.disk_read_rate IS '磁盘每秒读取字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.disk_write_rate IS '磁盘每秒写入字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.network_receive_rate IS '网络每秒接收字节数';
COMMENT ON COLUMN t_sys_monitor_host_history.network_transmit_rate IS '网络每秒发送字节数';

CREATE TABLE t_sys_monitor_instance_history (
    id bigint PRIMARY KEY,
    instance_id varchar(100) NOT NULL, host_id varchar(100) NOT NULL,
    sample_bucket timestamptz NOT NULL, sample_time timestamptz NOT NULL,
    process_cpu numeric(8,6), heap_used bigint, heap_max bigint,
    thread_count integer, blocked_thread_count integer,
    gc_count bigint, gc_duration_ms bigint,
    http_request_rate numeric(20,4), http_4xx_rate numeric(20,4), http_5xx_rate numeric(20,4),
    http_p95_ms numeric(20,4), http_p99_ms numeric(20,4),
    db_active integer, db_max integer, db_waiting integer,
    health_status varchar(30), database_status varchar(30), redis_status varchar(30)
);
CREATE UNIQUE INDEX uk_sys_monitor_instance_history_bucket ON t_sys_monitor_instance_history(instance_id, sample_bucket);
CREATE INDEX idx_sys_monitor_instance_history_time ON t_sys_monitor_instance_history(instance_id, sample_time DESC);
COMMENT ON TABLE t_sys_monitor_instance_history IS '应用实例监控历史';
COMMENT ON COLUMN t_sys_monitor_instance_history.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_instance_history.instance_id IS '应用实例稳定标识';
COMMENT ON COLUMN t_sys_monitor_instance_history.host_id IS '运行主机稳定标识';
COMMENT ON COLUMN t_sys_monitor_instance_history.sample_bucket IS '采样时间桶';
COMMENT ON COLUMN t_sys_monitor_instance_history.sample_time IS '采样时间';
COMMENT ON COLUMN t_sys_monitor_instance_history.process_cpu IS 'JVM 进程 CPU 使用率';
COMMENT ON COLUMN t_sys_monitor_instance_history.heap_used IS 'JVM 堆内存已用字节数';
COMMENT ON COLUMN t_sys_monitor_instance_history.heap_max IS 'JVM 堆内存上限字节数';
COMMENT ON COLUMN t_sys_monitor_instance_history.thread_count IS 'JVM 线程数';
COMMENT ON COLUMN t_sys_monitor_instance_history.blocked_thread_count IS 'JVM 阻塞线程数';
COMMENT ON COLUMN t_sys_monitor_instance_history.gc_count IS 'GC 累计次数';
COMMENT ON COLUMN t_sys_monitor_instance_history.gc_duration_ms IS 'GC 累计耗时毫秒数';
COMMENT ON COLUMN t_sys_monitor_instance_history.http_request_rate IS 'HTTP 每秒请求数';
COMMENT ON COLUMN t_sys_monitor_instance_history.http_4xx_rate IS 'HTTP 4xx 每秒请求数';
COMMENT ON COLUMN t_sys_monitor_instance_history.http_5xx_rate IS 'HTTP 5xx 每秒请求数';
COMMENT ON COLUMN t_sys_monitor_instance_history.http_p95_ms IS 'HTTP P95 耗时毫秒数';
COMMENT ON COLUMN t_sys_monitor_instance_history.http_p99_ms IS 'HTTP P99 耗时毫秒数';
COMMENT ON COLUMN t_sys_monitor_instance_history.db_active IS '数据库连接池活跃连接数';
COMMENT ON COLUMN t_sys_monitor_instance_history.db_max IS '数据库连接池最大连接数';
COMMENT ON COLUMN t_sys_monitor_instance_history.db_waiting IS '数据库连接池等待线程数';
COMMENT ON COLUMN t_sys_monitor_instance_history.health_status IS '应用健康状态';
COMMENT ON COLUMN t_sys_monitor_instance_history.database_status IS '数据库健康状态';
COMMENT ON COLUMN t_sys_monitor_instance_history.redis_status IS 'Redis 健康状态';

CREATE TABLE t_sys_monitor_alert_rule (
    id bigint PRIMARY KEY, rule_code varchar(80) NOT NULL, name varchar(200) NOT NULL,
    scope_type varchar(20) NOT NULL, enabled boolean NOT NULL,
    severity varchar(20) NOT NULL, threshold numeric(20,6) NOT NULL,
    duration_seconds integer NOT NULL, recovery_threshold numeric(20,6),
    repeat_interval_seconds integer NOT NULL, email_enabled boolean NOT NULL,
    description varchar(500), version integer NOT NULL DEFAULT 0,
    create_time timestamptz NOT NULL, update_time timestamptz NOT NULL,
    create_user bigint, update_user bigint
);
CREATE UNIQUE INDEX uk_sys_monitor_alert_rule_code ON t_sys_monitor_alert_rule(rule_code);
COMMENT ON TABLE t_sys_monitor_alert_rule IS '监控告警规则';
COMMENT ON COLUMN t_sys_monitor_alert_rule.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_alert_rule.rule_code IS '预定义规则编码';
COMMENT ON COLUMN t_sys_monitor_alert_rule.name IS '名称';
COMMENT ON COLUMN t_sys_monitor_alert_rule.scope_type IS '作用对象类型';
COMMENT ON COLUMN t_sys_monitor_alert_rule.enabled IS '启用状态';
COMMENT ON COLUMN t_sys_monitor_alert_rule.severity IS '严重程度';
COMMENT ON COLUMN t_sys_monitor_alert_rule.threshold IS '触发阈值';
COMMENT ON COLUMN t_sys_monitor_alert_rule.duration_seconds IS '持续时间秒数';
COMMENT ON COLUMN t_sys_monitor_alert_rule.recovery_threshold IS '恢复阈值';
COMMENT ON COLUMN t_sys_monitor_alert_rule.repeat_interval_seconds IS '重复通知间隔秒数';
COMMENT ON COLUMN t_sys_monitor_alert_rule.email_enabled IS '是否发送邮件';
COMMENT ON COLUMN t_sys_monitor_alert_rule.description IS '描述';
COMMENT ON COLUMN t_sys_monitor_alert_rule.version IS '乐观锁版本号';
COMMENT ON COLUMN t_sys_monitor_alert_rule.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_monitor_alert_rule.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_monitor_alert_rule.create_user IS '创建人用户ID';
COMMENT ON COLUMN t_sys_monitor_alert_rule.update_user IS '更新人用户ID';

CREATE TABLE t_sys_monitor_alert_rule_recipient (
    id bigint PRIMARY KEY, rule_id bigint NOT NULL, user_id bigint NOT NULL,
    create_time timestamptz NOT NULL,
    CONSTRAINT fk_monitor_alert_recipient_rule FOREIGN KEY(rule_id) REFERENCES t_sys_monitor_alert_rule(id),
    CONSTRAINT fk_monitor_alert_recipient_user FOREIGN KEY(user_id) REFERENCES t_sys_user(id)
);
CREATE UNIQUE INDEX uk_sys_monitor_alert_recipient ON t_sys_monitor_alert_rule_recipient(rule_id, user_id);
COMMENT ON TABLE t_sys_monitor_alert_rule_recipient IS '监控告警邮件接收人';
COMMENT ON COLUMN t_sys_monitor_alert_rule_recipient.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_alert_rule_recipient.rule_id IS '告警规则ID';
COMMENT ON COLUMN t_sys_monitor_alert_rule_recipient.user_id IS '接收用户ID';
COMMENT ON COLUMN t_sys_monitor_alert_rule_recipient.create_time IS '创建时间';

CREATE TABLE t_sys_monitor_alert_incident (
    id bigint PRIMARY KEY, rule_id bigint NOT NULL, rule_code varchar(80) NOT NULL,
    scope_type varchar(20) NOT NULL, scope_id varchar(100) NOT NULL,
    status varchar(20) NOT NULL, cycle_key varchar(80) NOT NULL,
    started_at timestamptz NOT NULL, fired_at timestamptz, recovered_at timestamptz,
    last_evaluated_at timestamptz NOT NULL, last_value numeric(20,6), peak_value numeric(20,6),
    threshold numeric(20,6) NOT NULL, last_notified_at timestamptz,
    notification_count integer NOT NULL DEFAULT 0, summary varchar(500) NOT NULL,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT fk_monitor_alert_incident_rule FOREIGN KEY(rule_id) REFERENCES t_sys_monitor_alert_rule(id)
);
CREATE UNIQUE INDEX uk_sys_monitor_alert_incident_cycle ON t_sys_monitor_alert_incident(rule_id, scope_type, scope_id, cycle_key);
CREATE UNIQUE INDEX uk_sys_monitor_alert_incident_active ON t_sys_monitor_alert_incident(rule_id, scope_type, scope_id) WHERE status IN ('PENDING','FIRING');
CREATE INDEX idx_sys_monitor_alert_incident_status_time ON t_sys_monitor_alert_incident(status, last_evaluated_at DESC);
CREATE INDEX idx_sys_monitor_alert_incident_scope ON t_sys_monitor_alert_incident(scope_type, scope_id, started_at DESC);
COMMENT ON TABLE t_sys_monitor_alert_incident IS '监控告警事件';
COMMENT ON COLUMN t_sys_monitor_alert_incident.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_alert_incident.rule_id IS '告警规则ID';
COMMENT ON COLUMN t_sys_monitor_alert_incident.rule_code IS '规则编码';
COMMENT ON COLUMN t_sys_monitor_alert_incident.scope_type IS '作用对象类型';
COMMENT ON COLUMN t_sys_monitor_alert_incident.scope_id IS '作用对象标识';
COMMENT ON COLUMN t_sys_monitor_alert_incident.status IS '告警状态';
COMMENT ON COLUMN t_sys_monitor_alert_incident.cycle_key IS '告警周期幂等键';
COMMENT ON COLUMN t_sys_monitor_alert_incident.started_at IS '异常开始时间';
COMMENT ON COLUMN t_sys_monitor_alert_incident.fired_at IS '触发时间';
COMMENT ON COLUMN t_sys_monitor_alert_incident.recovered_at IS '恢复时间';
COMMENT ON COLUMN t_sys_monitor_alert_incident.last_evaluated_at IS '最后评估时间';
COMMENT ON COLUMN t_sys_monitor_alert_incident.last_value IS '最新值';
COMMENT ON COLUMN t_sys_monitor_alert_incident.peak_value IS '峰值';
COMMENT ON COLUMN t_sys_monitor_alert_incident.threshold IS '触发阈值快照';
COMMENT ON COLUMN t_sys_monitor_alert_incident.last_notified_at IS '最后通知时间';
COMMENT ON COLUMN t_sys_monitor_alert_incident.notification_count IS '通知次数';
COMMENT ON COLUMN t_sys_monitor_alert_incident.summary IS '摘要';
COMMENT ON COLUMN t_sys_monitor_alert_incident.version IS '乐观锁版本号';

CREATE TABLE t_sys_monitor_alert_notification (
    id bigint PRIMARY KEY, incident_id bigint NOT NULL, notification_type varchar(20) NOT NULL,
    sequence_no integer NOT NULL, status varchar(20) NOT NULL, attempt_count integer NOT NULL DEFAULT 0,
    next_attempt_time timestamptz NOT NULL, claimed_time timestamptz, email_task_id bigint, error_message varchar(500),
    create_time timestamptz NOT NULL, completed_time timestamptz,
    CONSTRAINT fk_monitor_alert_notification_incident FOREIGN KEY(incident_id) REFERENCES t_sys_monitor_alert_incident(id)
);
CREATE UNIQUE INDEX uk_sys_monitor_alert_notification_sequence
    ON t_sys_monitor_alert_notification(incident_id, notification_type, sequence_no);
CREATE INDEX idx_sys_monitor_alert_notification_dispatch
    ON t_sys_monitor_alert_notification(status, next_attempt_time);
COMMENT ON TABLE t_sys_monitor_alert_notification IS '监控告警邮件通知发件箱';
COMMENT ON COLUMN t_sys_monitor_alert_notification.id IS 'ID';
COMMENT ON COLUMN t_sys_monitor_alert_notification.incident_id IS '告警事件ID';
COMMENT ON COLUMN t_sys_monitor_alert_notification.notification_type IS '通知类型';
COMMENT ON COLUMN t_sys_monitor_alert_notification.sequence_no IS '同类型通知序号';
COMMENT ON COLUMN t_sys_monitor_alert_notification.status IS '投递状态';
COMMENT ON COLUMN t_sys_monitor_alert_notification.attempt_count IS '尝试次数';
COMMENT ON COLUMN t_sys_monitor_alert_notification.next_attempt_time IS '下次尝试时间';
COMMENT ON COLUMN t_sys_monitor_alert_notification.claimed_time IS '领取时间';
COMMENT ON COLUMN t_sys_monitor_alert_notification.email_task_id IS '邮件任务ID';
COMMENT ON COLUMN t_sys_monitor_alert_notification.error_message IS '错误信息';
COMMENT ON COLUMN t_sys_monitor_alert_notification.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_monitor_alert_notification.completed_time IS '完成时间';

INSERT INTO t_sys_monitor_alert_rule(id,rule_code,name,scope_type,enabled,severity,threshold,duration_seconds,recovery_threshold,repeat_interval_seconds,email_enabled,description,version,create_time,update_time)
VALUES
(490000000000000001,'HOST_CPU_HIGH','主机 CPU 使用率过高','HOST',true,'WARNING',0.90,300,0.80,1800,false,'CPU 使用率持续超过阈值',0,now(),now()),
(490000000000000002,'HOST_MEMORY_HIGH','主机内存使用率过高','HOST',true,'WARNING',0.90,300,0.80,1800,false,'物理内存使用率持续超过阈值',0,now(),now()),
(490000000000000003,'HOST_SWAP_HIGH','主机交换空间使用率过高','HOST',true,'WARNING',0.80,300,0.70,1800,false,'交换空间使用率持续超过阈值',0,now(),now()),
(490000000000000004,'HOST_DISK_HIGH','主机文件系统使用率过高','HOST',true,'CRITICAL',0.90,300,0.85,1800,false,'任一重要文件系统持续超过阈值',0,now(),now()),
(490000000000000005,'INSTANCE_HEAP_HIGH','实例堆内存使用率过高','INSTANCE',true,'WARNING',0.90,300,0.80,1800,false,'JVM 堆内存使用率持续超过阈值',0,now(),now()),
(490000000000000006,'INSTANCE_BLOCKED_THREADS','实例阻塞线程过多','INSTANCE',true,'WARNING',5,120,1,1800,false,'阻塞线程数持续超过阈值',0,now(),now()),
(490000000000000007,'INSTANCE_OFFLINE','应用实例离线','INSTANCE',true,'CRITICAL',1,30,0,1800,false,'持久化目录中的实例心跳过期',0,now(),now()),
(490000000000000008,'HTTP_ERROR_RATE_HIGH','HTTP 5xx 速率过高','INSTANCE',true,'CRITICAL',1,300,0.2,1800,false,'HTTP 5xx 每秒速率持续超过阈值',0,now(),now()),
(490000000000000009,'HTTP_LATENCY_HIGH','HTTP P95 延迟过高','INSTANCE',true,'WARNING',1000,300,800,1800,false,'HTTP P95 延迟持续超过阈值（毫秒）',0,now(),now()),
(490000000000000010,'DB_HEALTH_DOWN','数据库健康检查失败','INSTANCE',true,'CRITICAL',1,30,0,1800,false,'数据库健康状态异常',0,now(),now()),
(490000000000000011,'DB_POOL_HIGH','数据库连接池使用率过高','INSTANCE',true,'WARNING',0.90,300,0.80,1800,false,'数据库连接池使用率持续超过阈值',0,now(),now()),
(490000000000000012,'DB_POOL_WAITING','数据库连接池存在等待','INSTANCE',true,'CRITICAL',1,60,0,1800,false,'连接池等待线程持续存在',0,now(),now()),
(490000000000000013,'REDIS_HEALTH_DOWN','Redis 健康检查失败','INSTANCE',true,'CRITICAL',1,30,0,1800,false,'Redis 健康状态异常',0,now(),now());

-- 正式替换尚未稳定的 Node 功能身份，不保留旧页面或权限。
UPDATE t_sys_feature SET feature_key='sys/monitor/runtime', default_name='运行监控', description='主机、应用实例、实时快照与历史趋势', update_time=now(), version=version+1 WHERE feature_key='sys/monitor/node';
UPDATE t_sys_permission SET name='运行监控-查看', number='sys:monitor:runtime:view', update_time=now(), version=version+1 WHERE number='sys:monitor:node:view';
UPDATE t_sys_menu SET number='runtime_monitor', name='运行监控', path='/sys/monitor/runtime', component='sys/monitor/runtime', description='查看主机与应用实例实时状态及历史趋势', update_time=now(), version=version+1 WHERE number='node_monitoring';

INSERT INTO t_sys_feature(id,feature_key,app_id,default_name,default_seq,description,visible,source,create_time,update_time,version)
VALUES(450000000000000041,'sys/monitor/alert',30,'监控告警',65,'配置预定义告警规则并查询告警事件',true,'SYSTEM',now(),now(),0);
INSERT INTO t_sys_permission(id,name,number,feature_id,create_time,update_time,version) VALUES
(490000000000000101,'监控告警-查看','sys:monitor:alert:view',450000000000000041,now(),now(),0),
(490000000000000102,'监控告警-管理','sys:monitor:alert:manage',450000000000000041,now(),now(),0);
INSERT INTO t_sys_role_perms(id,role_id,permission_id,create_time,create_user) VALUES
(490000000000000201,1,490000000000000101,now(),1),(490000000000000202,1,490000000000000102,now(),1);
INSERT INTO t_sys_menu(id,number,name,level,parent_id,app_id,permission_id,path,component,icon,description,sort,enabled,create_time,update_time,version,feature_id,target_type)
VALUES(490000000000000301,'monitor_alert','监控告警',1,470000000000000011,30,490000000000000101,'/sys/monitor/alert','sys/monitor/alert','AlertOutlined','告警规则与事件',20,true,now(),now(),0,450000000000000041,'INTERNAL_PAGE');

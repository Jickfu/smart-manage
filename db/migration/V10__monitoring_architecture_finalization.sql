-- 内建监控第二轮架构定型：实例生命周期、告警关闭语义、规则元数据与主机文件系统历史。

ALTER TABLE t_sys_monitor_instance
    ADD COLUMN lifecycle varchar(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN retired_at timestamptz;
ALTER TABLE t_sys_monitor_instance ADD CONSTRAINT ck_monitor_instance_lifecycle
    CHECK (lifecycle IN ('ACTIVE','RETIRED'));
COMMENT ON COLUMN t_sys_monitor_instance.lifecycle IS '实例生命周期：ACTIVE/RETIRED';
COMMENT ON COLUMN t_sys_monitor_instance.retired_at IS '退役时间';
CREATE INDEX idx_sys_monitor_instance_lifecycle ON t_sys_monitor_instance(lifecycle, host_id);

ALTER TABLE t_sys_monitor_alert_incident
    ADD COLUMN close_reason varchar(30);
ALTER TABLE t_sys_monitor_alert_incident ADD CONSTRAINT ck_monitor_alert_incident_status
    CHECK (status IN ('PENDING','FIRING','RECOVERED','CLOSED'));
ALTER TABLE t_sys_monitor_alert_incident ADD CONSTRAINT ck_monitor_alert_incident_close_reason
    CHECK ((status = 'CLOSED' AND close_reason IN ('PENDING_CLEARED','INSTANCE_RETIRED','RULE_DISABLED'))
        OR (status <> 'CLOSED' AND close_reason IS NULL));
COMMENT ON COLUMN t_sys_monitor_alert_incident.close_reason IS '非恢复性关闭原因';

ALTER TABLE t_sys_monitor_alert_rule
    ADD COLUMN value_kind varchar(20),
    ADD COLUMN display_unit varchar(20),
    ADD COLUMN min_value numeric(20,6),
    ADD COLUMN max_value numeric(20,6),
    ADD COLUMN recommended_threshold numeric(20,6);
COMMENT ON COLUMN t_sys_monitor_alert_rule.value_kind IS '值类型：RATIO/COUNT/BOOLEAN/RATE/DURATION_MS';
COMMENT ON COLUMN t_sys_monitor_alert_rule.display_unit IS '展示单位';
COMMENT ON COLUMN t_sys_monitor_alert_rule.min_value IS '最小可配置值';
COMMENT ON COLUMN t_sys_monitor_alert_rule.max_value IS '最大可配置值';
COMMENT ON COLUMN t_sys_monitor_alert_rule.recommended_threshold IS '推荐触发阈值';

UPDATE t_sys_monitor_alert_rule SET value_kind='RATIO',display_unit='%',min_value=0,max_value=1,recommended_threshold=0.9
WHERE rule_code IN ('HOST_CPU_HIGH','HOST_MEMORY_HIGH','HOST_SWAP_HIGH','HOST_DISK_HIGH','INSTANCE_HEAP_HIGH','DB_POOL_HIGH');
UPDATE t_sys_monitor_alert_rule SET value_kind='COUNT',display_unit='个',min_value=0,max_value=NULL,recommended_threshold=1
WHERE rule_code IN ('INSTANCE_BLOCKED_THREADS','DB_POOL_WAITING');
UPDATE t_sys_monitor_alert_rule SET value_kind='RATE',display_unit='req/s',min_value=0,max_value=NULL,recommended_threshold=1
WHERE rule_code='HTTP_ERROR_RATE_HIGH';
UPDATE t_sys_monitor_alert_rule SET value_kind='DURATION_MS',display_unit='ms',min_value=0,max_value=NULL,recommended_threshold=1000
WHERE rule_code='HTTP_LATENCY_HIGH';
UPDATE t_sys_monitor_alert_rule SET value_kind='BOOLEAN',display_unit='',min_value=0,max_value=1,recommended_threshold=1,
    threshold=1,recovery_threshold=0
WHERE rule_code IN ('DB_HEALTH_DOWN','REDIS_HEALTH_DOWN','INSTANCE_OFFLINE');
ALTER TABLE t_sys_monitor_alert_rule ALTER COLUMN value_kind SET NOT NULL;
ALTER TABLE t_sys_monitor_alert_rule ALTER COLUMN display_unit SET NOT NULL;
ALTER TABLE t_sys_monitor_alert_rule ALTER COLUMN min_value SET NOT NULL;
ALTER TABLE t_sys_monitor_alert_rule ALTER COLUMN recommended_threshold SET NOT NULL;
ALTER TABLE t_sys_monitor_alert_rule ADD CONSTRAINT ck_monitor_alert_rule_scope CHECK (scope_type IN ('HOST','INSTANCE'));
ALTER TABLE t_sys_monitor_alert_rule ADD CONSTRAINT ck_monitor_alert_rule_severity CHECK (severity IN ('INFO','WARNING','CRITICAL'));
ALTER TABLE t_sys_monitor_alert_rule ADD CONSTRAINT ck_monitor_alert_rule_value_kind CHECK (value_kind IN ('RATIO','COUNT','BOOLEAN','RATE','DURATION_MS'));
ALTER TABLE t_sys_monitor_alert_rule ADD CONSTRAINT ck_monitor_alert_rule_range CHECK
    (threshold >= min_value AND (max_value IS NULL OR threshold <= max_value)
     AND (recovery_threshold IS NULL OR (recovery_threshold >= min_value AND (max_value IS NULL OR recovery_threshold <= max_value))));

ALTER TABLE t_sys_monitor_alert_notification ADD CONSTRAINT ck_monitor_alert_notification_status
    CHECK (status IN ('PENDING','PROCESSING','SUCCESS','RETRY','FAILED','SKIPPED'));

ALTER TABLE t_sys_monitor_host_history
    ADD COLUMN worst_filesystem_usage numeric(8,6),
    ADD COLUMN worst_mount varchar(500);
COMMENT ON COLUMN t_sys_monitor_host_history.worst_filesystem_usage IS '最高使用率文件系统使用率';
COMMENT ON COLUMN t_sys_monitor_host_history.worst_mount IS '最高使用率挂载点';

-- 收口为单一“运维工具”产品分组，不保留数据/脚本两个并列分组。
UPDATE t_sys_menu SET name='运维工具',description='缓存、数据库与受控脚本运维能力',update_time=now(),version=version+1
WHERE number='data_operations';
UPDATE t_sys_menu SET parent_id=470000000000000014,update_time=now(),version=version+1
WHERE parent_id=470000000000000015;
DELETE FROM t_sys_menu WHERE number='script_operations';

INSERT INTO t_sys_permission(id,name,number,feature_id,create_time,update_time,version)
SELECT 490000000000000103,'运行监控-管理','sys:monitor:runtime:manage',id,now(),now(),0
FROM t_sys_feature WHERE feature_key='sys/monitor/runtime';
INSERT INTO t_sys_role_perms(id,role_id,permission_id,create_time,create_user)
VALUES(490000000000000203,1,490000000000000103,now(),1);

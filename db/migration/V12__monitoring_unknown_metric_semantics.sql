-- 指标不可用会打断 PENDING 的连续异常周期，但不表示故障恢复。
ALTER TABLE t_sys_monitor_alert_incident DROP CONSTRAINT ck_monitor_alert_incident_close_reason;
ALTER TABLE t_sys_monitor_alert_incident ADD CONSTRAINT ck_monitor_alert_incident_close_reason
    CHECK ((status = 'CLOSED' AND close_reason IN
            ('PENDING_CLEARED','INSTANCE_RETIRED','RULE_DISABLED','METRIC_UNAVAILABLE'))
        OR (status <> 'CLOSED' AND close_reason IS NULL));
COMMENT ON COLUMN t_sys_monitor_alert_incident.close_reason IS
    '非恢复性关闭原因：条件清除、实例退役、规则停用或指标不可用';

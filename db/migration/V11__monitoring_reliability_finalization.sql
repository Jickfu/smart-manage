-- 内建监控最终可靠性收口：移除无法在 PostgreSQL 完全不可用时兑现的自依赖告警规则。

DELETE FROM t_sys_monitor_alert_notification
WHERE incident_id IN (
    SELECT id FROM t_sys_monitor_alert_incident WHERE rule_code='DB_HEALTH_DOWN'
);
DELETE FROM t_sys_monitor_alert_incident WHERE rule_code='DB_HEALTH_DOWN';
DELETE FROM t_sys_monitor_alert_rule_recipient
WHERE rule_id IN (SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='DB_HEALTH_DOWN');
DELETE FROM t_sys_monitor_alert_rule WHERE rule_code='DB_HEALTH_DOWN';

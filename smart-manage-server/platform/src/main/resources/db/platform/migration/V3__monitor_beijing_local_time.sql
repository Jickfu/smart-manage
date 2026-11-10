-- 监控时间与普通业务字段采用同一北京时间口径，转换独立于迁移会话时区。

ALTER TABLE t_sys_monitor_alert_incident
    ALTER COLUMN started_at TYPE timestamp without time zone USING started_at AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN fired_at TYPE timestamp without time zone USING fired_at AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN recovered_at TYPE timestamp without time zone USING recovered_at AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN last_evaluated_at TYPE timestamp without time zone USING last_evaluated_at AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN last_notified_at TYPE timestamp without time zone USING last_notified_at AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_alert_notification
    ALTER COLUMN next_attempt_time TYPE timestamp without time zone USING next_attempt_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN claimed_time TYPE timestamp without time zone USING claimed_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN create_time TYPE timestamp without time zone USING create_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN completed_time TYPE timestamp without time zone USING completed_time AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_alert_rule
    ALTER COLUMN create_time TYPE timestamp without time zone USING create_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN update_time TYPE timestamp without time zone USING update_time AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_alert_rule_recipient
    ALTER COLUMN create_time TYPE timestamp without time zone USING create_time AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_host
    ALTER COLUMN first_seen_time TYPE timestamp without time zone USING first_seen_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN last_seen_time TYPE timestamp without time zone USING last_seen_time AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_host_history
    ALTER COLUMN sample_bucket TYPE timestamp without time zone USING sample_bucket AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN sample_time TYPE timestamp without time zone USING sample_time AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_instance
    ALTER COLUMN first_seen_time TYPE timestamp without time zone USING first_seen_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN last_seen_time TYPE timestamp without time zone USING last_seen_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN last_start_time TYPE timestamp without time zone USING last_start_time AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN retired_at TYPE timestamp without time zone USING retired_at AT TIME ZONE 'Asia/Shanghai';

ALTER TABLE t_sys_monitor_instance_history
    ALTER COLUMN sample_bucket TYPE timestamp without time zone USING sample_bucket AT TIME ZONE 'Asia/Shanghai',
    ALTER COLUMN sample_time TYPE timestamp without time zone USING sample_time AT TIME ZONE 'Asia/Shanghai';

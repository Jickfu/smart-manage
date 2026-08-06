ALTER TABLE t_sys_job_log
    ADD COLUMN instance_id varchar(200),
    ADD COLUMN fire_instance_id varchar(200);

CREATE INDEX idx_sys_job_log_fire_instance_id ON t_sys_job_log (fire_instance_id);

COMMENT ON COLUMN t_sys_job_log.instance_id IS '实际执行应用实例（Quartz instanceId）';
COMMENT ON COLUMN t_sys_job_log.fire_instance_id IS 'Quartz 本次触发实例ID，用于重复执行对账';

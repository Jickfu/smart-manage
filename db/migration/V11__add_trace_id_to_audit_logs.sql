ALTER TABLE t_sys_login_log
    ADD COLUMN trace_id varchar(64);

COMMENT ON COLUMN t_sys_login_log.trace_id IS '链路追踪ID';

CREATE INDEX idx_sys_login_trace_id ON t_sys_login_log (trace_id);

ALTER TABLE t_sys_operate_log
    ADD COLUMN trace_id varchar(64);

COMMENT ON COLUMN t_sys_operate_log.trace_id IS '链路追踪ID';

CREATE INDEX idx_sys_operate_trace_id ON t_sys_operate_log (trace_id);

ALTER TABLE t_sys_job_log
    ADD COLUMN trace_id varchar(64);

COMMENT ON COLUMN t_sys_job_log.trace_id IS '链路追踪ID';

CREATE INDEX idx_sys_job_log_trace_id ON t_sys_job_log (trace_id);

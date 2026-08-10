-- 当前日志均为本地测试数据，本迁移按已确认的架构边界重建日志表，不保留旧记录。
DROP TABLE IF EXISTS public.t_sys_login_log_history;
DROP TABLE IF EXISTS public.t_sys_operate_log_history;
DROP TABLE IF EXISTS public.t_sys_sql_log_history;
DROP TABLE IF EXISTS public.t_sys_script_log_history;
DROP TABLE IF EXISTS public.t_sys_job_log_history;
DROP TABLE public.t_sys_login_log;
DROP TABLE public.t_sys_operate_log;
DROP TABLE public.t_sys_sql_log;
DROP TABLE public.t_sys_script_log;
DROP TABLE public.t_sys_job_log;

CREATE TABLE public.t_sys_login_log (
    id bigint NOT NULL,
    user_id bigint,
    username varchar(128),
    nickname varchar(255),
    event_type varchar(32) NOT NULL,
    success boolean NOT NULL DEFAULT true,
    fail_reason varchar(512),
    ip varchar(64),
    user_agent varchar(1024),
    trace_id varchar(64),
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT pk_sys_login_log PRIMARY KEY (create_time, id)
) PARTITION BY RANGE (create_time);

CREATE TABLE public.t_sys_operate_log (
    id bigint NOT NULL,
    biz_name varchar(256),
    success boolean NOT NULL DEFAULT true,
    error_msg text,
    request_method varchar(32),
    request_uri varchar(512),
    ip varchar(64),
    user_agent varchar(1024),
    class_name varchar(256),
    method_name varchar(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username varchar(128),
    trace_id varchar(64),
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT pk_sys_operate_log PRIMARY KEY (create_time, id)
) PARTITION BY RANGE (create_time);

CREATE TABLE public.t_sys_sql_log (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer NOT NULL DEFAULT 0,
    result_type varchar(50) NOT NULL,
    row_count integer NOT NULL DEFAULT 0,
    error_message text,
    create_name varchar(200),
    create_ip varchar(100),
    remark varchar(500),
    create_user bigint,
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user bigint,
    update_time timestamp without time zone,
    CONSTRAINT pk_sys_sql_log PRIMARY KEY (create_time, id)
) PARTITION BY RANGE (create_time);

CREATE TABLE public.t_sys_script_log (
    id bigint NOT NULL,
    script_id bigint,
    script_name varchar(200),
    script_content text NOT NULL,
    transaction_mode varchar(20) NOT NULL,
    execute_status varchar(20) NOT NULL,
    execute_duration integer NOT NULL DEFAULT 0,
    transaction_result varchar(20) NOT NULL,
    output text,
    error_message text,
    create_name varchar(100),
    create_ip varchar(64),
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sys_script_log PRIMARY KEY (create_time, id)
) PARTITION BY RANGE (create_time);

CREATE TABLE public.t_sys_job_log (
    id bigint NOT NULL,
    job_id bigint,
    job_name varchar(200),
    job_group varchar(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status varchar(20),
    error_message text,
    trace_id varchar(64),
    instance_id varchar(200),
    fire_instance_id varchar(200),
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sys_job_log PRIMARY KEY (start_time, id)
) PARTITION BY RANGE (start_time);

-- 历史父表与在线父表保持完全相同的列定义，确保整月物理分区可以直接切换归属。
CREATE TABLE public.t_sys_login_log_history
    (LIKE public.t_sys_login_log INCLUDING DEFAULTS INCLUDING COMMENTS)
    PARTITION BY RANGE (create_time);
ALTER TABLE public.t_sys_login_log_history
    ADD CONSTRAINT pk_sys_login_log_history PRIMARY KEY (create_time, id);

CREATE TABLE public.t_sys_operate_log_history
    (LIKE public.t_sys_operate_log INCLUDING DEFAULTS INCLUDING COMMENTS)
    PARTITION BY RANGE (create_time);
ALTER TABLE public.t_sys_operate_log_history
    ADD CONSTRAINT pk_sys_operate_log_history PRIMARY KEY (create_time, id);

CREATE TABLE public.t_sys_sql_log_history
    (LIKE public.t_sys_sql_log INCLUDING DEFAULTS INCLUDING COMMENTS)
    PARTITION BY RANGE (create_time);
ALTER TABLE public.t_sys_sql_log_history
    ADD CONSTRAINT pk_sys_sql_log_history PRIMARY KEY (create_time, id);

CREATE TABLE public.t_sys_script_log_history
    (LIKE public.t_sys_script_log INCLUDING DEFAULTS INCLUDING COMMENTS)
    PARTITION BY RANGE (create_time);
ALTER TABLE public.t_sys_script_log_history
    ADD CONSTRAINT pk_sys_script_log_history PRIMARY KEY (create_time, id);

CREATE TABLE public.t_sys_job_log_history
    (LIKE public.t_sys_job_log INCLUDING DEFAULTS INCLUDING COMMENTS)
    PARTITION BY RANGE (start_time);
ALTER TABLE public.t_sys_job_log_history
    ADD CONSTRAINT pk_sys_job_log_history PRIMARY KEY (start_time, id);

COMMENT ON TABLE public.t_sys_login_log IS '系统服务-登录登出日志（在线分区父表）';
COMMENT ON TABLE public.t_sys_login_log_history IS '系统服务-登录登出日志（历史分区父表）';
COMMENT ON TABLE public.t_sys_operate_log IS '系统服务-操作日志（在线分区父表）';
COMMENT ON TABLE public.t_sys_operate_log_history IS '系统服务-操作日志（历史分区父表）';
COMMENT ON TABLE public.t_sys_sql_log IS 'SQL执行日志（在线分区父表）';
COMMENT ON TABLE public.t_sys_sql_log_history IS 'SQL执行日志（历史分区父表）';
COMMENT ON TABLE public.t_sys_script_log IS '脚本控制台执行审计（在线分区父表）';
COMMENT ON TABLE public.t_sys_script_log_history IS '脚本控制台执行审计（历史分区父表）';
COMMENT ON TABLE public.t_sys_job_log IS '定时任务执行实例（在线分区父表）';
COMMENT ON TABLE public.t_sys_job_log_history IS '定时任务执行实例（历史分区父表）';

COMMENT ON COLUMN public.t_sys_login_log.id IS 'ID';
COMMENT ON COLUMN public.t_sys_login_log.user_id IS '用户ID';
COMMENT ON COLUMN public.t_sys_login_log.username IS '用户名';
COMMENT ON COLUMN public.t_sys_login_log.nickname IS '昵称';
COMMENT ON COLUMN public.t_sys_login_log.event_type IS '事件类型';
COMMENT ON COLUMN public.t_sys_login_log.success IS '是否成功';
COMMENT ON COLUMN public.t_sys_login_log.fail_reason IS '失败原因';
COMMENT ON COLUMN public.t_sys_login_log.ip IS 'IP地址';
COMMENT ON COLUMN public.t_sys_login_log.user_agent IS 'User-Agent';
COMMENT ON COLUMN public.t_sys_login_log.trace_id IS '链路追踪ID';
COMMENT ON COLUMN public.t_sys_login_log.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_login_log.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_login_log.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_login_log.update_user IS '修改人';

COMMENT ON COLUMN public.t_sys_operate_log.id IS 'ID';
COMMENT ON COLUMN public.t_sys_operate_log.biz_name IS '业务名称';
COMMENT ON COLUMN public.t_sys_operate_log.success IS '是否成功';
COMMENT ON COLUMN public.t_sys_operate_log.error_msg IS '错误信息';
COMMENT ON COLUMN public.t_sys_operate_log.request_method IS '请求方法';
COMMENT ON COLUMN public.t_sys_operate_log.request_uri IS '请求URI';
COMMENT ON COLUMN public.t_sys_operate_log.ip IS 'IP地址';
COMMENT ON COLUMN public.t_sys_operate_log.user_agent IS 'User-Agent';
COMMENT ON COLUMN public.t_sys_operate_log.class_name IS '类名';
COMMENT ON COLUMN public.t_sys_operate_log.method_name IS '方法名';
COMMENT ON COLUMN public.t_sys_operate_log.duration_ms IS '耗时(ms)';
COMMENT ON COLUMN public.t_sys_operate_log.request_params IS '请求参数';
COMMENT ON COLUMN public.t_sys_operate_log.response_body IS '响应内容';
COMMENT ON COLUMN public.t_sys_operate_log.user_id IS '用户ID';
COMMENT ON COLUMN public.t_sys_operate_log.username IS '用户名';
COMMENT ON COLUMN public.t_sys_operate_log.trace_id IS '链路追踪ID';
COMMENT ON COLUMN public.t_sys_operate_log.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_operate_log.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_operate_log.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_operate_log.update_user IS '修改人';

COMMENT ON COLUMN public.t_sys_sql_log.id IS 'ID';
COMMENT ON COLUMN public.t_sys_sql_log.sql_text IS 'SQL语句';
COMMENT ON COLUMN public.t_sys_sql_log.execute_duration IS '执行耗时(ms)';
COMMENT ON COLUMN public.t_sys_sql_log.result_type IS '结果类型';
COMMENT ON COLUMN public.t_sys_sql_log.row_count IS '影响行数';
COMMENT ON COLUMN public.t_sys_sql_log.error_message IS '错误信息';
COMMENT ON COLUMN public.t_sys_sql_log.create_name IS '操作人';
COMMENT ON COLUMN public.t_sys_sql_log.create_ip IS '操作IP';
COMMENT ON COLUMN public.t_sys_sql_log.remark IS '备注';
COMMENT ON COLUMN public.t_sys_sql_log.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_sql_log.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_sql_log.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_sql_log.update_time IS '更新时间';

COMMENT ON COLUMN public.t_sys_script_log.id IS 'ID';
COMMENT ON COLUMN public.t_sys_script_log.script_id IS '关联脚本ID';
COMMENT ON COLUMN public.t_sys_script_log.script_name IS '脚本名称快照';
COMMENT ON COLUMN public.t_sys_script_log.script_content IS '实际执行脚本快照';
COMMENT ON COLUMN public.t_sys_script_log.transaction_mode IS '事务模式';
COMMENT ON COLUMN public.t_sys_script_log.execute_status IS '执行状态';
COMMENT ON COLUMN public.t_sys_script_log.execute_duration IS '执行耗时（毫秒）';
COMMENT ON COLUMN public.t_sys_script_log.transaction_result IS '事务结果';
COMMENT ON COLUMN public.t_sys_script_log.output IS '截断后的执行输出';
COMMENT ON COLUMN public.t_sys_script_log.error_message IS '错误信息';
COMMENT ON COLUMN public.t_sys_script_log.create_name IS '执行人';
COMMENT ON COLUMN public.t_sys_script_log.create_ip IS '执行IP';
COMMENT ON COLUMN public.t_sys_script_log.create_time IS '执行时间';

COMMENT ON COLUMN public.t_sys_job_log.id IS 'ID';
COMMENT ON COLUMN public.t_sys_job_log.job_id IS '任务ID';
COMMENT ON COLUMN public.t_sys_job_log.job_name IS '任务名称';
COMMENT ON COLUMN public.t_sys_job_log.job_group IS '任务组';
COMMENT ON COLUMN public.t_sys_job_log.start_time IS '开始时间';
COMMENT ON COLUMN public.t_sys_job_log.end_time IS '结束时间';
COMMENT ON COLUMN public.t_sys_job_log.duration_ms IS '耗时(ms)';
COMMENT ON COLUMN public.t_sys_job_log.status IS '状态';
COMMENT ON COLUMN public.t_sys_job_log.error_message IS '错误信息';
COMMENT ON COLUMN public.t_sys_job_log.trace_id IS '链路追踪ID';
COMMENT ON COLUMN public.t_sys_job_log.instance_id IS '实际执行应用实例（Quartz instanceId）';
COMMENT ON COLUMN public.t_sys_job_log.fire_instance_id IS 'Quartz本次触发实例ID';
COMMENT ON COLUMN public.t_sys_job_log.create_time IS '创建时间';

-- 历史父表逐列复制在线父表备注；历史表创建时在线列备注尚未声明，不能依赖 LIKE 自动继承。
DO $$
DECLARE
    table_pair text[];
    column_record record;
BEGIN
    FOREACH table_pair SLICE 1 IN ARRAY ARRAY[
        ARRAY['t_sys_login_log', 't_sys_login_log_history'],
        ARRAY['t_sys_operate_log', 't_sys_operate_log_history'],
        ARRAY['t_sys_sql_log', 't_sys_sql_log_history'],
        ARRAY['t_sys_script_log', 't_sys_script_log_history'],
        ARRAY['t_sys_job_log', 't_sys_job_log_history']
    ] LOOP
        FOR column_record IN
            SELECT attribute.attname AS column_name,
                   col_description(attribute.attrelid, attribute.attnum) AS column_comment
            FROM pg_catalog.pg_attribute attribute
            WHERE attribute.attrelid = ('public.' || table_pair[1])::regclass
              AND attribute.attnum > 0
              AND NOT attribute.attisdropped
        LOOP
            EXECUTE format('COMMENT ON COLUMN public.%I.%I IS %L',
                           table_pair[2], column_record.column_name, column_record.column_comment);
        END LOOP;
    END LOOP;
END $$;

-- 历史父表继承列备注后，建立与在线查询和 SQL 控制台排障相匹配的父级索引。
CREATE INDEX idx_sys_login_log_id ON public.t_sys_login_log (id);
CREATE INDEX idx_sys_login_log_user_time ON public.t_sys_login_log (user_id, create_time DESC);
CREATE INDEX idx_sys_login_log_name_time ON public.t_sys_login_log (username, create_time DESC);
CREATE INDEX idx_sys_login_log_result_time ON public.t_sys_login_log (success, event_type, create_time DESC, id DESC);
CREATE INDEX idx_sys_login_log_trace_id ON public.t_sys_login_log (trace_id);

CREATE INDEX idx_sys_operate_log_id ON public.t_sys_operate_log (id);
CREATE INDEX idx_sys_operate_log_user_time ON public.t_sys_operate_log (user_id, create_time DESC);
CREATE INDEX idx_sys_operate_log_result_time ON public.t_sys_operate_log (success, create_time DESC, id DESC);
CREATE INDEX idx_sys_operate_log_trace_id ON public.t_sys_operate_log (trace_id);

CREATE INDEX idx_sys_sql_log_id ON public.t_sys_sql_log (id DESC);
CREATE INDEX idx_sys_sql_log_result_time ON public.t_sys_sql_log (result_type, create_time DESC, id DESC);

CREATE INDEX idx_sys_script_log_id ON public.t_sys_script_log (id DESC);
CREATE INDEX idx_sys_script_log_status_time ON public.t_sys_script_log
    (execute_status, transaction_mode, create_time DESC, id DESC);

CREATE INDEX idx_sys_job_log_id ON public.t_sys_job_log (id);
CREATE INDEX idx_sys_job_log_job_start ON public.t_sys_job_log (job_id, start_time DESC, id DESC);
CREATE INDEX idx_sys_job_log_status_start ON public.t_sys_job_log (status, start_time DESC, id DESC);
CREATE INDEX idx_sys_job_log_trace_id ON public.t_sys_job_log (trace_id);
CREATE INDEX idx_sys_job_log_fire_instance_id ON public.t_sys_job_log (fire_instance_id);

CREATE INDEX idx_sys_login_log_history_id ON public.t_sys_login_log_history (id);
CREATE INDEX idx_sys_login_log_history_trace_id ON public.t_sys_login_log_history (trace_id);
CREATE INDEX idx_sys_operate_log_history_id ON public.t_sys_operate_log_history (id);
CREATE INDEX idx_sys_operate_log_history_trace_id ON public.t_sys_operate_log_history (trace_id);
CREATE INDEX idx_sys_sql_log_history_id ON public.t_sys_sql_log_history (id DESC);
CREATE INDEX idx_sys_script_log_history_id ON public.t_sys_script_log_history (id DESC);
CREATE INDEX idx_sys_job_log_history_id ON public.t_sys_job_log_history (id);
CREATE INDEX idx_sys_job_log_history_trace_id ON public.t_sys_job_log_history (trace_id);

-- 分区范围固定写入版本化迁移，避免不同安装时间产生不可复现的数据库结构。
DO $$
DECLARE
    partition_month date := DATE '2026-01-01';
    partition_end date;
    partition_suffix text;
    parent_table text;
BEGIN
    WHILE partition_month < DATE '2030-01-01' LOOP
        partition_end := (partition_month + INTERVAL '1 month')::date;
        partition_suffix := to_char(partition_month, 'YYYYMM');
        FOREACH parent_table IN ARRAY ARRAY[
            't_sys_login_log',
            't_sys_operate_log',
            't_sys_sql_log',
            't_sys_script_log'
        ] LOOP
            EXECUTE format(
                'CREATE TABLE public.%I_p%s PARTITION OF public.%I FOR VALUES FROM (%L) TO (%L)',
                parent_table, partition_suffix, parent_table, partition_month, partition_end);
        END LOOP;
        EXECUTE format(
            'CREATE TABLE public.t_sys_job_log_p%s PARTITION OF public.t_sys_job_log FOR VALUES FROM (%L) TO (%L)',
            partition_suffix, partition_month, partition_end);
        partition_month := partition_end;
    END LOOP;
END $$;

CREATE TABLE public.t_sys_login_log_default PARTITION OF public.t_sys_login_log DEFAULT;
CREATE TABLE public.t_sys_operate_log_default PARTITION OF public.t_sys_operate_log DEFAULT;
CREATE TABLE public.t_sys_sql_log_default PARTITION OF public.t_sys_sql_log DEFAULT;
CREATE TABLE public.t_sys_script_log_default PARTITION OF public.t_sys_script_log DEFAULT;
CREATE TABLE public.t_sys_job_log_default PARTITION OF public.t_sys_job_log DEFAULT;

-- 两个任务共用互斥键，避免转储和淘汰同时修改同一套分区元数据；初始暂停，由管理员确认后恢复。
INSERT INTO public.t_sys_job
    (id, number, job_name, job_group, job_class_name, cron_expression, job_data,
     mutex_key, status, is_system, remark, create_time, version)
VALUES
    (440000000000000001, 'SYSTEM_LOG_ARCHIVE', '系统日志分区转储', 'SYSTEM',
     'sm.domain.sys.scheduler.job.ArchiveSystemLogJob', '0 10 2 * * ?',
     '{"loginLogHotDays":180,"operateLogHotDays":180,"sqlLogHotDays":180,"scriptLogHotDays":180,"jobLogHotDays":90,"maxPartitionsPerRun":12}',
     'system-log-lifecycle', 'PAUSED', true,
     '参数说明：loginLogHotDays=登录日志在线保留天数；operateLogHotDays=操作日志在线保留天数；sqlLogHotDays=SQL执行日志在线保留天数；scriptLogHotDays=脚本执行日志在线保留天数；jobLogHotDays=调度实例在线保留天数；maxPartitionsPerRun=单次最多转储的月分区总数。所有参数必须为整数，只转储超过保留期的完整自然月。',
     CURRENT_TIMESTAMP, 0),
    (440000000000000002, 'SYSTEM_LOG_HISTORY_PURGE', '系统日志历史淘汰', 'SYSTEM',
     'sm.domain.sys.scheduler.job.PurgeSystemLogHistoryJob', '0 40 2 * * ?',
     '{"loginLogRetentionDays":1095,"operateLogRetentionDays":1095,"sqlLogRetentionDays":730,"scriptLogRetentionDays":730,"jobLogRetentionDays":365,"maxPartitionsPerRun":12}',
     'system-log-lifecycle', 'PAUSED', true,
     '参数说明：loginLogRetentionDays=登录日志历史保留天数；operateLogRetentionDays=操作日志历史保留天数；sqlLogRetentionDays=SQL执行日志历史保留天数；scriptLogRetentionDays=脚本执行日志历史保留天数；jobLogRetentionDays=调度实例历史保留天数；maxPartitionsPerRun=单次最多删除的月分区总数。所有参数必须为整数，只删除超过保留期的完整自然月。',
     CURRENT_TIMESTAMP, 0);

ALTER TABLE public.t_sys_script ADD COLUMN version integer NOT NULL DEFAULT 0;
ALTER TABLE public.t_sys_script ADD CONSTRAINT uk_sys_script_number UNIQUE (number);
COMMENT ON COLUMN public.t_sys_script.version IS '乐观锁版本号';

CREATE TABLE public.t_sys_script_log (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer NOT NULL DEFAULT 0,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT t_sys_script_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_script_log_create_time ON public.t_sys_script_log (create_time DESC);
CREATE INDEX idx_sys_script_log_status ON public.t_sys_script_log (execute_status);
COMMENT ON TABLE public.t_sys_script_log IS '脚本控制台执行审计';
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

INSERT INTO public.t_sys_param
    (id, number, name, value, remark, app_id, is_system, create_time, version)
VALUES
    (426000000000000010, 'SCRIPT_CONSOLE_TIMEOUT_SECONDS', '脚本控制台超时秒数', '30',
     '允许范围 1～300 秒；超时将取消 JavaScript 并回滚原子事务', 30, true, CURRENT_TIMESTAMP, 0),
    (426000000000000011, 'SCRIPT_CONSOLE_MAX_SOURCE_LENGTH', '脚本控制台最大源码长度', '100000',
     '允许范围 1000～1000000 字符', 30, true, CURRENT_TIMESTAMP, 0),
    (426000000000000012, 'SCRIPT_CONSOLE_MAX_OUTPUT_LENGTH', '脚本控制台最大输出长度', '100000',
     '允许范围 1000～1000000 字符；超过限制的输出将被截断', 30, true, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_permission SET name = '脚本控制台-执行', update_time = CURRENT_TIMESTAMP, version = version + 1 WHERE number = 'sys:monitor:script:execute';
UPDATE public.t_sys_permission SET name = '脚本管理-列表', update_time = CURRENT_TIMESTAMP, version = version + 1 WHERE number = 'sys:monitor:script:listPage';
UPDATE public.t_sys_permission SET name = '脚本管理-详情', update_time = CURRENT_TIMESTAMP, version = version + 1 WHERE number = 'sys:monitor:script:detail';
UPDATE public.t_sys_permission SET name = '脚本管理-保存', update_time = CURRENT_TIMESTAMP, version = version + 1 WHERE number = 'sys:monitor:script:save';
UPDATE public.t_sys_permission SET name = '脚本管理-删除', update_time = CURRENT_TIMESTAMP, version = version + 1 WHERE number = 'sys:monitor:script:delete';

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, version)
VALUES
    (426000000000000001, '脚本执行历史-列表', 'sys:monitor:script:log:listPage', 30, CURRENT_TIMESTAMP, 0),
    (426000000000000002, '脚本执行历史-详情', 'sys:monitor:script:log:detail', 30, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_menu
SET name = '脚本控制台', component = 'sys/monitor/script-console', path = '/sys/monitor/script-console',
    permission_id = 419000000000000001, description = '执行受控的服务端 JavaScript 运维脚本',
    sort = 1, update_time = CURRENT_TIMESTAMP
WHERE id = 419000000000000011;

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, description,
     sort, enabled, create_time)
VALUES
    (426000000000000011, 'script_manage_page', '脚本管理', 3, 419000000000000010, 30,
     419000000000000002, '/sys/monitor/script-manage', 'sys/monitor/script-manage',
     '维护可复用的运维脚本', 2, true, CURRENT_TIMESTAMP),
    (426000000000000012, 'script_log_page', '执行历史', 3, 419000000000000010, 30,
     426000000000000001, '/sys/monitor/script-log', 'sys/monitor/script-log',
     '查看脚本控制台执行审计', 3, true, CURRENT_TIMESTAMP);

WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), script_permissions AS (
    SELECT id FROM public.t_sys_permission WHERE number LIKE 'sys:monitor:script:%'
), missing_grants AS (
    SELECT administrator_role.id AS role_id, script_permissions.id AS permission_id
    FROM administrator_role CROSS JOIN script_permissions
    WHERE NOT EXISTS (
        SELECT 1 FROM public.t_sys_role_perms existing_grant
        WHERE existing_grant.role_id = administrator_role.id
          AND existing_grant.permission_id = script_permissions.id
    )
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY missing_grants.permission_id),
       missing_grants.role_id, missing_grants.permission_id, CURRENT_TIMESTAMP
FROM missing_grants CROSS JOIN current_max;

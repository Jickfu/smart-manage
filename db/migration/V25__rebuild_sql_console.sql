-- 将旧 SQL 控制台权限收敛为与公开接口一致的细粒度权限。
UPDATE public.t_sys_permission
SET name = 'SQL控制台-执行', number = 'sys:monitor:sql:execute',
    update_time = CURRENT_TIMESTAMP, version = version + 1
WHERE number = 'sys:monitor:sql';

UPDATE public.t_sys_permission
SET name = 'SQL执行历史-列表', number = 'sys:monitor:sql:log:listPage',
    update_time = CURRENT_TIMESTAMP, version = version + 1
WHERE number = 'sys:monitor:sql-log:listPage';

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, version)
VALUES (425000000000000001, 'SQL执行历史-详情', 'sys:monitor:sql:log:detail', 30, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_menu
SET component = 'sys/monitor/sql-console', path = '/sys/monitor/sql-console',
    description = '执行 PostgreSQL 查询、单条命令或批量 INSERT', update_time = CURRENT_TIMESTAMP
WHERE id = 413501707400000002;

UPDATE public.t_sys_menu
SET component = 'sys/monitor/sql-log', description = '查看 SQL 控制台执行审计',
    update_time = CURRENT_TIMESTAMP
WHERE id = 413501707400000004;

INSERT INTO public.t_sys_param
    (id, number, name, value, remark, app_id, is_system, create_time, version)
VALUES
    (425000000000000010, 'SQL_CONSOLE_MAX_ROWS', 'SQL 控制台最大返回行数', '1000',
     '允许范围 1～5000；超过限制的查询结果会被截断', 30, true, CURRENT_TIMESTAMP, 0);

WITH granted_roles AS (
    SELECT DISTINCT role_permission.role_id
    FROM public.t_sys_role_perms role_permission
    JOIN public.t_sys_permission permission ON permission.id = role_permission.permission_id
    WHERE permission.number = 'sys:monitor:sql:log:listPage'
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY granted_roles.role_id),
       granted_roles.role_id, 425000000000000001, CURRENT_TIMESTAMP
FROM granted_roles CROSS JOIN current_max;

WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), sql_permissions AS (
    SELECT id FROM public.t_sys_permission WHERE number LIKE 'sys:monitor:sql:%'
), missing_grants AS (
    SELECT administrator_role.id AS role_id, sql_permissions.id AS permission_id
    FROM administrator_role CROSS JOIN sql_permissions
    WHERE NOT EXISTS (
        SELECT 1 FROM public.t_sys_role_perms existing_grant
        WHERE existing_grant.role_id = administrator_role.id
          AND existing_grant.permission_id = sql_permissions.id
    )
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY missing_grants.permission_id),
       missing_grants.role_id, missing_grants.permission_id, CURRENT_TIMESTAMP
FROM missing_grants CROSS JOIN current_max;

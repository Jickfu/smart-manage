-- 将旧的模糊“保存”能力收敛为明确的应用缓存清理能力。
UPDATE public.t_sys_permission
SET name = '应用缓存-清理',
    number = 'sys:monitor:cache:clear',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE number = 'sys:monitor:cache:save';

INSERT INTO public.t_sys_permission
    (id, name, number, app_id, create_time, version)
VALUES
    (421000000000000001, '应用缓存-全部清理', 'sys:monitor:cache:clearAll', 30, CURRENT_TIMESTAMP, 0),
    (421000000000000002, 'Redis管理-查询', 'sys:monitor:redis:listPage', 30, CURRENT_TIMESTAMP, 0),
    (421000000000000003, 'Redis管理-查看值', 'sys:monitor:redis:value', 30, CURRENT_TIMESTAMP, 0),
    (421000000000000004, 'Redis管理-删除', 'sys:monitor:redis:delete', 30, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_menu
SET name = '缓存与 Redis',
    update_time = CURRENT_TIMESTAMP
WHERE id = 50080;

UPDATE public.t_sys_menu
SET name = '应用缓存',
    update_time = CURRENT_TIMESTAMP
WHERE id = 50081;

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon,
     description, sort, enabled, create_time, update_time)
VALUES
    (421000000000000010, 'REDIS_PAGE', 'Redis 管理', 3, 50080, 30, 421000000000000002,
     '/sys/monitor/redis', 'sys/monitor/redis', 'DatabaseOutlined',
     'Redis 运行状态与 Key 运维', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 既有缓存清理角色继续具备“全部清理”能力。
WITH granted_roles AS (
    SELECT DISTINCT role_permission.role_id
    FROM public.t_sys_role_perms role_permission
    JOIN public.t_sys_permission permission ON permission.id = role_permission.permission_id
    WHERE permission.number = 'sys:monitor:cache:clear'
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY granted_roles.role_id),
       granted_roles.role_id, 421000000000000001, CURRENT_TIMESTAMP
FROM granted_roles CROSS JOIN current_max;

-- Redis 任意 Key 能力只授予 administrator 角色，Service 仍会再次校验管理员身份。
WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), redis_permissions AS (
    SELECT id FROM public.t_sys_permission WHERE number LIKE 'sys:monitor:redis:%'
), missing_grants AS (
    SELECT administrator_role.id AS role_id, redis_permissions.id AS permission_id
    FROM administrator_role CROSS JOIN redis_permissions
    WHERE NOT EXISTS (
        SELECT 1 FROM public.t_sys_role_perms existing_grant
        WHERE existing_grant.role_id = administrator_role.id
          AND existing_grant.permission_id = redis_permissions.id
    )
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY missing_grants.permission_id),
       missing_grants.role_id, missing_grants.permission_id, CURRENT_TIMESTAMP
FROM missing_grants CROSS JOIN current_max;

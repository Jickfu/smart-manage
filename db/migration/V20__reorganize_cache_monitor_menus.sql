-- 缓存监控按用户任务拆分为只读状态页和统一操作页，不再暴露底层实现维度。
INSERT INTO public.t_sys_permission
    (id, name, number, app_id, create_time, version)
VALUES
    (421000000000000005, '缓存管理-查看值', 'sys:monitor:cache:value', 30, CURRENT_TIMESTAMP, 0),
    (421000000000000006, '缓存管理-删除', 'sys:monitor:cache:delete', 30, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_menu
SET name = '缓存监控',
    update_time = CURRENT_TIMESTAMP
WHERE id = 50080;

UPDATE public.t_sys_menu
SET name = '缓存状态',
    path = '/sys/monitor/cache-status',
    component = 'sys/monitor/cache-status',
    description = 'Redis 运行状态与 JetCache 实时统计',
    sort = 1,
    update_time = CURRENT_TIMESTAMP
WHERE id = 50081;

UPDATE public.t_sys_menu
SET name = '缓存管理',
    permission_id = (SELECT id FROM public.t_sys_permission WHERE number = 'sys:monitor:cache:listPage'),
    path = '/sys/monitor/cache-management',
    component = 'sys/monitor/cache-management',
    icon = 'DatabaseOutlined',
    description = '统一查看和操作本地与 Redis 缓存',
    sort = 2,
    update_time = CURRENT_TIMESTAMP
WHERE id = 421000000000000010;

-- 任意缓存值和删除能力仅授予 administrator，Service 仍执行管理员身份复核。
WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), cache_operation_permissions AS (
    SELECT id FROM public.t_sys_permission
    WHERE number IN ('sys:monitor:cache:value', 'sys:monitor:cache:delete')
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY cache_operation_permissions.id),
       administrator_role.id, cache_operation_permissions.id, CURRENT_TIMESTAMP
FROM administrator_role CROSS JOIN cache_operation_permissions CROSS JOIN current_max;

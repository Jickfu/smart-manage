-- 缓存管理已经统一使用 sys:monitor:cache:* 权限，移除不再对应公开接口的 Redis 独立权限。
DELETE FROM public.t_sys_role_perms
WHERE permission_id IN (
    SELECT id
    FROM public.t_sys_permission
    WHERE number IN (
        'sys:monitor:redis:listPage',
        'sys:monitor:redis:value',
        'sys:monitor:redis:delete'
    )
);

DELETE FROM public.t_sys_permission
WHERE number IN (
    'sys:monitor:redis:listPage',
    'sys:monitor:redis:value',
    'sys:monitor:redis:delete'
);

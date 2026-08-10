-- 文件配置和界面配置已经收敛为单例详情/保存接口，不再提供列表删除能力。
DELETE FROM public.t_sys_role_perms
WHERE permission_id IN (
    SELECT id
    FROM public.t_sys_permission
    WHERE number IN (
        'sys:base:file-config:delete',
        'sys:base:ui-config:delete'
    )
);

DELETE FROM public.t_sys_permission
WHERE number IN (
    'sys:base:file-config:delete',
    'sys:base:ui-config:delete'
);

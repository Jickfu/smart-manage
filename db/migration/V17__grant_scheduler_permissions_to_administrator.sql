-- 旧基线未给管理员角色分配任务权限；独立应用启用时补齐完整能力。
WITH administrator_role AS (
    SELECT id
    FROM public.t_sys_role
    WHERE number = 'admin'
),
scheduler_permissions AS (
    SELECT id
    FROM public.t_sys_permission
    WHERE app_id = 32
),
missing_grants AS (
    SELECT
        administrator_role.id AS role_id,
        scheduler_permissions.id AS permission_id
    FROM administrator_role
    CROSS JOIN scheduler_permissions
    WHERE NOT EXISTS (
        SELECT 1
        FROM public.t_sys_role_perms existing_grant
        WHERE existing_grant.role_id = administrator_role.id
          AND existing_grant.permission_id = scheduler_permissions.id
    )
),
current_max AS (
    SELECT COALESCE(max(id), 0) AS id
    FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms
    (id, role_id, permission_id, create_time)
SELECT
    current_max.id + row_number() OVER (ORDER BY missing_grants.permission_id),
    missing_grants.role_id,
    missing_grants.permission_id,
    CURRENT_TIMESTAMP
FROM missing_grants
CROSS JOIN current_max;

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, version)
VALUES
    (420000000000001017, '云管理-选择', 'sys:base:cloud:select', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001018, '菜单管理-删除', 'sys:base:menu:delete', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001019, '角色管理-选择', 'sys:base:role:select', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001020, '角色管理-删除', 'sys:base:role:delete', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001021, '用户管理-详情', 'sys:base:user:detail', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001022, '用户管理-保存', 'sys:base:user:save', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001023, '用户管理-删除', 'sys:base:user:delete', 31, CURRENT_TIMESTAMP, 0);

WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), missing_permissions AS (
    SELECT id FROM public.t_sys_permission WHERE id BETWEEN 420000000000001017 AND 420000000000001023
), missing_grants AS (
    SELECT administrator_role.id AS role_id, missing_permissions.id AS permission_id
    FROM administrator_role CROSS JOIN missing_permissions
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY missing_grants.permission_id),
       missing_grants.role_id, missing_grants.permission_id, CURRENT_TIMESTAMP
FROM missing_grants CROSS JOIN current_max;

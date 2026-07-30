-- 日志不得保存任何令牌内容，包括可关联会话的首尾片段。
ALTER TABLE public.t_sys_login_log
    DROP COLUMN token_hint;

INSERT INTO public.t_sys_permission
    (id, name, number, app_id, create_time, version)
VALUES
    (420000000000001014, '登录日志-详情', 'sys:log:login:detail', 30, CURRENT_TIMESTAMP, 0),
    (420000000000001015, '操作日志-详情', 'sys:log:operate:detail', 30, CURRENT_TIMESTAMP, 0);

-- 既有列表查看角色继续具备详情能力，避免升级后出现列表可见但详情被拒绝。
WITH granted_roles AS (
    SELECT DISTINCT list_role_permission.role_id
    FROM public.t_sys_role_perms list_role_permission
    JOIN public.t_sys_permission list_permission
        ON list_permission.id = list_role_permission.permission_id
    WHERE list_permission.number = 'sys:log:login:listPage'
),
current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms
    (id, role_id, permission_id, create_time)
SELECT
    current_max.id + row_number() OVER (ORDER BY granted_roles.role_id),
    granted_roles.role_id,
    detail_permission.id,
    CURRENT_TIMESTAMP
FROM granted_roles
CROSS JOIN current_max
JOIN public.t_sys_permission detail_permission
    ON detail_permission.number = 'sys:log:login:detail';

WITH granted_roles AS (
    SELECT DISTINCT list_role_permission.role_id
    FROM public.t_sys_role_perms list_role_permission
    JOIN public.t_sys_permission list_permission
        ON list_permission.id = list_role_permission.permission_id
    WHERE list_permission.number = 'sys:log:operate:listPage'
),
current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms
    (id, role_id, permission_id, create_time)
SELECT
    current_max.id + row_number() OVER (ORDER BY granted_roles.role_id),
    granted_roles.role_id,
    detail_permission.id,
    CURRENT_TIMESTAMP
FROM granted_roles
CROSS JOIN current_max
JOIN public.t_sys_permission detail_permission
    ON detail_permission.number = 'sys:log:operate:detail';

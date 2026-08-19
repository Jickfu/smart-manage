-- 菜单树服务要求所有页面菜单必须挂在同应用分组下；为页面较少的应用恢复精简职责分组。

INSERT INTO public.t_sys_permission (id, name, number, feature_id, app_id, version)
VALUES (470000000000000030, '采购管理-应用入口', 'scm:procurement:access', NULL,
        430000000000000002, 0);

INSERT INTO public.t_sys_role_perms (id, role_id, permission_id)
SELECT 470000000000000100 + row_number() OVER (ORDER BY role_permission.role_id),
       role_permission.role_id,
       470000000000000030
FROM public.t_sys_role_perms role_permission
WHERE role_permission.permission_id = 430000000000000010;

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, feature_id, permission_id, path, component,
     icon, description, sort, enabled, create_time, update_time, version)
VALUES
    (470000000000000021, 'scheduler_operations', '调度管理', 0, 0, 32, NULL, 5001,
     NULL, NULL, 'ClockCircleOutlined', '定时任务定义与执行记录', 10, true, now(), now(), 0),
    (470000000000000031, 'procurement_operations', '采购业务', 0, 0, 430000000000000002,
     NULL, 470000000000000030, NULL, NULL, 'ShoppingOutlined', '采购业务单据', 10, true,
     now(), now(), 0);

UPDATE public.t_sys_menu
SET parent_id = 470000000000000021,
    update_time = now(),
    version = version + 1
WHERE id IN (413260828563165184, 413260828571553792);

UPDATE public.t_sys_menu
SET parent_id = 470000000000000031,
    update_time = now(),
    version = version + 1
WHERE id = 430000000000000020;

-- 页面菜单允许直接位于应用根级，移除仅为满足旧层级约束而恢复的调度和采购分组。

UPDATE public.t_sys_menu
SET parent_id = 0,
    update_time = now(),
    version = version + 1
WHERE id IN (413260828563165184, 413260828571553792, 430000000000000020);

DELETE FROM public.t_sys_menu
WHERE id IN (470000000000000021, 470000000000000031);

DELETE FROM public.t_sys_role_perms
WHERE permission_id IN (5001, 470000000000000030);

DELETE FROM public.t_sys_permission
WHERE id IN (5001, 470000000000000030);

-- 节点监控收敛为生产安全的当前实例运行快照，沿用权限 ID 以保留既有角色授权。
UPDATE public.t_sys_permission
SET name = '运行监控-查看',
    number = 'sys:monitor:node:view',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707269836800;

UPDATE public.t_sys_menu
SET name = '运行监控',
    description = '查看当前请求命中的应用实例运行快照',
    permission_id = 413501707269836800,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707345334272;

-- 单数据源连接池摘要已合并到运行监控，不再保留没有前端实现的 Druid 菜单。
DELETE FROM public.t_sys_menu
WHERE id = 413501707362111488;

DELETE FROM public.t_sys_role_perms
WHERE permission_id = 413501707311779840;

DELETE FROM public.t_sys_permission
WHERE id = 413501707311779840;

-- Arthas 页面访问和诊断执行分权，页面与诊断分组不再复用节点监控权限。
INSERT INTO public.t_sys_permission
    (id, name, number, app_id, create_time, version)
VALUES
    (438000000000000001, 'Arthas诊断-访问', 'sys:monitor:arthas:access', 30, CURRENT_TIMESTAMP, 0);

UPDATE public.t_sys_permission
SET name = 'Arthas诊断-执行',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707320168448;

UPDATE public.t_sys_menu
SET name = 'Arthas 诊断',
    description = '执行当前应用实例的受控 Arthas 诊断动作',
    permission_id = 438000000000000001,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707391471616;

UPDATE public.t_sys_menu
SET permission_id = 438000000000000001,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707370500096;

INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + 1, administrator_role.id, 438000000000000001, CURRENT_TIMESTAMP
FROM (SELECT COALESCE(MAX(id), 0) AS id FROM public.t_sys_role_perms) current_max
JOIN public.t_sys_role administrator_role ON administrator_role.number = 'administrator'
WHERE NOT EXISTS (
    SELECT 1
    FROM public.t_sys_role_perms existing_grant
    WHERE existing_grant.role_id = administrator_role.id
      AND existing_grant.permission_id = 438000000000000001
);

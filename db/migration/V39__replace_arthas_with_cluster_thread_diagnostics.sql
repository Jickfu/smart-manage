-- 运行监控支持从 Redis 在线实例注册表选择目标节点。
UPDATE public.t_sys_menu
SET description = '选择在线应用实例并查看运行快照',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707345334272;

-- 项目不再暴露 Arthas 能力，沿用既有权限和菜单 ID 保留管理员角色授权。
UPDATE public.t_sys_permission
SET name = '线程诊断-访问',
    number = 'sys:monitor:thread:access',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 438000000000000001;

UPDATE public.t_sys_permission
SET name = '线程诊断-采集',
    number = 'sys:monitor:thread:collect',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707320168448;

UPDATE public.t_sys_menu
SET number = 'thread_diagnostic',
    name = '线程诊断',
    path = '/sys/monitor/thread',
    component = 'sys/monitor/thread',
    description = '选择在线实例并查看线程、堆栈、热点和死锁信息',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707391471616;

UPDATE public.t_sys_menu
SET name = '诊断工具',
    description = '面向超级管理员的生产问题诊断能力',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 413501707370500096;

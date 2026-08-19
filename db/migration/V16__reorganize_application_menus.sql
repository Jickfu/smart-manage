-- 统一应用与菜单展示结构：稳定应用编码、Feature、组件键和权限码保持不变。

UPDATE public.t_sys_app
SET name = '系统管理',
    description = '组织、权限、资料、平台结构与系统配置',
    update_time = now(),
    version = version + 1
WHERE number = 'base';

UPDATE public.t_sys_app
SET name = '运维中心',
    icon = 'DashboardOutlined',
    icon_color = '#fa8c16',
    description = '运行监控、审计日志、诊断与高风险运维工具',
    update_time = now(),
    version = version + 1
WHERE number = 'monitor';

UPDATE public.t_sys_permission
SET name = CASE number
        WHEN 'sys:base:access' THEN '系统管理-应用入口'
        WHEN 'sys:log:access' THEN '运维中心-应用入口'
        WHEN 'sys:scheduler:category' THEN '任务调度-应用入口'
        ELSE name
    END,
    update_time = now(),
    version = version + 1
WHERE number IN ('sys:base:access', 'sys:log:access', 'sys:scheduler:category');

UPDATE public.t_sys_feature
SET default_name = CASE feature_key
        WHEN 'sys/base/permission' THEN '权限定义'
        WHEN 'sys/base/basic-data' THEN '基础资料'
        WHEN 'sys/base/file-config' THEN '存储配置'
        WHEN 'sys/monitor/node' THEN '服务状态'
        WHEN 'sys/monitor/slow-sql' THEN '慢 SQL 分析'
        WHEN 'sys/scheduler/execution' THEN '执行记录'
        ELSE default_name
    END,
    update_time = now(),
    version = version + 1
WHERE feature_key IN (
    'sys/base/permission',
    'sys/base/basic-data',
    'sys/base/file-config',
    'sys/monitor/node',
    'sys/monitor/slow-sql',
    'sys/scheduler/execution'
);

-- 系统管理按管理员任务场景建立五个应用级分组。
INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, feature_id, permission_id, path, component,
     icon, description, sort, enabled, create_time, update_time, version)
VALUES
    (470000000000000001, 'organization_and_user', '组织与用户', 0, 0, 31, NULL, 10030,
     NULL, NULL, 'TeamOutlined', '组织架构与用户账号', 10, true, now(), now(), 0),
    (470000000000000002, 'role_and_permission', '角色与权限', 0, 0, 31, NULL, 10030,
     NULL, NULL, 'SafetyOutlined', '角色、权限定义与授权关系', 20, true, now(), now(), 0),
    (470000000000000003, 'data_and_numbering', '资料与编号', 0, 0, 31, NULL, 10030,
     NULL, NULL, 'ProfileOutlined', '通用基础资料与编号规则', 30, true, now(), now(), 0),
    (470000000000000004, 'platform_structure', '平台结构', 0, 0, 31, NULL, 10030,
     NULL, NULL, 'ClusterOutlined', '云、应用、功能与菜单结构', 40, true, now(), now(), 0),
    (470000000000000005, 'system_configuration', '系统配置', 0, 0, 31, NULL, 10030,
     NULL, NULL, 'SettingOutlined', '系统级参数、界面、存储与附件配置', 50, true, now(), now(), 0);

UPDATE public.t_sys_menu target
SET parent_id = source.parent_id,
    name = source.name,
    icon = source.icon,
    sort = source.sort,
    update_time = now(),
    version = target.version + 1
FROM (VALUES
    (450000000000000010::bigint, 470000000000000001::bigint, '组织管理', 'ApartmentOutlined', 10),
    (2102::bigint,               470000000000000001::bigint, '用户管理', 'UserOutlined', 20),
    (2105::bigint,               470000000000000002::bigint, '角色管理', 'IdcardOutlined', 10),
    (2104::bigint,               470000000000000002::bigint, '权限定义', 'SafetyOutlined', 20),
    (411644663089963008::bigint, 470000000000000003::bigint, '基础资料', 'ProfileOutlined', 10),
    (460000000000000020::bigint, 470000000000000003::bigint, '编号规则', 'FieldNumberOutlined', 20),
    (3102::bigint,               470000000000000004::bigint, '云管理', 'CloudOutlined', 10),
    (3103::bigint,               470000000000000004::bigint, '应用管理', 'AppstoreOutlined', 20),
    (450000000000000110::bigint, 470000000000000004::bigint, '功能管理', 'ClusterOutlined', 30),
    (2103::bigint,               470000000000000004::bigint, '菜单管理', 'MenuOutlined', 40),
    (50061::bigint,              470000000000000005::bigint, '系统参数', 'FormOutlined', 10),
    (413172783545511936::bigint, 470000000000000005::bigint, '界面配置', 'MonitorOutlined', 20),
    (413196675798462464::bigint, 470000000000000005::bigint, '存储配置', 'DatabaseOutlined', 30),
    (420000000000001104::bigint, 470000000000000005::bigint, '附件配置', 'PaperClipOutlined', 40)
) AS source(id, parent_id, name, icon, sort)
WHERE target.id = source.id;

-- 运维中心按只读监控、审计、诊断、数据运维和脚本运维重新分组。
INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, feature_id, permission_id, path, component,
     icon, description, sort, enabled, create_time, update_time, version)
VALUES
    (470000000000000011, 'runtime_monitoring', '运行监控', 0, 0, 30, NULL, 10020,
     NULL, NULL, 'DashboardOutlined', '应用实例与缓存运行状态', 10, true, now(), now(), 0),
    (470000000000000012, 'audit_logs', '审计日志', 0, 0, 30, NULL, 10020,
     NULL, NULL, 'AuditOutlined', '登录与业务操作审计记录', 20, true, now(), now(), 0),
    (470000000000000013, 'diagnostic_analysis', '诊断分析', 0, 0, 30, NULL, 10020,
     NULL, NULL, 'ToolOutlined', '慢 SQL 与线程运行诊断', 30, true, now(), now(), 0),
    (470000000000000014, 'data_operations', '数据运维', 0, 0, 30, NULL, 10020,
     NULL, NULL, 'DatabaseOutlined', '缓存与数据库高风险运维能力', 40, true, now(), now(), 0),
    (470000000000000015, 'script_operations', '脚本运维', 0, 0, 30, NULL, 10020,
     NULL, NULL, 'CodeOutlined', '受控运维脚本及执行审计', 50, true, now(), now(), 0);

UPDATE public.t_sys_menu target
SET parent_id = source.parent_id,
    name = source.name,
    icon = source.icon,
    sort = source.sort,
    update_time = now(),
    version = target.version + 1
FROM (VALUES
    (413501707345334272::bigint, 470000000000000011::bigint, '服务状态', 'DashboardOutlined', 10),
    (50081::bigint,              470000000000000011::bigint, '缓存状态', 'LineChartOutlined', 20),
    (3002::bigint,               470000000000000012::bigint, '登录日志', 'FileTextOutlined', 10),
    (3003::bigint,               470000000000000012::bigint, '操作日志', 'AuditOutlined', 20),
    (441000000000000010::bigint, 470000000000000013::bigint, '慢 SQL 分析', 'DatabaseOutlined', 10),
    (413501707391471616::bigint, 470000000000000013::bigint, '线程诊断', 'ToolOutlined', 20),
    (421000000000000010::bigint, 470000000000000014::bigint, '缓存管理', 'DatabaseOutlined', 10),
    (413501707400000002::bigint, 470000000000000014::bigint, 'SQL 控制台', 'ConsoleSqlOutlined', 20),
    (413501707400000004::bigint, 470000000000000014::bigint, 'SQL 执行记录', 'HistoryOutlined', 30),
    (419000000000000011::bigint, 470000000000000015::bigint, '脚本控制台', 'ConsoleSqlOutlined', 10),
    (426000000000000011::bigint, 470000000000000015::bigint, '脚本管理', 'CodeOutlined', 20),
    (426000000000000012::bigint, 470000000000000015::bigint, '脚本执行记录', 'HistoryOutlined', 30)
) AS source(id, parent_id, name, icon, sort)
WHERE target.id = source.id;

-- 页面较少且职责单一的应用直接平铺，避免与应用同名的单页空壳分组。
UPDATE public.t_sys_menu
SET parent_id = 0,
    sort = CASE id
        WHEN 413260828563165184 THEN 10
        WHEN 413260828571553792 THEN 20
        ELSE sort
    END,
    name = CASE id
        WHEN 413260828571553792 THEN '执行记录'
        ELSE name
    END,
    update_time = now(),
    version = version + 1
WHERE id IN (413260828563165184, 413260828571553792);

UPDATE public.t_sys_menu
SET parent_id = 0,
    sort = 10,
    update_time = now(),
    version = version + 1
WHERE id = 430000000000000020;

-- 子菜单全部迁移完成后移除旧分组。
DELETE FROM public.t_sys_menu
WHERE id IN (
    3101,
    413172783532929024,
    413196675785879552,
    50060,
    3000,
    50080,
    413501707332751360,
    413501707410000001,
    419000000000000010,
    413501707370500096,
    5001,
    430000000000000019
);

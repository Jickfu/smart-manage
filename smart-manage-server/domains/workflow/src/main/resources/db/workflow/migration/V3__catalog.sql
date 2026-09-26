INSERT INTO t_sys_domain (id, name, number, seq) VALUES (470000000000000001, '工作流', 'workflow', 30);
INSERT INTO t_sys_app (id, name, number, icon, domain_id, seq) VALUES
(470000000000000002, '流程管理', 'process', 'ApartmentOutlined', 470000000000000001, 10);
INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(470000000000000011, 'workflow/process/definition', 470000000000000002, '流程定义', 10),
(470000000000000012, 'workflow/process/task', 470000000000000002, '任务中心', 20);
INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(470000000000000021, '流程定义-入口', 'workflow:process:definition', 470000000000000011),
(470000000000000022, '流程定义-列表', 'workflow:process:definition:listPage', 470000000000000011),
(470000000000000023, '流程定义-设计', 'workflow:process:definition:design', 470000000000000011),
(470000000000000024, '流程定义-保存', 'workflow:process:definition:save', 470000000000000011),
(470000000000000025, '流程定义-发布与停用', 'workflow:process:definition:publish', 470000000000000011),
(470000000000000031, '任务中心-入口', 'workflow:process:task', 470000000000000012),
(470000000000000032, '任务中心-维护候选人', 'workflow:process:task:maintain', 470000000000000012);
INSERT INTO t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(470000000000000041, 'workflow_definition', '流程定义', 1, 470000000000000002, 470000000000000021,
 '/workflow/process/definition', 'workflow/process/definition', 'ApartmentOutlined', 10, 470000000000000011, 'INTERNAL_PAGE'),
(470000000000000042, 'workflow_task', '任务中心', 1, 470000000000000002, 470000000000000031,
 '/workflow/process/task', 'workflow/process/task', 'AuditOutlined', 20, 470000000000000012, 'INTERNAL_PAGE');

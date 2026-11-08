INSERT INTO t_sys_domain (id, name, number, seq) VALUES (470000000000000001, '工作流', 'workflow', 30);

INSERT INTO t_sys_app (id, name, number, icon, domain_id, seq) VALUES
(470000000000000002, '流程管理', 'process', 'ApartmentOutlined', 470000000000000001, 10);

INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(470000000000000011, 'workflow/process/definition', 470000000000000002, '流程定义', 10),
(470000000000000012, 'workflow/process/task', 470000000000000002, '个人流程任务', 50);

INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(470000000000000013, 'workflow/process/instance', 470000000000000002, '流程实例', 20),
(470000000000000014, 'workflow/process/task-monitor', 470000000000000002, '任务监控', 30),
(470000000000000015, 'workflow/process/flow-log', 470000000000000002, '流转日志', 40);

INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(470000000000000021, '流程定义-入口', 'workflow:process:definition', 470000000000000011),
(470000000000000022, '流程定义-列表', 'workflow:process:definition:listPage', 470000000000000011),
(470000000000000023, '流程定义-设计', 'workflow:process:definition:design', 470000000000000011),
(470000000000000024, '流程定义-保存', 'workflow:process:definition:save', 470000000000000011),
(470000000000000025, '流程定义-发布与停用', 'workflow:process:definition:publish', 470000000000000011),
(470000000000000032, '任务监控-维护候选人', 'workflow:process:task-monitor:maintain', 470000000000000014);

INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(470000000000000041, '流程实例-入口', 'workflow:process:instance', 470000000000000013),
(470000000000000042, '流程实例-列表', 'workflow:process:instance:listPage', 470000000000000013),
(470000000000000043, '流程实例-详情', 'workflow:process:instance:detail', 470000000000000013),
(470000000000000044, '流程实例-挂起', 'workflow:process:instance:suspend', 470000000000000013),
(470000000000000045, '流程实例-恢复', 'workflow:process:instance:resume', 470000000000000013),
(470000000000000046, '流程实例-终止', 'workflow:process:instance:terminate', 470000000000000013),
(470000000000000047, '流程实例-跳转', 'workflow:process:instance:jump', 470000000000000013),
(470000000000000048, '流程实例-修改变量', 'workflow:process:instance:variables', 470000000000000013),
(470000000000000051, '任务监控-入口', 'workflow:process:task-monitor', 470000000000000014),
(470000000000000052, '任务监控-列表', 'workflow:process:task-monitor:listPage', 470000000000000014),
(470000000000000061, '流转日志-入口', 'workflow:process:flow-log', 470000000000000015),
(470000000000000062, '流转日志-列表', 'workflow:process:flow-log:listPage', 470000000000000015);

INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(470000000000000049, '流程实例-重试脚本节点', 'workflow:process:instance:script-retry', 470000000000000013);

INSERT INTO t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(470000000000000041, 'workflow_definition', '流程定义', 1, 470000000000000002, 470000000000000021,
 '/workflow/process/definition', 'workflow/process/definition', 'ApartmentOutlined', 10, 470000000000000011, 'INTERNAL_PAGE');

INSERT INTO t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(470000000000000043, 'workflow_instance', '流程实例', 1, 470000000000000002, 470000000000000041,
 '/workflow/process/instance', 'workflow/process/instance', 'DeploymentUnitOutlined', 20, 470000000000000013, 'INTERNAL_PAGE'),
(470000000000000044, 'workflow_task_monitor', '任务监控', 1, 470000000000000002, 470000000000000051,
 '/workflow/process/task-monitor', 'workflow/process/task-monitor', 'MonitorOutlined', 30, 470000000000000014, 'INTERNAL_PAGE'),
(470000000000000045, 'workflow_flow_log', '流转日志', 1, 470000000000000002, 470000000000000061,
 '/workflow/process/flow-log', 'workflow/process/flow-log', 'ProfileOutlined', 40, 470000000000000015, 'INTERNAL_PAGE');

INSERT INTO t_sys_job (id, number, job_name, description, job_class_name, cron_expression, job_data, status, is_system, version, app_id)
VALUES (470000000000000051, 'workflowNotification', '工作流通知投递', '投递已提交工作流事务中的待办和结果通知',
 'sm.domain.workflow.process.notification.job.DispatchWorkflowNotificationJob', '0/10 * * * * ?', '{}', 'ENABLED', true, 0, 470000000000000002);

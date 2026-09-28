CREATE TABLE t_workflow_operation (
    id bigint PRIMARY KEY,
    instance_id bigint NOT NULL REFERENCES t_workflow_instance(id),
    task_id bigint,
    action varchar(40) NOT NULL,
    operator_id bigint NOT NULL,
    reason varchar(500) NOT NULL,
    before_data text,
    after_data text,
    create_time timestamp,
    update_time timestamp,
    create_user bigint,
    update_user bigint
);
COMMENT ON TABLE t_workflow_operation IS '流程实例管理操作审计；实例禁止删除，审计记录随实例永久保留';
COMMENT ON COLUMN t_workflow_operation.id IS '主键';
COMMENT ON COLUMN t_workflow_operation.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_operation.task_id IS '关联当前任务ID';
COMMENT ON COLUMN t_workflow_operation.action IS '管理动作';
COMMENT ON COLUMN t_workflow_operation.operator_id IS '操作人ID';
COMMENT ON COLUMN t_workflow_operation.reason IS '操作原因';
COMMENT ON COLUMN t_workflow_operation.before_data IS '操作前工作流投影';
COMMENT ON COLUMN t_workflow_operation.after_data IS '操作后工作流投影';
COMMENT ON COLUMN t_workflow_operation.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_operation.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_operation.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_operation.update_user IS '更新人';
CREATE INDEX idx_workflow_operation_instance ON t_workflow_operation(instance_id, create_time, id);

INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq) VALUES
(470000000000000013, 'workflow/process/instance', 470000000000000002, '流程实例', 20),
(470000000000000014, 'workflow/process/task-monitor', 470000000000000002, '任务监控', 30),
(470000000000000015, 'workflow/process/flow-log', 470000000000000002, '流转日志', 40);

UPDATE t_sys_feature
SET default_name = '个人流程任务', default_seq = 50
WHERE id = 470000000000000012;

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

DELETE FROM t_sys_menu WHERE id = 470000000000000042;
DELETE FROM t_sys_permission WHERE id = 470000000000000031;
UPDATE t_sys_permission
SET name = '任务监控-维护候选人',
    number = 'workflow:process:task-monitor:maintain',
    feature_id = 470000000000000014
WHERE id = 470000000000000032;

INSERT INTO t_sys_menu (id, number, name, level, app_id, permission_id, path, component, icon, sort, feature_id, target_type) VALUES
(470000000000000043, 'workflow_instance', '流程实例', 1, 470000000000000002, 470000000000000041,
 '/workflow/process/instance', 'workflow/process/instance', 'DeploymentUnitOutlined', 20, 470000000000000013, 'INTERNAL_PAGE'),
(470000000000000044, 'workflow_task_monitor', '任务监控', 1, 470000000000000002, 470000000000000051,
 '/workflow/process/task-monitor', 'workflow/process/task-monitor', 'MonitorOutlined', 30, 470000000000000014, 'INTERNAL_PAGE'),
(470000000000000045, 'workflow_flow_log', '流转日志', 1, 470000000000000002, 470000000000000061,
 '/workflow/process/flow-log', 'workflow/process/flow-log', 'ProfileOutlined', 40, 470000000000000015, 'INTERNAL_PAGE');

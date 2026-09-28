CREATE TABLE t_workflow_script_execution (
    id bigint PRIMARY KEY,
    instance_id bigint NOT NULL REFERENCES t_workflow_instance(id),
    task_id bigint NOT NULL,
    node_code varchar(100) NOT NULL,
    status varchar(20) NOT NULL,
    duration_ms integer NOT NULL,
    error_message varchar(500),
    result_data text,
    operator_id bigint NOT NULL,
    create_time timestamp,
    update_time timestamp,
    create_user bigint,
    update_user bigint
);
COMMENT ON TABLE t_workflow_script_execution IS '受限工作流脚本节点执行记录';
COMMENT ON COLUMN t_workflow_script_execution.id IS '主键';
COMMENT ON COLUMN t_workflow_script_execution.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_script_execution.task_id IS '映射的Warm-Flow任务ID';
COMMENT ON COLUMN t_workflow_script_execution.node_code IS '脚本节点编码';
COMMENT ON COLUMN t_workflow_script_execution.status IS 'SUCCESS、ERROR或TIMEOUT';
COMMENT ON COLUMN t_workflow_script_execution.duration_ms IS '执行耗时毫秒';
COMMENT ON COLUMN t_workflow_script_execution.error_message IS '安全裁剪后的失败说明';
COMMENT ON COLUMN t_workflow_script_execution.result_data IS '脚本返回的流程变量和下一节点参与人';
COMMENT ON COLUMN t_workflow_script_execution.operator_id IS '自动执行时为申请人，重试时为管理员';
COMMENT ON COLUMN t_workflow_script_execution.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_script_execution.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_script_execution.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_script_execution.update_user IS '更新人';
CREATE INDEX idx_workflow_script_execution_instance
    ON t_workflow_script_execution(instance_id, create_time, id);

INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(470000000000000049, '流程实例-重试脚本节点', 'workflow:process:instance:script-retry', 470000000000000013);

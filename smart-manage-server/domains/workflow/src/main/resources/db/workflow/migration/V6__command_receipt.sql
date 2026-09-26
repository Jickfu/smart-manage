CREATE TABLE t_workflow_command (
    request_id uuid PRIMARY KEY, actor_id bigint NOT NULL, instance_id bigint NOT NULL,
    request_digest varchar(64) NOT NULL, result text NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE t_workflow_command IS '审批和撤回命令回执，与业务事务一同提交；同键异内容拒绝';
COMMENT ON COLUMN t_workflow_command.result IS '本项目运行结果投影，不含业务单据快照';
COMMENT ON COLUMN t_workflow_command.request_id IS '幂等请求标识';
COMMENT ON COLUMN t_workflow_command.actor_id IS '命令执行主体ID';
COMMENT ON COLUMN t_workflow_command.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_command.request_digest IS '请求内容SHA-256摘要';
COMMENT ON COLUMN t_workflow_command.create_time IS '创建时间';

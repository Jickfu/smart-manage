CREATE TABLE t_workflow_candidate_change (
    id bigint PRIMARY KEY, instance_id bigint NOT NULL, task_id bigint NOT NULL, operator_id bigint NOT NULL,
    before_candidates text NOT NULL, after_candidates text NOT NULL, reason varchar(1000) NOT NULL,
    create_time timestamp NOT NULL DEFAULT current_timestamp, update_time timestamp, create_user bigint, update_user bigint
);
COMMENT ON TABLE t_workflow_candidate_change IS '当前任务候选人维护审计；不属于审批记录，不影响首节点撤回资格';
CREATE INDEX idx_workflow_candidate_change_instance ON t_workflow_candidate_change(instance_id, id);
COMMENT ON COLUMN t_workflow_candidate_change.id IS '主键';
COMMENT ON COLUMN t_workflow_candidate_change.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_candidate_change.task_id IS '任务ID';
COMMENT ON COLUMN t_workflow_candidate_change.operator_id IS '操作人ID';
COMMENT ON COLUMN t_workflow_candidate_change.before_candidates IS '变更前候选人ID列表';
COMMENT ON COLUMN t_workflow_candidate_change.after_candidates IS '变更后候选人ID列表';
COMMENT ON COLUMN t_workflow_candidate_change.reason IS '原因';
COMMENT ON COLUMN t_workflow_candidate_change.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_candidate_change.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_candidate_change.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_candidate_change.update_user IS '更新人';

CREATE TABLE t_workflow_binding (
    id bigint PRIMARY KEY, number varchar(40) NOT NULL UNIQUE, name varchar(100) NOT NULL,
    business_type varchar(100) NOT NULL UNIQUE, enabled boolean NOT NULL DEFAULT true,
    version integer NOT NULL DEFAULT 0, create_time timestamp, update_time timestamp,
    create_user bigint, update_user bigint
);
COMMENT ON TABLE t_workflow_binding IS '流程业务绑定；引擎拥有定义和版本';
COMMENT ON COLUMN t_workflow_binding.business_type IS '创建时绑定已注册业务类型，后续版本继承此绑定';
COMMENT ON COLUMN t_workflow_binding.enabled IS '只控制新发起，不终止现有实例';

CREATE TABLE t_workflow_instance (
    id bigint PRIMARY KEY, business_type varchar(100) NOT NULL, business_id bigint NOT NULL,
    number varchar(100) NOT NULL, org_id bigint NOT NULL, applicant_id bigint NOT NULL,
    request_id uuid NOT NULL UNIQUE, request_digest varchar(64) NOT NULL, snapshot text NOT NULL,
    create_time timestamp NOT NULL DEFAULT current_timestamp, update_time timestamp,
    create_user bigint, update_user bigint
);
COMMENT ON TABLE t_workflow_instance IS '每轮流程的不可变业务关联和单据快照；不复制任务或引擎状态';
COMMENT ON COLUMN t_workflow_instance.snapshot IS '提交时服务端生成的业务快照，历史详情不能读取新轮次单据';
CREATE INDEX idx_workflow_instance_business ON t_workflow_instance(business_type, business_id, id DESC);
CREATE INDEX idx_workflow_instance_applicant ON t_workflow_instance(applicant_id, id DESC);
CREATE INDEX idx_flow_user_task_candidates ON flow_user(associated, type, processed_by);
CREATE INDEX idx_flow_history_actor ON flow_his_task(approver, instance_id);
COMMENT ON COLUMN t_workflow_binding.id IS '主键';
COMMENT ON COLUMN t_workflow_binding.number IS '编码';
COMMENT ON COLUMN t_workflow_binding.name IS '名称';
COMMENT ON COLUMN t_workflow_binding.version IS '乐观锁版本';
COMMENT ON COLUMN t_workflow_binding.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_binding.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_binding.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_binding.update_user IS '更新人';
COMMENT ON COLUMN t_workflow_instance.id IS '主键';
COMMENT ON COLUMN t_workflow_instance.business_type IS '业务类型';
COMMENT ON COLUMN t_workflow_instance.business_id IS '业务主单ID';
COMMENT ON COLUMN t_workflow_instance.number IS '编码';
COMMENT ON COLUMN t_workflow_instance.org_id IS '所属组织ID';
COMMENT ON COLUMN t_workflow_instance.applicant_id IS '申请人ID';
COMMENT ON COLUMN t_workflow_instance.request_id IS '幂等请求标识';
COMMENT ON COLUMN t_workflow_instance.request_digest IS '请求内容SHA-256摘要';
COMMENT ON COLUMN t_workflow_instance.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_instance.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_instance.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_instance.update_user IS '更新人';

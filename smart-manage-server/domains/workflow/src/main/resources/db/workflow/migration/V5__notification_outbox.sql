CREATE TABLE t_workflow_outbox (
    id bigint PRIMARY KEY,
    event_key varchar(200) NOT NULL UNIQUE,
    instance_id bigint NOT NULL REFERENCES t_workflow_instance(id),
    recipient_id bigint NOT NULL,
    title varchar(200) NOT NULL,
    content varchar(1000) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','DELIVERED')),
    attempts integer NOT NULL DEFAULT 0,
    next_attempt_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claim_token uuid,
    claimed_until timestamp,
    last_error varchar(200),
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE t_workflow_outbox IS '工作流事务通知发件箱，失败重试不影响审批结果';
COMMENT ON COLUMN t_workflow_outbox.event_key IS '单个事件和接收人的稳定幂等键';
COMMENT ON COLUMN t_workflow_outbox.claim_token IS '投递租约标识，旧租约不得确认新投递';
COMMENT ON COLUMN t_workflow_outbox.last_error IS '安全错误分类，不保存异常正文或单据隐私';
CREATE INDEX idx_workflow_outbox_pending ON t_workflow_outbox(next_attempt_at) WHERE status = 'PENDING';

INSERT INTO t_sys_job (id, number, job_name, description, job_class_name, cron_expression, job_data, status, is_system, version, app_id)
VALUES (470000000000000051, 'workflowNotification', '工作流通知投递', '投递已提交工作流事务中的待办和结果通知',
 'sm.domain.workflow.process.notification.job.DispatchWorkflowNotificationJob', '0/10 * * * * ?', '{}', 'ENABLED', true, 0, 470000000000000002);
COMMENT ON COLUMN t_workflow_outbox.id IS '主键';
COMMENT ON COLUMN t_workflow_outbox.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_outbox.recipient_id IS '接收人ID';
COMMENT ON COLUMN t_workflow_outbox.title IS '通知标题';
COMMENT ON COLUMN t_workflow_outbox.content IS '通知正文';
COMMENT ON COLUMN t_workflow_outbox.status IS '投递状态';
COMMENT ON COLUMN t_workflow_outbox.attempts IS '已尝试次数';
COMMENT ON COLUMN t_workflow_outbox.next_attempt_at IS '下次可投递时间';
COMMENT ON COLUMN t_workflow_outbox.claimed_until IS '投递租约截止时间';
COMMENT ON COLUMN t_workflow_outbox.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_outbox.update_time IS '更新时间';

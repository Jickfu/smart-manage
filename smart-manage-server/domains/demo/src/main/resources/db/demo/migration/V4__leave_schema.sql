CREATE TABLE t_demo_leave (
    id bigint PRIMARY KEY, number varchar(100) NOT NULL, client_key uuid NOT NULL UNIQUE,
    org_id bigint NOT NULL, applicant_id bigint NOT NULL, biz_date date NOT NULL,
    leave_type varchar(20) NOT NULL, start_time timestamp NOT NULL, end_time timestamp NOT NULL,
    days numeric(6,2) NOT NULL CHECK (days > 0), reason varchar(2000) NOT NULL,
    bill_status char(1) NOT NULL DEFAULT 'A' CHECK (bill_status IN ('A','B','C')),
    current_instance_id bigint, last_outcome varchar(20), version integer NOT NULL DEFAULT 0,
    create_time timestamp, update_time timestamp, create_user bigint, update_user bigint,
    CHECK (end_time > start_time), UNIQUE(org_id, number)
);
COMMENT ON TABLE t_demo_leave IS '独立请假样板，不改变采购审批行为';
COMMENT ON COLUMN t_demo_leave.bill_status IS 'A可编辑，B审批中，C通过；拒绝或撤回回到A，结果记录在last_outcome';
COMMENT ON COLUMN t_demo_leave.days IS '人工填写天数，不计算工作日或考勤余额';
COMMENT ON COLUMN t_demo_leave.current_instance_id IS '最近轮次；不建立跨领域外键';
CREATE INDEX idx_demo_leave_owner ON t_demo_leave(applicant_id, id DESC);
CREATE INDEX idx_demo_leave_org ON t_demo_leave(org_id, id DESC);

CREATE TABLE t_demo_leave_attachment_entry (
    id bigint PRIMARY KEY, parent_id bigint NOT NULL, attachment_id bigint NOT NULL,
    UNIQUE(parent_id, attachment_id)
);
COMMENT ON TABLE t_demo_leave_attachment_entry IS '当前请假单选择的附件';
CREATE TABLE t_demo_leave_attachment_snapshot (
    id bigint PRIMARY KEY, leave_id bigint NOT NULL, instance_id bigint NOT NULL, attachment_id bigint NOT NULL,
    UNIQUE(instance_id, attachment_id)
);
COMMENT ON TABLE t_demo_leave_attachment_snapshot IS '每轮不可变附件引用，用于保留和精确授权';
CREATE INDEX idx_demo_leave_attachment_snapshot_file ON t_demo_leave_attachment_snapshot(leave_id, attachment_id);
COMMENT ON COLUMN t_demo_leave.id IS '主键';
COMMENT ON COLUMN t_demo_leave.number IS '编码';
COMMENT ON COLUMN t_demo_leave.client_key IS '客户端稳定建单标识';
COMMENT ON COLUMN t_demo_leave.org_id IS '所属组织ID';
COMMENT ON COLUMN t_demo_leave.applicant_id IS '申请人ID';
COMMENT ON COLUMN t_demo_leave.biz_date IS '业务日期';
COMMENT ON COLUMN t_demo_leave.leave_type IS '请假类型';
COMMENT ON COLUMN t_demo_leave.start_time IS '开始时间';
COMMENT ON COLUMN t_demo_leave.end_time IS '结束时间';
COMMENT ON COLUMN t_demo_leave.reason IS '原因';
COMMENT ON COLUMN t_demo_leave.last_outcome IS '最近轮次结果';
COMMENT ON COLUMN t_demo_leave.version IS '乐观锁版本';
COMMENT ON COLUMN t_demo_leave.create_time IS '创建时间';
COMMENT ON COLUMN t_demo_leave.update_time IS '更新时间';
COMMENT ON COLUMN t_demo_leave.create_user IS '创建人';
COMMENT ON COLUMN t_demo_leave.update_user IS '更新人';
COMMENT ON COLUMN t_demo_leave_attachment_entry.id IS '主键';
COMMENT ON COLUMN t_demo_leave_attachment_entry.parent_id IS '所属主单ID';
COMMENT ON COLUMN t_demo_leave_attachment_entry.attachment_id IS '附件ID';
COMMENT ON COLUMN t_demo_leave_attachment_snapshot.id IS '主键';
COMMENT ON COLUMN t_demo_leave_attachment_snapshot.leave_id IS '请假单ID';
COMMENT ON COLUMN t_demo_leave_attachment_snapshot.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_demo_leave_attachment_snapshot.attachment_id IS '附件ID';

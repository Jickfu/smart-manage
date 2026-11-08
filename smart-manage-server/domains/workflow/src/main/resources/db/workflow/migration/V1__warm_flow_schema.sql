-- Warm-Flow 1.8.9, Apache-2.0, Copyright 2024-2025 Warm-Flow.
-- Source: dromara/warm-flow @ 37b17a8939f0931ab8c19d66b768050325da3bcf
CREATE TABLE flow_definition
(
    id              int8         NOT NULL,
    flow_code       varchar(40)  NOT NULL,
    flow_name       varchar(100) NOT NULL,
    model_value     varchar(40)  NOT NULL DEFAULT 'CLASSICS',
    category        varchar(100) NULL,
    "version"       varchar(20)  NOT NULL,
    is_publish      int2         NOT NULL DEFAULT 0,
    form_custom     bpchar(1)    NULL     DEFAULT 'N':: character varying,
    form_path       varchar(100) NULL,
    activity_status int2         NOT NULL DEFAULT 1,
    listener_type   varchar(100) NULL,
    listener_path   varchar(400) NULL,
    ext             varchar(500) NULL,
    create_time     timestamp    NULL,
    create_by       varchar(64)  NULL     DEFAULT '':: character varying,
    update_time     timestamp    NULL,
    update_by       varchar(64)  NULL     DEFAULT '':: character varying,
    del_flag        bpchar(1)    NULL     DEFAULT '0':: character varying,
    tenant_id       varchar(40)  NULL,
    CONSTRAINT flow_definition_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_definition IS '流程定义表';

COMMENT ON COLUMN flow_definition.id IS '主键id';
COMMENT ON COLUMN flow_definition.flow_code IS '流程编码';
COMMENT ON COLUMN flow_definition.flow_name IS '流程名称';
COMMENT ON COLUMN flow_definition.model_value IS '设计器模型（CLASSICS经典模型 MIMIC仿钉钉模型）';
COMMENT ON COLUMN flow_definition.category IS '流程类别';
COMMENT ON COLUMN flow_definition."version" IS '流程版本';
COMMENT ON COLUMN flow_definition.is_publish IS '是否发布（0未发布 1已发布 9失效）';
COMMENT ON COLUMN flow_definition.form_custom IS '审批表单是否自定义（Y是 N否）';
COMMENT ON COLUMN flow_definition.form_path IS '审批表单路径';
COMMENT ON COLUMN flow_definition.activity_status IS '流程激活状态（0挂起 1激活）';
COMMENT ON COLUMN flow_definition.listener_type IS '监听器类型';
COMMENT ON COLUMN flow_definition.listener_path IS '监听器路径';
COMMENT ON COLUMN flow_definition.ext IS '扩展字段，预留给业务系统使用';
COMMENT ON COLUMN flow_definition.create_time IS '创建时间';
COMMENT ON COLUMN flow_definition.create_by IS '创建人';
COMMENT ON COLUMN flow_definition.update_time IS '更新时间';
COMMENT ON COLUMN flow_definition.update_by IS '更新人';
COMMENT ON COLUMN flow_definition.del_flag IS '删除标志';
COMMENT ON COLUMN flow_definition.tenant_id IS '租户id';

CREATE TABLE flow_node
(
    id              int8          NOT NULL,
    node_type       int2          NOT NULL,
    definition_id   int8          NOT NULL,
    node_code       varchar(100)  NOT NULL,
    node_name       varchar(100)  NULL,
    permission_flag varchar(200)  NULL,
    node_ratio      varchar(200) NULL,
    coordinate      varchar(100)  NULL,
    any_node_skip   varchar(100)  NULL,
    listener_type   varchar(100)  NULL,
    listener_path   varchar(400)  NULL,
    form_custom     bpchar(1)     NULL DEFAULT 'N':: character varying,
    form_path       varchar(100)  NULL,
    "version"       varchar(20)   NOT NULL,
    create_time     timestamp     NULL,
    create_by       varchar(64)   NULL     DEFAULT '':: character varying,
    update_time     timestamp     NULL,
    update_by       varchar(64)   NULL     DEFAULT '':: character varying,
    ext             text          NULL,
    del_flag        bpchar(1)     NULL DEFAULT '0':: character varying,
    tenant_id       varchar(40)   NULL,
    CONSTRAINT flow_node_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_node IS '流程节点表';

COMMENT ON COLUMN flow_node.id IS '主键id';
COMMENT ON COLUMN flow_node.node_type IS '节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_node.definition_id IS '流程定义id';
COMMENT ON COLUMN flow_node.node_code IS '流程节点编码';
COMMENT ON COLUMN flow_node.node_name IS '流程节点名称';
COMMENT ON COLUMN flow_node.permission_flag IS '权限标识（权限类型:权限标识，可以多个，用@@隔开)';
COMMENT ON COLUMN flow_node.node_ratio IS '流程签署比例值';
COMMENT ON COLUMN flow_node.coordinate IS '坐标';
COMMENT ON COLUMN flow_node.any_node_skip IS '任意结点跳转';
COMMENT ON COLUMN flow_node.listener_type IS '监听器类型';
COMMENT ON COLUMN flow_node.listener_path IS '监听器路径';
COMMENT ON COLUMN flow_node.form_custom IS '审批表单是否自定义（Y是 N否）';
COMMENT ON COLUMN flow_node.form_path IS '审批表单路径';
COMMENT ON COLUMN flow_node."version" IS '版本';
COMMENT ON COLUMN flow_node.create_time IS '创建时间';
COMMENT ON COLUMN flow_node.create_by IS '创建人';
COMMENT ON COLUMN flow_node.update_time IS '更新时间';
COMMENT ON COLUMN flow_node.update_by IS '更新人';
COMMENT ON COLUMN flow_node.ext IS '节点扩展属性';
COMMENT ON COLUMN flow_node.del_flag IS '删除标志';
COMMENT ON COLUMN flow_node.tenant_id IS '租户id';


CREATE TABLE flow_skip
(
    id             int8         NOT NULL,
    definition_id  int8         NOT NULL,
    now_node_code  varchar(100) NOT NULL,
    now_node_type  int2         NULL,
    next_node_code varchar(100) NOT NULL,
    next_node_type int2         NULL,
    skip_name      varchar(100) NULL,
    skip_type      varchar(40)  NULL,
    skip_condition varchar(200) NULL,
    coordinate     varchar(100) NULL,
    create_time    timestamp    NULL,
    create_by      varchar(64)  NULL     DEFAULT '':: character varying,
    update_time    timestamp    NULL,
    update_by      varchar(64)  NULL     DEFAULT '':: character varying,
    del_flag       bpchar(1)    NULL DEFAULT '0':: character varying,
    tenant_id      varchar(40)  NULL,
    CONSTRAINT flow_skip_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_skip IS '节点跳转关联表';

COMMENT ON COLUMN flow_skip.id IS '主键id';
COMMENT ON COLUMN flow_skip.definition_id IS '流程定义id';
COMMENT ON COLUMN flow_skip.now_node_code IS '当前流程节点的编码';
COMMENT ON COLUMN flow_skip.now_node_type IS '当前节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_skip.next_node_code IS '下一个流程节点的编码';
COMMENT ON COLUMN flow_skip.next_node_type IS '下一个节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_skip.skip_name IS '跳转名称';
COMMENT ON COLUMN flow_skip.skip_type IS '跳转类型（PASS审批通过 REJECT退回）';
COMMENT ON COLUMN flow_skip.skip_condition IS '跳转条件';
COMMENT ON COLUMN flow_skip.coordinate IS '坐标';
COMMENT ON COLUMN flow_skip.create_time IS '创建时间';
COMMENT ON COLUMN flow_skip.create_by IS '创建人';
COMMENT ON COLUMN flow_skip.update_time IS '更新时间';
COMMENT ON COLUMN flow_skip.update_by IS '更新人';
COMMENT ON COLUMN flow_skip.del_flag IS '删除标志';
COMMENT ON COLUMN flow_skip.tenant_id IS '租户id';

CREATE TABLE flow_instance
(
    id              int8         NOT NULL,
    definition_id   int8         NOT NULL,
    business_id     varchar(40)  NOT NULL,
    node_type       int2         NOT NULL,
    node_code       varchar(40)  NOT NULL,
    node_name       varchar(100) NULL,
    variable        text         NULL,
    flow_status     varchar(20)  NOT NULL,
    activity_status int2         NOT NULL DEFAULT 1,
    def_json        text         NULL,
    create_time     timestamp    NULL,
    create_by       varchar(64)  NULL     DEFAULT '':: character varying,
    update_time     timestamp    NULL,
    update_by       varchar(64)  NULL     DEFAULT '':: character varying,
    ext             varchar(500) NULL,
    del_flag        bpchar(1)    NULL     DEFAULT '0':: character varying,
    tenant_id       varchar(40)  NULL,
    CONSTRAINT flow_instance_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_instance IS '流程实例表';

COMMENT ON COLUMN flow_instance.id IS '主键id';
COMMENT ON COLUMN flow_instance.definition_id IS '对应flow_definition表的id';
COMMENT ON COLUMN flow_instance.business_id IS '业务id';
COMMENT ON COLUMN flow_instance.node_type IS '节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_instance.node_code IS '流程节点编码';
COMMENT ON COLUMN flow_instance.node_name IS '流程节点名称';
COMMENT ON COLUMN flow_instance.variable IS '任务变量';
COMMENT ON COLUMN flow_instance.flow_status IS '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）';
COMMENT ON COLUMN flow_instance.activity_status IS '流程激活状态（0挂起 1激活）';
COMMENT ON COLUMN flow_instance.def_json IS '流程定义json';
COMMENT ON COLUMN flow_instance.create_time IS '创建时间';
COMMENT ON COLUMN flow_instance.create_by IS '创建人';
COMMENT ON COLUMN flow_instance.update_time IS '更新时间';
COMMENT ON COLUMN flow_instance.update_by IS '更新人';
COMMENT ON COLUMN flow_instance.ext IS '扩展字段，预留给业务系统使用';
COMMENT ON COLUMN flow_instance.del_flag IS '删除标志';
COMMENT ON COLUMN flow_instance.tenant_id IS '租户id';

CREATE TABLE flow_task
(
    id            int8         NOT NULL,
    definition_id int8         NOT NULL,
    instance_id   int8         NOT NULL,
    node_code     varchar(100) NOT NULL,
    node_name     varchar(100) NULL,
    node_type     int2         NOT NULL,
    flow_status      varchar(20)  NOT NULL,
    form_custom   bpchar(1)    NULL DEFAULT 'N':: character varying,
    form_path     varchar(100) NULL,
    create_time   timestamp    NULL,
    create_by     varchar(64)  NULL     DEFAULT '':: character varying,
    update_time   timestamp    NULL,
    update_by     varchar(64)  NULL     DEFAULT '':: character varying,
    del_flag      bpchar(1)    NULL DEFAULT '0':: character varying,
    tenant_id     varchar(40)  NULL,
    CONSTRAINT flow_task_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_task IS '待办任务表';

COMMENT ON COLUMN flow_task.id IS '主键id';
COMMENT ON COLUMN flow_task.definition_id IS '对应flow_definition表的id';
COMMENT ON COLUMN flow_task.instance_id IS '对应flow_instance表的id';
COMMENT ON COLUMN flow_task.node_code IS '节点编码';
COMMENT ON COLUMN flow_task.node_name IS '节点名称';
COMMENT ON COLUMN flow_task.node_type IS '节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_task.flow_status IS '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）';
COMMENT ON COLUMN flow_task.form_custom IS '审批表单是否自定义（Y是 N否）';
COMMENT ON COLUMN flow_task.form_path IS '审批表单路径';
COMMENT ON COLUMN flow_task.create_time IS '创建时间';
COMMENT ON COLUMN flow_task.create_by IS '创建人';
COMMENT ON COLUMN flow_task.update_time IS '更新时间';
COMMENT ON COLUMN flow_task.update_by IS '更新人';
COMMENT ON COLUMN flow_task.del_flag IS '删除标志';
COMMENT ON COLUMN flow_task.tenant_id IS '租户id';

CREATE TABLE flow_his_task
(
    id               int8         NOT NULL,
    definition_id    int8         NOT NULL,
    instance_id      int8         NOT NULL,
    task_id          int8         NOT NULL,
    node_code        varchar(100) NULL,
    node_name        varchar(100) NULL,
    node_type        int2         NULL,
    target_node_code varchar(200) NULL,
    target_node_name varchar(200) NULL,
    approver         varchar(40)  NULL,
    cooperate_type   int2         NOT NULL DEFAULT 0,
    collaborator     varchar(500)  NULL,
    skip_type        varchar(10)  NULL,
    flow_status      varchar(20)  NOT NULL,
    form_custom      bpchar(1)    NULL     DEFAULT 'N':: character varying,
    form_path        varchar(100) NULL,
    ext              text         NULL,
    message          varchar(500) NULL,
    variable         text         NULL,
    create_time      timestamptz  NULL,
    update_time      timestamptz  NULL,
    del_flag         bpchar(1)    NULL     DEFAULT '0':: character varying,
    tenant_id        varchar(40)  NULL,
    CONSTRAINT flow_his_task_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE flow_his_task IS '历史任务记录表';

COMMENT ON COLUMN flow_his_task.id IS '主键id';
COMMENT ON COLUMN flow_his_task.definition_id IS '对应flow_definition表的id';
COMMENT ON COLUMN flow_his_task.instance_id IS '对应flow_instance表的id';
COMMENT ON COLUMN flow_his_task.task_id IS '对应flow_task表的id';
COMMENT ON COLUMN flow_his_task.node_code IS '开始节点编码';
COMMENT ON COLUMN flow_his_task.node_name IS '开始节点名称';
COMMENT ON COLUMN flow_his_task.node_type IS '开始节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）';
COMMENT ON COLUMN flow_his_task.target_node_code IS '目标节点编码';
COMMENT ON COLUMN flow_his_task.target_node_name IS '结束节点名称';
COMMENT ON COLUMN flow_his_task.approver IS '审批者';
COMMENT ON COLUMN flow_his_task.cooperate_type IS '协作方式(1审批 2转办 3委派 4会签 5票签 6加签 7减签)';
COMMENT ON COLUMN flow_his_task.collaborator IS '协作人';
COMMENT ON COLUMN flow_his_task.skip_type IS '流转类型（PASS通过 REJECT退回 NONE无动作）';
COMMENT ON COLUMN flow_his_task.flow_status IS '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）';
COMMENT ON COLUMN flow_his_task.form_custom IS '审批表单是否自定义（Y是 N否）';
COMMENT ON COLUMN flow_his_task.form_path IS '审批表单路径';
COMMENT ON COLUMN flow_his_task.message IS '审批意见';
COMMENT ON COLUMN flow_his_task.variable IS '任务变量';
COMMENT ON COLUMN flow_his_task.ext IS '扩展字段，预留给业务系统使用';
COMMENT ON COLUMN flow_his_task.create_time IS '任务开始的绝对时间';
COMMENT ON COLUMN flow_his_task.update_time IS '审批完成的绝对时间';
COMMENT ON COLUMN flow_his_task.del_flag IS '删除标志';
COMMENT ON COLUMN flow_his_task.tenant_id IS '租户id';

CREATE TABLE flow_user
(
    id           int8        NOT NULL,
    "type"       bpchar(1)   NOT NULL,
    processed_by varchar(80) NULL,
    associated   int8        NOT NULL,
    create_time  timestamp    NULL,
    create_by    varchar(64)  NULL     DEFAULT '':: character varying,
    update_time  timestamp    NULL,
    update_by    varchar(64)  NULL     DEFAULT '':: character varying,
    del_flag     bpchar(1)   NULL DEFAULT '0':: character varying,
    tenant_id    varchar(40) NULL,
    CONSTRAINT flow_user_pk PRIMARY KEY (id)
);
CREATE INDEX user_processed_type ON flow_user USING btree (processed_by, type);
CREATE INDEX user_associated_idx ON FLOW_USER USING btree (associated);
COMMENT ON TABLE flow_user IS '流程用户表';

COMMENT ON COLUMN flow_user.id IS '主键id';
COMMENT ON COLUMN flow_user."type" IS '人员类型（1待办任务的审批人权限 2待办任务的转办人权限 3待办任务的委托人权限）';
COMMENT ON COLUMN flow_user.processed_by IS '权限人';
COMMENT ON COLUMN flow_user.associated IS '任务表id';
COMMENT ON COLUMN flow_user.create_time IS '创建时间';
COMMENT ON COLUMN flow_user.create_by IS '创建人';
COMMENT ON COLUMN flow_user.update_time IS '更新时间';
COMMENT ON COLUMN flow_user.update_by IS '更新人';
COMMENT ON COLUMN flow_user.del_flag IS '删除标志';
COMMENT ON COLUMN flow_user.tenant_id IS '租户id';

-- 工作流领域的业务绑定、运行记录和审计结构。

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

CREATE TABLE t_workflow_command (
    id bigint PRIMARY KEY,
    request_id uuid NOT NULL UNIQUE, actor_id bigint NOT NULL, instance_id bigint NOT NULL,
    request_digest varchar(64) NOT NULL, result text NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE t_workflow_command IS '审批和撤回命令回执，与业务事务一同提交；同键异内容拒绝';
COMMENT ON COLUMN t_workflow_command.id IS 'ID';
COMMENT ON COLUMN t_workflow_command.result IS '本项目运行结果投影，不含业务单据快照';
COMMENT ON COLUMN t_workflow_command.request_id IS '幂等请求标识';
COMMENT ON COLUMN t_workflow_command.actor_id IS '命令执行主体ID';
COMMENT ON COLUMN t_workflow_command.instance_id IS '流程实例ID';
COMMENT ON COLUMN t_workflow_command.request_digest IS '请求内容SHA-256摘要';
COMMENT ON COLUMN t_workflow_command.create_time IS '创建时间';

CREATE TABLE t_workflow_operation (
    id bigint PRIMARY KEY,
    instance_id bigint NOT NULL REFERENCES t_workflow_instance(id),
    task_id bigint,
    action varchar(40) NOT NULL,
    operator_id bigint NOT NULL,
    reason varchar(500) NOT NULL,
    before_data text,
    after_data text,
    event_time timestamptz NOT NULL DEFAULT clock_timestamp(),
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
COMMENT ON COLUMN t_workflow_operation.event_time IS '管理事件入库时刻，独立于通用审计时间';
COMMENT ON COLUMN t_workflow_operation.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_operation.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_operation.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_operation.update_user IS '更新人';
CREATE INDEX idx_workflow_operation_instance ON t_workflow_operation(instance_id, create_time, id);

CREATE TABLE t_workflow_script_execution (
    id bigint PRIMARY KEY,
    instance_id bigint NOT NULL REFERENCES flow_instance(id),
    task_id bigint NOT NULL,
    node_code varchar(100) NOT NULL,
    status varchar(20) NOT NULL,
    duration_ms integer NOT NULL,
    error_message varchar(500),
    result_data text,
    operator_id bigint NOT NULL,
    event_time timestamptz NOT NULL DEFAULT clock_timestamp(),
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
COMMENT ON COLUMN t_workflow_script_execution.event_time IS '脚本执行记录入库时刻，独立于通用审计时间';
COMMENT ON COLUMN t_workflow_script_execution.create_time IS '创建时间';
COMMENT ON COLUMN t_workflow_script_execution.update_time IS '更新时间';
COMMENT ON COLUMN t_workflow_script_execution.create_user IS '创建人';
COMMENT ON COLUMN t_workflow_script_execution.update_user IS '更新人';
CREATE INDEX idx_workflow_script_execution_instance
    ON t_workflow_script_execution(instance_id, create_time, id);

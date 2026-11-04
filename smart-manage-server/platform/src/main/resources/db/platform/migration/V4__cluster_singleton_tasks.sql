-- 常驻任务按代码注册；共享期望状态不因重启而重置。
CREATE TABLE t_sys_singleton_task (
    id bigint PRIMARY KEY,
    number varchar(120) NOT NULL UNIQUE,
    name varchar(120) NOT NULL,
    app_number varchar(120) NOT NULL REFERENCES t_sys_app(number),
    description varchar(1000) NOT NULL,
    controllable boolean NOT NULL,
    requested boolean NOT NULL,
    version integer NOT NULL DEFAULT 0,
    state varchar(20) NOT NULL DEFAULT 'STOPPED' CHECK (state IN ('STOPPED','STARTING','RUNNING','STOPPING','FAILED')),
    owner_instance_id varchar(120),
    run_id varchar(36),
    generation bigint NOT NULL DEFAULT 0,
    started_at timestamp,
    heartbeat_at timestamp,
    retry_at timestamp,
    last_error varchar(80)
);
CREATE TABLE t_sys_singleton_event (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_number varchar(120) NOT NULL REFERENCES t_sys_singleton_task(number),
    occurred_at timestamp NOT NULL DEFAULT localtimestamp,
    event_type varchar(30) NOT NULL,
    instance_id varchar(120),
    run_id varchar(36),
    reason varchar(80)
);
CREATE INDEX idx_singleton_event_task ON t_sys_singleton_event(task_number, id DESC);
COMMENT ON TABLE t_sys_singleton_task IS '集群单例任务共享控制与运行状态';
COMMENT ON TABLE t_sys_singleton_event IS '集群单例任务近期运行事件，每任务保留最近200条';
INSERT INTO t_sys_feature (id, feature_key, app_id, default_name, default_seq, description)
VALUES (560000000000000300, 'sys/scheduler/singleton', 32, '常驻任务', 40, '集群单例任务运行状态与启停管理');
INSERT INTO t_sys_permission (id, name, number, feature_id) VALUES
(560000000000000301, '常驻任务-列表', 'sys:scheduler:singleton:listPage', 560000000000000300),
(560000000000000302, '常驻任务-详情', 'sys:scheduler:singleton:detail', 560000000000000300),
(560000000000000303, '常驻任务-启停', 'sys:scheduler:singleton:control', 560000000000000300);
INSERT INTO t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, sort, feature_id, target_type, icon)
VALUES (560000000000000310, 'singleton', '常驻任务', 1, 0, 32, 560000000000000301,
'/sys/scheduler/singleton', 'sys/scheduler/singleton', 30, 560000000000000300, 'INTERNAL_PAGE', 'ClusterOutlined');
INSERT INTO t_sys_feature_data_object (id, feature_id, schema_name, object_name, object_type, object_role, display_name, seq) VALUES
(560000000000000320, 560000000000000300, 'public', 't_sys_singleton_task', 'TABLE', 'PRIMARY', '常驻任务', 10),
(560000000000000321, 560000000000000300, 'public', 't_sys_singleton_event', 'TABLE', 'LOG', '常驻任务近期事件', 20);

COMMENT ON COLUMN t_sys_singleton_task.id IS 'ID';
COMMENT ON COLUMN t_sys_singleton_task.number IS '编码';
COMMENT ON COLUMN t_sys_singleton_task.name IS '名称';
COMMENT ON COLUMN t_sys_singleton_task.app_number IS '所属应用编码';
COMMENT ON COLUMN t_sys_singleton_task.description IS '任务说明';
COMMENT ON COLUMN t_sys_singleton_task.controllable IS '是否允许管理端启停';
COMMENT ON COLUMN t_sys_singleton_task.requested IS '期望是否运行';
COMMENT ON COLUMN t_sys_singleton_task.version IS '启停命令乐观锁版本';
COMMENT ON COLUMN t_sys_singleton_task.state IS '实际运行状态';
COMMENT ON COLUMN t_sys_singleton_task.owner_instance_id IS '当前执行实例';
COMMENT ON COLUMN t_sys_singleton_task.run_id IS '本次运行身份';
COMMENT ON COLUMN t_sys_singleton_task.generation IS '执行代际';
COMMENT ON COLUMN t_sys_singleton_task.started_at IS '本次开始时间';
COMMENT ON COLUMN t_sys_singleton_task.heartbeat_at IS '最近心跳时间';
COMMENT ON COLUMN t_sys_singleton_task.retry_at IS '退避截止时间';
COMMENT ON COLUMN t_sys_singleton_task.last_error IS '安全失败分类';
COMMENT ON COLUMN t_sys_singleton_event.id IS 'ID';
COMMENT ON COLUMN t_sys_singleton_event.task_number IS '任务编码';
COMMENT ON COLUMN t_sys_singleton_event.occurred_at IS '事件时间';
COMMENT ON COLUMN t_sys_singleton_event.event_type IS '事件类型';
COMMENT ON COLUMN t_sys_singleton_event.instance_id IS '执行实例';
COMMENT ON COLUMN t_sys_singleton_event.run_id IS '运行身份';
COMMENT ON COLUMN t_sys_singleton_event.reason IS '安全原因分类';

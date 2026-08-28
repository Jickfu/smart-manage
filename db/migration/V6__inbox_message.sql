CREATE TABLE public.t_sys_inbox_message (
    id bigint NOT NULL,
    scene_key character varying(100) NOT NULL,
    idempotency_key character varying(200) NOT NULL,
    title character varying(200) NOT NULL,
    content text NOT NULL,
    level character varying(20) DEFAULT 'NORMAL' NOT NULL,
    status character varying(20) DEFAULT 'DRAFT' NOT NULL,
    sender_user_id bigint,
    sender_name character varying(100),
    audience_type character varying(30) DEFAULT 'ALL_ENABLED_USERS' NOT NULL,
    recipient_count bigint DEFAULT 0 NOT NULL,
    publish_time timestamp without time zone,
    expire_time timestamp without time zone NOT NULL,
    resource_type character varying(100),
    resource_id character varying(100),
    action_code character varying(50),
    action_payload text,
    attempt_count integer DEFAULT 0 NOT NULL,
    claimed_time timestamp without time zone,
    error_message character varying(1000),
    create_time timestamp without time zone NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    CONSTRAINT t_sys_inbox_message_pkey PRIMARY KEY (id),
    CONSTRAINT ck_sys_inbox_message_level CHECK (level IN ('NORMAL', 'IMPORTANT', 'URGENT')),
    CONSTRAINT ck_sys_inbox_message_status CHECK (status IN ('DRAFT', 'PENDING', 'PUBLISHING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT ck_sys_inbox_message_audience CHECK (audience_type IN ('ALL_ENABLED_USERS', 'USERS')),
    CONSTRAINT ck_sys_inbox_message_expire CHECK (expire_time > create_time),
    CONSTRAINT ck_sys_inbox_message_publish_time CHECK (
        (status = 'PUBLISHED' AND publish_time IS NOT NULL)
        OR (status <> 'PUBLISHED' AND publish_time IS NULL)
    )
);

COMMENT ON TABLE public.t_sys_inbox_message IS '站内消息主体';
COMMENT ON COLUMN public.t_sys_inbox_message.id IS 'ID';
COMMENT ON COLUMN public.t_sys_inbox_message.scene_key IS '消息场景键';
COMMENT ON COLUMN public.t_sys_inbox_message.idempotency_key IS '场景幂等键';
COMMENT ON COLUMN public.t_sys_inbox_message.title IS '标题';
COMMENT ON COLUMN public.t_sys_inbox_message.content IS '纯文本正文';
COMMENT ON COLUMN public.t_sys_inbox_message.level IS '消息级别';
COMMENT ON COLUMN public.t_sys_inbox_message.status IS '发布状态';
COMMENT ON COLUMN public.t_sys_inbox_message.sender_user_id IS '发布人ID';
COMMENT ON COLUMN public.t_sys_inbox_message.sender_name IS '发布人名称快照';
COMMENT ON COLUMN public.t_sys_inbox_message.audience_type IS '收件范围类型';
COMMENT ON COLUMN public.t_sys_inbox_message.recipient_count IS '收件人数';
COMMENT ON COLUMN public.t_sys_inbox_message.publish_time IS '发布时间';
COMMENT ON COLUMN public.t_sys_inbox_message.expire_time IS '失效时间';
COMMENT ON COLUMN public.t_sys_inbox_message.resource_type IS '关联业务资源类型';
COMMENT ON COLUMN public.t_sys_inbox_message.resource_id IS '关联业务资源ID';
COMMENT ON COLUMN public.t_sys_inbox_message.action_code IS '关联业务动作编码';
COMMENT ON COLUMN public.t_sys_inbox_message.action_payload IS '关联业务动作参数';
COMMENT ON COLUMN public.t_sys_inbox_message.attempt_count IS '发布尝试次数';
COMMENT ON COLUMN public.t_sys_inbox_message.claimed_time IS '任务领取时间';
COMMENT ON COLUMN public.t_sys_inbox_message.error_message IS '失败原因';
COMMENT ON COLUMN public.t_sys_inbox_message.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_inbox_message.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_inbox_message.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_inbox_message.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_inbox_message.version IS '乐观锁版本号';

CREATE UNIQUE INDEX uk_sys_inbox_message_scene_idempotency
    ON public.t_sys_inbox_message(scene_key, idempotency_key);
CREATE INDEX idx_sys_inbox_message_admin_list
    ON public.t_sys_inbox_message(create_time DESC, id DESC);
CREATE INDEX idx_sys_inbox_message_dispatch
    ON public.t_sys_inbox_message(status, claimed_time, create_time)
    WHERE status IN ('PENDING', 'PUBLISHING');

CREATE TABLE public.t_sys_inbox_recipient (
    message_id bigint NOT NULL,
    user_id bigint NOT NULL,
    received_time timestamp without time zone NOT NULL,
    read_status boolean DEFAULT false NOT NULL,
    read_time timestamp without time zone,
    CONSTRAINT t_sys_inbox_recipient_pkey PRIMARY KEY (received_time, message_id, user_id),
    CONSTRAINT fk_sys_inbox_recipient_message FOREIGN KEY (message_id)
        REFERENCES public.t_sys_inbox_message(id),
    CONSTRAINT ck_sys_inbox_recipient_read CHECK (
        (read_status = false AND read_time IS NULL)
        OR (read_status = true AND read_time IS NOT NULL)
    )
) PARTITION BY RANGE (received_time);

COMMENT ON TABLE public.t_sys_inbox_recipient IS '站内消息用户收件状态（月度分区父表）';
COMMENT ON COLUMN public.t_sys_inbox_recipient.message_id IS '消息ID';
COMMENT ON COLUMN public.t_sys_inbox_recipient.user_id IS '收件用户ID';
COMMENT ON COLUMN public.t_sys_inbox_recipient.received_time IS '收件时间';
COMMENT ON COLUMN public.t_sys_inbox_recipient.read_status IS '是否已读';
COMMENT ON COLUMN public.t_sys_inbox_recipient.read_time IS '阅读时间';

CREATE INDEX idx_sys_inbox_recipient_timeline
    ON public.t_sys_inbox_recipient(user_id, received_time DESC, message_id DESC)
    INCLUDE (read_status, read_time);
CREATE INDEX idx_sys_inbox_recipient_unread
    ON public.t_sys_inbox_recipient(user_id, received_time DESC, message_id DESC)
    WHERE read_status = false;

DO $partition_creation$
DECLARE
    partition_start date := DATE '2026-01-01';
    partition_end date;
    partition_name text;
BEGIN
    WHILE partition_start < DATE '2030-01-01' LOOP
        partition_end := (partition_start + INTERVAL '1 month')::date;
        partition_name := 't_sys_inbox_recipient_p' || to_char(partition_start, 'YYYYMM');
        EXECUTE format(
            'CREATE TABLE public.%I PARTITION OF public.t_sys_inbox_recipient FOR VALUES FROM (%L) TO (%L)',
            partition_name, partition_start, partition_end
        );
        partition_start := partition_end;
    END LOOP;
END
$partition_creation$;

CREATE TABLE public.t_sys_inbox_recipient_default
    PARTITION OF public.t_sys_inbox_recipient DEFAULT;

UPDATE public.t_sys_app
SET description = '站内消息、发信账号、管理员邮件投递与发送记录',
    update_time = now(),
    version = version + 1
WHERE id = 470000000000001000;

INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, create_time, version)
VALUES
    (510000000000000010, 'sys/message/inbox-broadcast', 470000000000001000,
     '消息发布', 10, '创建、发布和查询全站站内消息', true, 'SYSTEM', now(), 0);

INSERT INTO public.t_sys_permission
    (id, name, number, create_time, version, feature_id, app_id)
VALUES
    (510000000000000101, '消息发布-查询', 'sys:message:inbox-broadcast:listPage', now(), 0, 510000000000000010, NULL),
    (510000000000000102, '消息发布-详情', 'sys:message:inbox-broadcast:detail', now(), 0, 510000000000000010, NULL),
    (510000000000000103, '消息发布-保存', 'sys:message:inbox-broadcast:save', now(), 0, 510000000000000010, NULL),
    (510000000000000104, '消息发布-发布', 'sys:message:inbox-broadcast:publish', now(), 0, 510000000000000010, NULL),
    (510000000000000105, '消息发布-重试', 'sys:message:inbox-broadcast:retry', now(), 0, 510000000000000010, NULL);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon,
     description, sort, enabled, create_time, version, feature_id, target_type)
VALUES
    (510000000000000200, 'inbox_message', '站内消息', 0, 0, 470000000000001000,
     470000000000001100, NULL, NULL, 'NotificationOutlined', '全站消息发布与用户消息中心',
     5, true, now(), 0, NULL, NULL),
    (510000000000000201, 'inbox_broadcast', '消息发布', 1, 510000000000000200,
     470000000000001000, 510000000000000101, '/sys/message/inbox-broadcast',
     'sys/message/inbox-broadcast', 'NotificationOutlined', '创建并发布全站站内消息',
     10, true, now(), 0, 510000000000000010, 'INTERNAL_PAGE');

INSERT INTO public.t_sys_job
    (id, job_name, job_group, description, job_class_name, cron_expression, job_data, status,
     create_time, number, is_system, version, mutex_key)
VALUES
    (510000000000000301, '站内消息发布派发', 'SYSTEM', '为待发布消息生成当前启用用户收件快照',
     'sm.domain.sys.scheduler.job.DispatchInboxMessageJob', '0/10 * * * * ?', '{"batchSize":5}',
     'ENABLED', now(), 'SYSTEM_INBOX_MESSAGE_DISPATCH', true, 0, 'system-inbox-message-dispatch');

-- 独立消息服务应用及首期 SMTP 邮件能力。
INSERT INTO public.t_sys_app
    (id, name, number, icon, seq, description, domain_id, enabled, create_time, version)
VALUES
    (470000000000001000, '消息服务', 'message', 'MailOutlined', 4,
     '发信账号、管理员邮件投递与发送记录', 4, true, CURRENT_TIMESTAMP, 0);

INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, version)
VALUES
    (470000000000001010, 'sys/message/email-account', 470000000000001000, '发信账号', 10,
     '维护 SMTP 发信账号及全局默认账号', true, 'SYSTEM', 0),
    (470000000000001020, 'sys/message/email-compose', 470000000000001000, '发送邮件', 20,
     '由超级管理员创建正式邮件投递任务', true, 'SYSTEM', 0),
    (470000000000001030, 'sys/message/email-record', 470000000000001000, '发送记录', 30,
     '查询、取消和重新发送邮件投递任务', true, 'SYSTEM', 0);

INSERT INTO public.t_sys_permission
    (id, name, number, version, feature_id, app_id)
VALUES
    (470000000000001101, '发信账号-查询', 'sys:message:email-account:listPage', 0, 470000000000001010, NULL),
    (470000000000001102, '发信账号-详情', 'sys:message:email-account:detail', 0, 470000000000001010, NULL),
    (470000000000001103, '发信账号-保存', 'sys:message:email-account:save', 0, 470000000000001010, NULL),
    (470000000000001104, '发信账号-启停', 'sys:message:email-account:enable', 0, 470000000000001010, NULL),
    (470000000000001105, '发信账号-删除', 'sys:message:email-account:delete', 0, 470000000000001010, NULL),
    (470000000000001106, '发信账号-测试', 'sys:message:email-account:test', 0, 470000000000001010, NULL),
    (470000000000001111, '发送邮件-发送', 'sys:message:email-compose:send', 0, 470000000000001020, NULL),
    (470000000000001121, '发送记录-查询', 'sys:message:email-record:listPage', 0, 470000000000001030, NULL),
    (470000000000001122, '发送记录-详情', 'sys:message:email-record:detail', 0, 470000000000001030, NULL),
    (470000000000001123, '发送记录-重发', 'sys:message:email-record:retry', 0, 470000000000001030, NULL),
    (470000000000001124, '发送记录-取消', 'sys:message:email-record:cancel', 0, 470000000000001030, NULL),
    (470000000000001100, '消息服务-访问', 'sys:message:access', 0, NULL, 470000000000001000);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon,
     description, sort, enabled, version, feature_id, target_type)
VALUES
    (470000000000001201, 'email_account', '发信账号', 1, 0, 470000000000001000,
     470000000000001101, '/sys/message/email-account', 'sys/message/email-account', 'MailOutlined',
     '维护 SMTP 发信账号', 10, true, 0, 470000000000001010, 'INTERNAL_PAGE'),
    (470000000000001202, 'email_compose', '发送邮件', 1, 0, 470000000000001000,
     470000000000001111, '/sys/message/email-compose', 'sys/message/email-compose', 'SendOutlined',
     '创建正式邮件投递任务', 20, true, 0, 470000000000001020, 'INTERNAL_PAGE'),
    (470000000000001203, 'email_record', '发送记录', 1, 0, 470000000000001000,
     470000000000001121, '/sys/message/email-record', 'sys/message/email-record', 'HistoryOutlined',
     '查看邮件投递状态和尝试结果', 30, true, 0, 470000000000001030, 'INTERNAL_PAGE');

CREATE TABLE public.t_sys_email_account (
    id bigint NOT NULL,
    number varchar(64) NOT NULL,
    name varchar(100) NOT NULL,
    host varchar(255) NOT NULL,
    port integer NOT NULL,
    security_mode varchar(16) NOT NULL,
    username varchar(255) NOT NULL,
    password_cipher text NOT NULL,
    from_address varchar(320) NOT NULL,
    from_name varchar(100),
    reply_to varchar(320),
    enabled boolean NOT NULL DEFAULT false,
    default_account boolean NOT NULL DEFAULT false,
    allow_manual boolean NOT NULL DEFAULT false,
    connection_timeout_ms integer NOT NULL DEFAULT 10000,
    read_timeout_ms integer NOT NULL DEFAULT 10000,
    description varchar(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_email_account PRIMARY KEY (id),
    CONSTRAINT uk_sys_email_account_number UNIQUE (number),
    CONSTRAINT ck_sys_email_account_security CHECK (security_mode IN ('NONE', 'STARTTLS', 'SSL_TLS')),
    CONSTRAINT ck_sys_email_account_port CHECK (port BETWEEN 1 AND 65535),
    CONSTRAINT ck_sys_email_account_timeout CHECK (connection_timeout_ms BETWEEN 1000 AND 60000 AND read_timeout_ms BETWEEN 1000 AND 60000)
);
CREATE UNIQUE INDEX uk_sys_email_account_default ON public.t_sys_email_account (default_account) WHERE default_account;

CREATE TABLE public.t_sys_email_task (
    id bigint NOT NULL,
    scene_key varchar(100) NOT NULL,
    idempotency_key varchar(200) NOT NULL,
    source_task_id bigint,
    account_id bigint NOT NULL,
    account_number varchar(64) NOT NULL,
    from_address varchar(320) NOT NULL,
    from_name varchar(100),
    to_addresses text NOT NULL,
    cc_addresses text,
    bcc_addresses text,
    subject varchar(300) NOT NULL,
    html_body text NOT NULL,
    text_body text,
    status varchar(20) NOT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    max_attempts integer NOT NULL DEFAULT 3,
    next_attempt_time timestamp without time zone,
    started_time timestamp without time zone,
    completed_time timestamp without time zone,
    error_category varchar(50),
    error_message varchar(1000),
    trace_id varchar(64),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_email_task PRIMARY KEY (id),
    CONSTRAINT uk_sys_email_task_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_sys_email_task_account FOREIGN KEY (account_id) REFERENCES public.t_sys_email_account(id),
    CONSTRAINT fk_sys_email_task_source FOREIGN KEY (source_task_id) REFERENCES public.t_sys_email_task(id),
    CONSTRAINT ck_sys_email_task_status CHECK (status IN ('PENDING','SENDING','SUCCESS','RETRY_WAIT','FAILED','UNKNOWN','CANCELLED')),
    CONSTRAINT ck_sys_email_task_attempt CHECK (attempt_count >= 0 AND max_attempts BETWEEN 1 AND 10)
);
CREATE INDEX idx_sys_email_task_dispatch ON public.t_sys_email_task (status, next_attempt_time, create_time);
CREATE INDEX idx_sys_email_task_account ON public.t_sys_email_task (account_id, create_time DESC);

CREATE TABLE public.t_sys_email_attempt (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    attempt_no integer NOT NULL,
    status varchar(20) NOT NULL,
    started_time timestamp without time zone NOT NULL,
    completed_time timestamp without time zone,
    error_category varchar(50),
    error_message varchar(1000),
    instance_id varchar(100),
    trace_id varchar(64),
    create_time timestamp without time zone,
    CONSTRAINT pk_sys_email_attempt PRIMARY KEY (id),
    CONSTRAINT fk_sys_email_attempt_task FOREIGN KEY (task_id) REFERENCES public.t_sys_email_task(id),
    CONSTRAINT uk_sys_email_attempt_no UNIQUE (task_id, attempt_no),
    CONSTRAINT ck_sys_email_attempt_status CHECK (status IN ('SENDING','SUCCESS','FAILED','UNKNOWN'))
);

COMMENT ON TABLE public.t_sys_email_account IS 'SMTP 发信账号';
COMMENT ON COLUMN public.t_sys_email_account.password_cipher IS 'SMTP 密码或授权码的 SM4/GCM 密文';
COMMENT ON COLUMN public.t_sys_email_account.default_account IS '是否为全局默认发信账号';
COMMENT ON COLUMN public.t_sys_email_account.allow_manual IS '是否允许管理员手工选择';
COMMENT ON TABLE public.t_sys_email_task IS '邮件持久化投递任务';
COMMENT ON COLUMN public.t_sys_email_task.idempotency_key IS '业务幂等键';
COMMENT ON COLUMN public.t_sys_email_task.to_addresses IS 'JSON 格式收件人地址快照';
COMMENT ON COLUMN public.t_sys_email_task.status IS 'PENDING/SENDING/SUCCESS/RETRY_WAIT/FAILED/UNKNOWN/CANCELLED';
COMMENT ON TABLE public.t_sys_email_attempt IS '邮件每次 SMTP 投递尝试记录';

INSERT INTO public.t_sys_job
    (id, job_name, job_group, description, job_class_name, cron_expression, job_data, status,
     number, is_system, version, mutex_key)
VALUES
    (470000000000001301, '邮件投递派发', 'SYSTEM', '处理持久化邮件任务并执行有限重试',
     'sm.domain.sys.scheduler.job.DispatchEmailJob', '0/15 * * * * ?', '{"batchSize":20}',
     'ENABLED', 'SYSTEM_EMAIL_DISPATCH', true, 0, 'system-email-dispatch');

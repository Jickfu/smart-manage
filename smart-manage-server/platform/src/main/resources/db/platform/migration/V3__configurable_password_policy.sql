-- 密码策略仅作用于后续凭据写入，保留已有密码及默认校验行为。
CREATE TABLE public.t_sys_password_config (
    id bigint PRIMARY KEY CHECK (id = 1),
    min_length integer NOT NULL CHECK (min_length BETWEEN 4 AND 64),
    complexity_enabled boolean NOT NULL,
    version integer NOT NULL DEFAULT 0,
    create_time timestamp DEFAULT now(),
    update_time timestamp,
    create_user bigint,
    update_user bigint
);
COMMENT ON TABLE public.t_sys_password_config IS '全系统密码策略单例';
COMMENT ON COLUMN public.t_sys_password_config.id IS 'ID';
COMMENT ON COLUMN public.t_sys_password_config.min_length IS '密码最小长度';
COMMENT ON COLUMN public.t_sys_password_config.complexity_enabled IS '是否启用四类字符复杂度';
COMMENT ON COLUMN public.t_sys_password_config.version IS '乐观锁版本';
COMMENT ON COLUMN public.t_sys_password_config.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_password_config.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_password_config.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_password_config.update_user IS '修改人';
INSERT INTO public.t_sys_password_config (id, min_length, complexity_enabled) VALUES (1, 15, false);

INSERT INTO public.t_sys_feature (id, feature_key, app_id, default_name, default_seq, description)
VALUES (560000000000000200, 'sys/base/password-config', 31, '密码策略', 136, '配置全系统新密码的长度和复杂度要求');
INSERT INTO public.t_sys_permission (id, name, number, feature_id) VALUES
(560000000000000201, '密码策略-查看', 'sys:base:password-config:detail', 560000000000000200),
(560000000000000202, '密码策略-保存', 'sys:base:password-config:save', 560000000000000200);
INSERT INTO public.t_sys_menu (id, number, name, level, parent_id, app_id, permission_id, path, component, sort, feature_id, target_type, icon)
VALUES (560000000000000210, 'password_config', '密码策略', 1, 470000000000000005, 31, 560000000000000201,
'/sys/base/password-config', 'sys/base/password-config', 61, 560000000000000200, 'INTERNAL_PAGE', 'LockOutlined');
INSERT INTO public.t_sys_feature_data_object
(id, feature_id, schema_name, object_name, object_type, object_role, display_name, seq)
VALUES (560000000000000220, 560000000000000200, 'public', 't_sys_password_config', 'TABLE', 'PRIMARY', '密码策略', 10);

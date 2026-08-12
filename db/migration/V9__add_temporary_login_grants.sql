ALTER TABLE public.t_sys_login_log
    ADD COLUMN issuer_user_id bigint,
    ADD COLUMN grant_id varchar(64),
    ADD COLUMN grant_reason varchar(500),
    ADD COLUMN grant_expires_at timestamp without time zone;

ALTER TABLE public.t_sys_login_log_history
    ADD COLUMN issuer_user_id bigint,
    ADD COLUMN grant_id varchar(64),
    ADD COLUMN grant_reason varchar(500),
    ADD COLUMN grant_expires_at timestamp without time zone;

COMMENT ON COLUMN public.t_sys_login_log.issuer_user_id IS '代登录凭证签发管理员ID';
COMMENT ON COLUMN public.t_sys_login_log.grant_id IS '代登录授权编号';
COMMENT ON COLUMN public.t_sys_login_log.grant_reason IS '代登录原因';
COMMENT ON COLUMN public.t_sys_login_log.grant_expires_at IS '代登录凭证计划失效时间';
COMMENT ON COLUMN public.t_sys_login_log_history.issuer_user_id IS '代登录凭证签发管理员ID';
COMMENT ON COLUMN public.t_sys_login_log_history.grant_id IS '代登录授权编号';
COMMENT ON COLUMN public.t_sys_login_log_history.grant_reason IS '代登录原因';
COMMENT ON COLUMN public.t_sys_login_log_history.grant_expires_at IS '代登录凭证计划失效时间';

INSERT INTO public.t_sys_permission
    (id, name, number, app_id, feature_id, create_time, version)
SELECT 420000000000001024,
       '用户管理-生成代登录凭证',
       'sys:base:user:temporaryLogin',
       NULL,
       id,
       CURRENT_TIMESTAMP,
       0
FROM public.t_sys_feature
WHERE feature_key = 'sys/base/user';

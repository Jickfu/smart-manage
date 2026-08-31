ALTER TABLE public.t_sys_openapi_grant
    ADD COLUMN update_time timestamp without time zone,
    ADD COLUMN update_user bigint;

COMMENT ON COLUMN public.t_sys_openapi_grant.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_openapi_grant.update_user IS '更新用户';

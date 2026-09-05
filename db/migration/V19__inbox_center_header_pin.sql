-- 全局消息中心参与个人页签固定，不创建虚构业务应用或授予消息发布权限。
ALTER TABLE public.t_sys_user_app_pin ALTER COLUMN app_id DROP NOT NULL;
ALTER TABLE public.t_sys_user_app_pin ADD COLUMN builtin_key varchar(30);
COMMENT ON COLUMN public.t_sys_user_app_pin.builtin_key IS '内置全局页签键，仅支持消息中心';
ALTER TABLE public.t_sys_user_app_pin ADD CONSTRAINT ck_user_app_pin_target
    CHECK ((app_id IS NOT NULL AND builtin_key IS NULL)
        OR (app_id IS NULL AND builtin_key IS NOT NULL AND builtin_key = 'builtin:inbox'));
CREATE UNIQUE INDEX uk_user_app_pin_builtin ON public.t_sys_user_app_pin(user_id, builtin_key)
    WHERE builtin_key IS NOT NULL;
ALTER TABLE public.t_sys_app ADD CONSTRAINT ck_app_number_builtin_inbox
    CHECK (number <> 'builtin:inbox');

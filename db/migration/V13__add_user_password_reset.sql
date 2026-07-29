ALTER TABLE public.t_sys_user
    ADD COLUMN password_reset boolean DEFAULT true NOT NULL;

COMMENT ON COLUMN public.t_sys_user.password_reset IS '是否必须修改密码';

-- 既有密码并非临时密码，不改变现有账号的登录行为；新建用户仍使用字段默认值 true。
UPDATE public.t_sys_user SET password_reset = false;

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, version)
VALUES (420000000000001013, '用户管理-重置密码', 'sys:base:user:resetPassword', 31, CURRENT_TIMESTAMP, 0);

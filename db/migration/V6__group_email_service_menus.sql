-- 消息服务按渠道建立独立菜单分组，邮件配置、操作和记录由邮件能力维护者共同管理。
INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, icon, description,
     sort, enabled, version, feature_id, target_type)
VALUES
    (470000000000001200, 'email_service', '邮件服务', 0, 0, 470000000000001000,
     470000000000001100, 'MailOutlined', 'SMTP 发信账号、邮件发送与投递记录',
     10, true, 0, NULL, NULL);

UPDATE public.t_sys_menu
SET parent_id = 470000000000001200,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id IN (470000000000001201, 470000000000001202, 470000000000001203);

-- 个人消息中心是全体登录用户的全局能力，不归属管理员消息发布功能。
INSERT INTO public.t_sys_param
    (id, number, name, value, description, is_system, version, feature_id, create_time)
VALUES
    (520000000000000001, 'INBOX_POLL_INTERVAL_SECONDS', '消息轮询间隔（秒）', '60',
     '0关闭轮询；启用时为10～2147483秒的整数，默认60秒。配置随下次刷新生效；关闭后可重新打开消息侧栏或聚焦窗口读取新配置。',
     true, 0, NULL, now());

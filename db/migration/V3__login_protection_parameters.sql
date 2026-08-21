-- 登录保护运行参数由系统参数统一维护，多实例通过远程缓存失效同步生效。
INSERT INTO public.t_sys_param
    (id, number, name, value, description, create_time, is_system, version, app_id)
VALUES
    (480000000000000001, 'LOGIN_CAPTCHA_CHALLENGE_EXPIRE_SECONDS', '滑块挑战有效秒数', '120', '必须为正整数；建议 60～300 秒，过短会影响操作，过长会扩大挑战重放窗口', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000002, 'LOGIN_CAPTCHA_TICKET_EXPIRE_SECONDS', '滑块票据有效秒数', '90', '必须为正整数；建议 30～180 秒，票据只能消费一次', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000003, 'LOGIN_CAPTCHA_MIN_INTERVAL_MILLIS', '同一IP获取滑块最小间隔毫秒数', '1000', '必须为正整数；建议 500～5000 毫秒，用于限制高频图片生成', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000004, 'LOGIN_CAPTCHA_IP_MAX_PER_MINUTE', '同一IP每分钟最多创建滑块数', '10', '必须为正整数；内网共享出口可根据实际并发适当调高', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000005, 'LOGIN_FAILURE_WINDOW_MINUTES', '登录失败统计窗口分钟数', '10', '必须为正整数；账号、IP及账号IP组合共用该统计窗口', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000006, 'LOGIN_ACCOUNT_MAX_FAILURES', '账号失败触发短时保护次数', '10', '必须为正整数；达到后进入短时账号保护，避免设置过低造成恶意阻断', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000007, 'LOGIN_ACCOUNT_BLOCK_SECONDS', '账号短时保护秒数', '60', '必须为正整数；建议使用短时保护，禁止配置成长时间账号锁定', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000008, 'LOGIN_IP_MAX_FAILURES', 'IP失败触发保护次数', '30', '必须为正整数；内网共享出口应使用高于账号IP组合的阈值', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000009, 'LOGIN_IP_BLOCK_MINUTES', 'IP保护分钟数', '5', '必须为正整数；共享出口环境修改前应评估同网用户影响', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000010, 'LOGIN_ACCOUNT_IP_MAX_FAILURES', '账号IP组合失败触发保护次数', '5', '必须为正整数；用于优先限制单一来源对单一账号的连续尝试', CURRENT_TIMESTAMP, true, 0, 31),
    (480000000000000011, 'LOGIN_ACCOUNT_IP_BLOCK_MINUTES', '账号IP组合保护分钟数', '10', '必须为正整数；只限制当前账号与当前客户端IP组合', CURRENT_TIMESTAMP, true, 0, 31);

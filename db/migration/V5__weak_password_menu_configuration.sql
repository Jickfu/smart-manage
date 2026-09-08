-- 弱口令属于全系统密码安全配置，只调整导航归组，保留词库、功能和权限身份。
UPDATE t_sys_menu
SET parent_id = 470000000000000005,
    sort = 60,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 560000000000000110;

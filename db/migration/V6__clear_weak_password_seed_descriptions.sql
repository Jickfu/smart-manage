-- 清除初始化占位文案，保留管理员填写的真实说明和词条匹配身份。
UPDATE t_sys_weak_password
SET description = NULL,
    version = version + 1,
    update_time = CURRENT_TIMESTAMP
WHERE description = '初始弱口令词库';

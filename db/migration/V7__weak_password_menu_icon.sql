-- 为内置弱口令管理补齐密码锁图标，保留管理员已经自定义的图标。
UPDATE t_sys_menu
SET icon = 'LockOutlined',
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = 560000000000000110
  AND (icon IS NULL OR btrim(icon) = '');

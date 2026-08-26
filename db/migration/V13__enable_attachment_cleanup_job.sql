-- 将历史占位任务收敛为系统内置附件对象清理任务，持续重试过期临时对象和待删除对象。
UPDATE t_sys_job
SET job_name = '附件对象清理',
    job_group = 'SYSTEM',
    description = '清理过期临时附件，并重试数据库已标记待删除的对象',
    cron_expression = '0 0/30 * * * ?',
    job_data = '{}',
    status = 'ENABLED',
    update_time = CURRENT_TIMESTAMP,
    update_user = NULL,
    number = 'ATTACHMENT_OBJECT_CLEANUP',
    is_system = true,
    version = version + 1,
    mutex_key = 'attachment-object-cleanup'
WHERE id = 2082857218823630850
  AND job_class_name = 'sm.domain.sys.scheduler.job.CleanTempFileJob';

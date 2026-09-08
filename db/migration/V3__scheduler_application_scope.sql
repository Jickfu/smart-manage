-- 开发阶段模型调整：升级前须停止调度实例并备份数据库。本版本明确清空旧执行记录。
TRUNCATE TABLE t_sys_job_log, t_sys_job_log_history;

ALTER TABLE t_sys_job ADD COLUMN app_id bigint;
UPDATE t_sys_job AS task
SET app_id = app.id
FROM (VALUES
    ('ATTACHMENT_OBJECT_CLEANUP', 'base'),
    ('SYSTEM_EMAIL_DISPATCH', 'message'),
    ('SYSTEM_INBOX_MESSAGE_DISPATCH', 'message'),
    ('SYSTEM_LOG_ARCHIVE', 'monitor'),
    ('SYSTEM_LOG_HISTORY_PURGE', 'monitor')
) AS ownership(number, app_number)
JOIN t_sys_app AS app ON app.number = ownership.app_number
WHERE task.number = ownership.number;

-- 不猜测未知任务的业务归属；有扩展任务的开发库必须先明确映射后才能升级。
ALTER TABLE t_sys_job ALTER COLUMN app_id SET NOT NULL;
ALTER TABLE t_sys_job ADD CONSTRAINT fk_sys_job_app FOREIGN KEY (app_id) REFERENCES t_sys_app(id);
COMMENT ON COLUMN t_sys_job.app_id IS '所属应用ID，领域由应用推导';
CREATE INDEX idx_sys_job_app ON t_sys_job(app_id);

-- 只清除本系统统一 Dispatcher 的调度项，后续启动按任务 ID 重建全部期望任务。
-- Quartz 原生 group 字段仍然存在；移除的只是业务模型中的自由分组。
CREATE TEMPORARY TABLE scheduler_rebuild_keys ON COMMIT DROP AS
SELECT sched_name, job_name, job_group
FROM qrtz_job_details
WHERE job_class_name = 'sm.domain.sys.scheduler.service.ManagedJobDispatcher';

CREATE TEMPORARY TABLE scheduler_rebuild_triggers ON COMMIT DROP AS
SELECT trigger.sched_name, trigger.trigger_name, trigger.trigger_group
FROM qrtz_triggers AS trigger
JOIN scheduler_rebuild_keys AS job
  ON job.sched_name = trigger.sched_name AND job.job_name = trigger.job_name AND job.job_group = trigger.job_group;

DELETE FROM qrtz_cron_triggers AS trigger USING scheduler_rebuild_triggers AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.trigger_name = stale.trigger_name AND trigger.trigger_group = stale.trigger_group;
DELETE FROM qrtz_simple_triggers AS trigger USING scheduler_rebuild_triggers AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.trigger_name = stale.trigger_name AND trigger.trigger_group = stale.trigger_group;
DELETE FROM qrtz_simprop_triggers AS trigger USING scheduler_rebuild_triggers AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.trigger_name = stale.trigger_name AND trigger.trigger_group = stale.trigger_group;
DELETE FROM qrtz_blob_triggers AS trigger USING scheduler_rebuild_triggers AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.trigger_name = stale.trigger_name AND trigger.trigger_group = stale.trigger_group;
DELETE FROM qrtz_triggers AS trigger USING scheduler_rebuild_triggers AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.trigger_name = stale.trigger_name AND trigger.trigger_group = stale.trigger_group;
DELETE FROM qrtz_fired_triggers AS trigger USING scheduler_rebuild_keys AS stale
WHERE trigger.sched_name = stale.sched_name AND trigger.job_name = stale.job_name AND trigger.job_group = stale.job_group;
DELETE FROM qrtz_paused_trigger_grps AS paused
WHERE EXISTS (SELECT 1 FROM scheduler_rebuild_triggers AS stale
              WHERE stale.sched_name = paused.sched_name AND stale.trigger_group = paused.trigger_group)
  AND NOT EXISTS (SELECT 1 FROM qrtz_triggers AS remaining
                  WHERE remaining.sched_name = paused.sched_name AND remaining.trigger_group = paused.trigger_group);
DELETE FROM qrtz_job_details AS job USING scheduler_rebuild_keys AS stale
WHERE job.sched_name = stale.sched_name AND job.job_name = stale.job_name AND job.job_group = stale.job_group;

DROP INDEX idx_sys_job_name_group;
ALTER TABLE t_sys_job DROP COLUMN job_group;

ALTER TABLE t_sys_job_log
    DROP COLUMN job_group,
    ADD COLUMN domain_id bigint NOT NULL,
    ADD COLUMN domain_name varchar(255) NOT NULL,
    ADD COLUMN app_id bigint NOT NULL,
    ADD COLUMN app_name varchar(255) NOT NULL;
ALTER TABLE t_sys_job_log_history
    DROP COLUMN job_group,
    ADD COLUMN domain_id bigint NOT NULL,
    ADD COLUMN domain_name varchar(255) NOT NULL,
    ADD COLUMN app_id bigint NOT NULL,
    ADD COLUMN app_name varchar(255) NOT NULL;

-- 快照不建立到当前目录的外键，确保任务及目录删除后记录仍可查询与转储。
COMMENT ON COLUMN t_sys_job_log.domain_id IS '执行时所属领域ID';
COMMENT ON COLUMN t_sys_job_log.domain_name IS '执行时所属领域名称';
COMMENT ON COLUMN t_sys_job_log.app_id IS '执行时所属应用ID';
COMMENT ON COLUMN t_sys_job_log.app_name IS '执行时所属应用名称';
COMMENT ON COLUMN t_sys_job_log_history.domain_id IS '执行时所属领域ID';
COMMENT ON COLUMN t_sys_job_log_history.domain_name IS '执行时所属领域名称';
COMMENT ON COLUMN t_sys_job_log_history.app_id IS '执行时所属应用ID';
COMMENT ON COLUMN t_sys_job_log_history.app_name IS '执行时所属应用名称';
CREATE INDEX idx_sys_job_log_domain_start ON t_sys_job_log(domain_id, start_time DESC, id DESC);
CREATE INDEX idx_sys_job_log_app_start ON t_sys_job_log(app_id, start_time DESC, id DESC);
CREATE INDEX idx_sys_job_log_history_domain_start ON t_sys_job_log_history(domain_id, start_time DESC, id DESC);
CREATE INDEX idx_sys_job_log_history_app_start ON t_sys_job_log_history(app_id, start_time DESC, id DESC);

-- 将任务调度从只读观测性质的系统监控中拆分为独立应用。
INSERT INTO public.t_sys_app
    (id, name, number, icon, seq, description, cloud_id, enabled, create_time, icon_color, version)
VALUES
    (32, '任务调度', 'scheduler', 'ClockCircleOutlined', 3,
     '定时任务定义与执行实例管理', 4, true, CURRENT_TIMESTAMP, '#1677ff', 0);

UPDATE public.t_sys_permission
SET app_id = 32,
    number = CASE number
        WHEN 'sys:monitor:job:listPage' THEN 'sys:scheduler:job:listPage'
        WHEN 'sys:monitor:job:detail' THEN 'sys:scheduler:job:detail'
        WHEN 'sys:monitor:job:save' THEN 'sys:scheduler:job:save'
        WHEN 'sys:monitor:job:delete' THEN 'sys:scheduler:job:delete'
        WHEN 'sys:monitor:job-log:listPage' THEN 'sys:scheduler:execution:listPage'
        WHEN 'sys:monitor:job:category' THEN 'sys:scheduler:category'
    END,
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE number IN (
    'sys:monitor:job:listPage',
    'sys:monitor:job:detail',
    'sys:monitor:job:save',
    'sys:monitor:job:delete',
    'sys:monitor:job-log:listPage',
    'sys:monitor:job:category'
);

INSERT INTO public.t_sys_permission
    (id, name, number, app_id, create_time, version)
VALUES
    (420000000000001016, '执行实例-详情', 'sys:scheduler:execution:detail', 32, CURRENT_TIMESTAMP, 0);

-- 已具备执行实例列表权限的角色继续获得详情权限。
WITH granted_roles AS (
    SELECT DISTINCT role_permission.role_id
    FROM public.t_sys_role_perms role_permission
    JOIN public.t_sys_permission permission
        ON permission.id = role_permission.permission_id
    WHERE permission.number = 'sys:scheduler:execution:listPage'
),
current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms
    (id, role_id, permission_id, create_time)
SELECT
    current_max.id + row_number() OVER (ORDER BY granted_roles.role_id),
    granted_roles.role_id,
    detail_permission.id,
    CURRENT_TIMESTAMP
FROM granted_roles
CROSS JOIN current_max
JOIN public.t_sys_permission detail_permission
    ON detail_permission.number = 'sys:scheduler:execution:detail';

UPDATE public.t_sys_menu
SET app_id = 32,
    path = CASE id
        WHEN 413260828563165184 THEN '/sys/scheduler/job'
        WHEN 413260828571553792 THEN '/sys/scheduler/execution'
        ELSE path
    END,
    component = CASE id
        WHEN 413260828563165184 THEN 'sys/scheduler/job'
        WHEN 413260828571553792 THEN 'sys/scheduler/execution'
        ELSE component
    END,
    update_time = CURRENT_TIMESTAMP
WHERE id IN (5001, 413260828563165184, 413260828571553792);

ALTER TABLE public.t_sys_job
    ADD COLUMN version integer NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.t_sys_job.version IS '乐观锁版本号';

-- 支撑按任务批量读取最近一次执行实例。
CREATE INDEX idx_sys_job_log_job_start
    ON public.t_sys_job_log (job_id, start_time DESC, id DESC);

-- 内置任务实现随领域包迁移；不保留旧类名兼容入口。
UPDATE public.t_sys_job
SET job_class_name = replace(
        job_class_name,
        'sm.domain.sys.monitor.job.',
        'sm.domain.sys.scheduler.'
    ),
    update_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE job_class_name LIKE 'sm.domain.sys.monitor.job.%';

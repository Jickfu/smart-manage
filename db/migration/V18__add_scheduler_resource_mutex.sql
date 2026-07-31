ALTER TABLE public.t_sys_job
    ADD COLUMN mutex_key varchar(100);

COMMENT ON COLUMN public.t_sys_job.mutex_key IS
    '共享资源互斥键；不同任务使用相同键时，同一时刻只执行一个';

CREATE INDEX idx_sys_job_mutex_key
    ON public.t_sys_job (mutex_key)
    WHERE mutex_key IS NOT NULL;

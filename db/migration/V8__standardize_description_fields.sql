ALTER TABLE public.t_sys_role
    ADD COLUMN description character varying(500);

ALTER TABLE public.t_sys_basic_data_category
    RENAME COLUMN remark TO description;

ALTER TABLE public.t_sys_basic_data_item
    RENAME COLUMN remark TO description;

ALTER TABLE public.t_sys_param
    RENAME COLUMN remark TO description;

ALTER TABLE public.t_sys_script
    RENAME COLUMN remark TO description;

-- 当前任务维护接口只写 remark，迁移时以现用字段为准，避免旧 description 覆盖用户数据。
UPDATE public.t_sys_job
SET description = COALESCE(NULLIF(remark, ''), description)
WHERE remark IS NOT NULL;

ALTER TABLE public.t_sys_job
    DROP COLUMN remark;

COMMENT ON COLUMN public.t_sys_role.description IS '描述';
COMMENT ON COLUMN public.t_sys_basic_data_category.description IS '描述';
COMMENT ON COLUMN public.t_sys_basic_data_item.description IS '描述';
COMMENT ON COLUMN public.t_sys_param.description IS '描述';
COMMENT ON COLUMN public.t_sys_script.description IS '描述';
COMMENT ON COLUMN public.t_sys_job.description IS '描述';

UPDATE public.t_sys_basic_data_entry
SET sort = 0
WHERE sort IS NULL;

ALTER TABLE public.t_sys_basic_data_entry
    ALTER COLUMN sort SET DEFAULT 0,
    ALTER COLUMN sort SET NOT NULL;

COMMENT ON TABLE public.t_sys_basic_data IS
    '系统级可配置选项集；不用于承载具有独立生命周期的业务主数据';
COMMENT ON TABLE public.t_sys_basic_data_entry IS
    '基础数据选项明细，生命周期从属于基础数据主表';
COMMENT ON COLUMN public.t_sys_basic_data_entry.number IS '选项编码，在同一基础数据下唯一';
COMMENT ON COLUMN public.t_sys_basic_data_entry.name IS '选项显示名称';

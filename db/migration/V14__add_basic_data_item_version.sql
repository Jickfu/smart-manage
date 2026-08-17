ALTER TABLE public.t_sys_basic_data_item
    ADD COLUMN version integer DEFAULT 0 NOT NULL;

COMMENT ON COLUMN public.t_sys_basic_data_item.version IS '乐观锁版本号';

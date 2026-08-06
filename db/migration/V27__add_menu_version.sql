ALTER TABLE public.t_sys_menu
    ADD COLUMN version integer NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.t_sys_menu.version IS '乐观锁版本';

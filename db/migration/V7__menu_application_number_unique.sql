ALTER TABLE public.t_sys_menu
    ADD CONSTRAINT uk_sys_menu_app_number UNIQUE (app_id, number);

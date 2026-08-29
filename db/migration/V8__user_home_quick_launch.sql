CREATE TABLE public.t_sys_user_home_quick_launch (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    scope_type character varying(16) NOT NULL,
    app_id bigint,
    menu_id bigint NOT NULL,
    seq integer NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT pk_sys_user_home_quick_launch PRIMARY KEY (id),
    CONSTRAINT ck_sys_user_home_quick_launch_scope CHECK (
        (scope_type = 'SYSTEM' AND app_id IS NULL)
        OR (scope_type = 'APPLICATION' AND app_id IS NOT NULL)
    ),
    CONSTRAINT uk_sys_user_home_quick_launch UNIQUE NULLS NOT DISTINCT
        (user_id, scope_type, app_id, menu_id),
    CONSTRAINT fk_sys_user_home_quick_launch_user FOREIGN KEY (user_id)
        REFERENCES public.t_sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_home_quick_launch_app FOREIGN KEY (app_id)
        REFERENCES public.t_sys_app(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_home_quick_launch_menu FOREIGN KEY (menu_id)
        REFERENCES public.t_sys_menu(id) ON DELETE CASCADE
);

CREATE INDEX idx_sys_user_home_quick_launch_scope_seq
    ON public.t_sys_user_home_quick_launch (user_id, scope_type, app_id, seq, id);

COMMENT ON TABLE public.t_sys_user_home_quick_launch IS '用户首页快速发起配置';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.id IS 'ID';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.user_id IS '用户ID';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.scope_type IS '首页范围：SYSTEM、APPLICATION';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.app_id IS '应用ID，系统首页为空';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.menu_id IS '菜单ID';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.seq IS '排序号';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_user_home_quick_launch.update_user IS '修改人';

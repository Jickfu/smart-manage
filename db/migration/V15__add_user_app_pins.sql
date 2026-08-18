CREATE TABLE public.t_sys_user_app_pin (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    app_id bigint NOT NULL,
    seq integer NOT NULL,
    create_time timestamp without time zone NOT NULL,
    create_user bigint,
    update_time timestamp without time zone,
    update_user bigint,
    CONSTRAINT pk_sys_user_app_pin PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_app_pin UNIQUE (user_id, app_id),
    CONSTRAINT fk_sys_user_app_pin_user FOREIGN KEY (user_id)
        REFERENCES public.t_sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_app_pin_app FOREIGN KEY (app_id)
        REFERENCES public.t_sys_app(id) ON DELETE CASCADE
);

CREATE INDEX idx_sys_user_app_pin_user_seq
    ON public.t_sys_user_app_pin(user_id, seq, id);

COMMENT ON TABLE public.t_sys_user_app_pin IS '用户固定应用';
COMMENT ON COLUMN public.t_sys_user_app_pin.id IS 'ID';
COMMENT ON COLUMN public.t_sys_user_app_pin.user_id IS '用户ID';
COMMENT ON COLUMN public.t_sys_user_app_pin.app_id IS '应用ID';
COMMENT ON COLUMN public.t_sys_user_app_pin.seq IS '排序号';
COMMENT ON COLUMN public.t_sys_user_app_pin.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_user_app_pin.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_user_app_pin.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_user_app_pin.update_user IS '修改人';

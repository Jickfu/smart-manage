ALTER TABLE public.t_sys_user
    ADD COLUMN name character varying(255),
    ADD COLUMN number character varying(100),
    ADD COLUMN gender character varying(10),
    ADD COLUMN birthday date,
    ADD COLUMN avatar_attachment_id bigint;

UPDATE public.t_sys_user
SET name = COALESCE(NULLIF(BTRIM(nickname), ''), username),
    number = username;

ALTER TABLE public.t_sys_user
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN number SET NOT NULL,
    DROP COLUMN nickname,
    DROP COLUMN avatar,
    ADD CONSTRAINT fk_sys_user_avatar_attachment FOREIGN KEY (avatar_attachment_id) REFERENCES public.t_sys_attachment(id),
    ADD CONSTRAINT ck_sys_user_gender CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE'));

CREATE UNIQUE INDEX uk_sys_user_number ON public.t_sys_user(number);

COMMENT ON COLUMN public.t_sys_user.name IS '姓名';
COMMENT ON COLUMN public.t_sys_user.number IS '工号';
COMMENT ON COLUMN public.t_sys_user.gender IS '性别';
COMMENT ON COLUMN public.t_sys_user.birthday IS '生日';
COMMENT ON COLUMN public.t_sys_user.avatar_attachment_id IS '头像附件ID';

CREATE TABLE public.t_sys_user_assignment (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    org_id bigint NOT NULL,
    position character varying(200) NOT NULL,
    is_org_leader boolean DEFAULT false NOT NULL,
    is_primary boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    update_time timestamp without time zone DEFAULT now(),
    update_user bigint,
    CONSTRAINT pk_sys_user_assignment PRIMARY KEY (id),
    CONSTRAINT fk_sys_user_assignment_user FOREIGN KEY (user_id) REFERENCES public.t_sys_user(id),
    CONSTRAINT fk_sys_user_assignment_org FOREIGN KEY (org_id) REFERENCES public.t_sys_org(id),
    CONSTRAINT uk_sys_user_assignment_user_org UNIQUE (user_id, org_id)
);

CREATE UNIQUE INDEX uk_sys_user_assignment_primary
    ON public.t_sys_user_assignment(user_id)
    WHERE is_primary;
CREATE INDEX idx_sys_user_assignment_org_user
    ON public.t_sys_user_assignment(org_id, user_id);

COMMENT ON TABLE public.t_sys_user_assignment IS '用户部门任职';
COMMENT ON COLUMN public.t_sys_user_assignment.id IS 'ID';
COMMENT ON COLUMN public.t_sys_user_assignment.user_id IS '用户ID';
COMMENT ON COLUMN public.t_sys_user_assignment.org_id IS '组织ID';
COMMENT ON COLUMN public.t_sys_user_assignment.position IS '职位';
COMMENT ON COLUMN public.t_sys_user_assignment.is_org_leader IS '是否为部门负责人';
COMMENT ON COLUMN public.t_sys_user_assignment.is_primary IS '是否为主职';
COMMENT ON COLUMN public.t_sys_user_assignment.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_user_assignment.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_user_assignment.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_user_assignment.update_user IS '修改人';

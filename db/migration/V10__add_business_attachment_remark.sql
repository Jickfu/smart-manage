ALTER TABLE public.t_sys_biz_attachment
    ADD COLUMN remark character varying(500);

COMMENT ON COLUMN public.t_sys_biz_attachment.remark IS '附件备注';

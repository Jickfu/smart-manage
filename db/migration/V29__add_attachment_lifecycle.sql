ALTER TABLE public.t_sys_attachment
    ADD COLUMN object_key text,
    ADD COLUMN status varchar(30),
    ADD COLUMN upload_session_id varchar(64),
    ADD COLUMN expires_at timestamp without time zone,
    ADD COLUMN sha256 varchar(64);

UPDATE public.t_sys_attachment
SET object_key = stored_path,
    status = CASE WHEN is_temp THEN 'TEMP' ELSE 'ACTIVE' END;

ALTER TABLE public.t_sys_attachment
    ALTER COLUMN object_key SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT ck_sys_attachment_status
        CHECK (status IN ('TEMP', 'ACTIVE', 'PENDING_DELETE', 'DELETED'));

CREATE UNIQUE INDEX uk_sys_attachment_object_key ON public.t_sys_attachment (object_key);
CREATE INDEX idx_sys_attachment_cleanup ON public.t_sys_attachment (status, expires_at);

COMMENT ON COLUMN public.t_sys_attachment.object_key IS '稳定对象键或兼容存储定位键';
COMMENT ON COLUMN public.t_sys_attachment.status IS '生命周期状态';
COMMENT ON COLUMN public.t_sys_attachment.upload_session_id IS '上传会话ID';
COMMENT ON COLUMN public.t_sys_attachment.expires_at IS '临时附件过期时间';
COMMENT ON COLUMN public.t_sys_attachment.sha256 IS '文件SHA-256摘要';

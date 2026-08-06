CREATE TABLE public.t_sys_attachment_config (
    id bigint NOT NULL,
    max_upload_bytes bigint NOT NULL,
    allowed_extensions varchar(2000) NOT NULL,
    allowed_mime_types varchar(2000) NOT NULL,
    temp_expire_hours integer NOT NULL,
    version integer NOT NULL DEFAULT 0,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT pk_sys_attachment_config PRIMARY KEY (id),
    CONSTRAINT ck_sys_attachment_config_max_upload_bytes CHECK (max_upload_bytes > 0),
    CONSTRAINT ck_sys_attachment_config_temp_expire_hours CHECK (temp_expire_hours BETWEEN 1 AND 168)
);

COMMENT ON TABLE public.t_sys_attachment_config IS '附件全局限制配置';
COMMENT ON COLUMN public.t_sys_attachment_config.id IS 'ID';
COMMENT ON COLUMN public.t_sys_attachment_config.max_upload_bytes IS '单文件最大字节数';
COMMENT ON COLUMN public.t_sys_attachment_config.allowed_extensions IS '允许扩展名，逗号分隔';
COMMENT ON COLUMN public.t_sys_attachment_config.allowed_mime_types IS '允许MIME类型，逗号分隔';
COMMENT ON COLUMN public.t_sys_attachment_config.temp_expire_hours IS '临时附件有效小时数';
COMMENT ON COLUMN public.t_sys_attachment_config.version IS '乐观锁版本号';
COMMENT ON COLUMN public.t_sys_attachment_config.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_attachment_config.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_attachment_config.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_attachment_config.update_user IS '修改人';

INSERT INTO public.t_sys_attachment_config
    (id, max_upload_bytes, allowed_extensions, allowed_mime_types, temp_expire_hours, create_time, version)
VALUES
    (420000000000001101, 20971520,
     'pdf,png,jpg,jpeg,gif,webp,doc,docx,xls,xlsx,ppt,pptx,txt',
     'application/pdf,image/png,image/jpeg,image/gif,image/webp,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,text/plain',
     24, CURRENT_TIMESTAMP, 0);

INSERT INTO public.t_sys_permission (id, name, number, app_id, create_time, version)
VALUES
    (420000000000001102, '附件配置-查看', 'sys:base:attachment-config:detail', 31, CURRENT_TIMESTAMP, 0),
    (420000000000001103, '附件配置-保存', 'sys:base:attachment-config:save', 31, CURRENT_TIMESTAMP, 0);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, create_time)
VALUES
    (420000000000001104, 'ATTACHMENT_CONFIG_PAGE', '附件配置', 3, 413196675785879552, 31,
     420000000000001102, '/sys/base/attachment-config', 'sys/base/attachment-config',
     'PaperClipOutlined', '统一管理附件上传限制和临时附件有效期', 20, true, CURRENT_TIMESTAMP);

WITH administrator_role AS (
    SELECT id FROM public.t_sys_role WHERE number = 'admin'
), attachment_permissions AS (
    SELECT id FROM public.t_sys_permission WHERE id IN (420000000000001102, 420000000000001103)
), current_max AS (
    SELECT COALESCE(max(id), 0) AS id FROM public.t_sys_role_perms
)
INSERT INTO public.t_sys_role_perms (id, role_id, permission_id, create_time)
SELECT current_max.id + row_number() OVER (ORDER BY attachment_permissions.id),
       administrator_role.id, attachment_permissions.id, CURRENT_TIMESTAMP
FROM administrator_role CROSS JOIN attachment_permissions CROSS JOIN current_max;

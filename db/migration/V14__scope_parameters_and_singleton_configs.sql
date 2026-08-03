ALTER TABLE public.t_sys_param
    ADD COLUMN app_id bigint;

COMMENT ON COLUMN public.t_sys_param.app_id IS '所属应用ID';

ALTER TABLE public.t_sys_param
    ADD CONSTRAINT fk_sys_param_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id);

CREATE INDEX idx_sys_param_app_id ON public.t_sys_param (app_id);
CREATE UNIQUE INDEX uk_sys_param_number ON public.t_sys_param (number);

ALTER TABLE public.t_sys_ui_config
    ADD COLUMN version integer DEFAULT 0 NOT NULL,
    ADD COLUMN login_banner_attachment_id bigint,
    ADD COLUMN login_logo_attachment_id bigint,
    ADD COLUMN header_logo_attachment_id bigint;

COMMENT ON COLUMN public.t_sys_ui_config.version IS '乐观锁版本号';
COMMENT ON COLUMN public.t_sys_ui_config.login_banner_attachment_id IS '登录页Banner附件ID';
COMMENT ON COLUMN public.t_sys_ui_config.login_logo_attachment_id IS '登录页Logo附件ID';
COMMENT ON COLUMN public.t_sys_ui_config.header_logo_attachment_id IS '顶部Logo附件ID';

ALTER TABLE public.t_sys_ui_config
    ADD CONSTRAINT fk_ui_config_login_banner_attachment
        FOREIGN KEY (login_banner_attachment_id) REFERENCES public.t_sys_attachment(id),
    ADD CONSTRAINT fk_ui_config_login_logo_attachment
        FOREIGN KEY (login_logo_attachment_id) REFERENCES public.t_sys_attachment(id),
    ADD CONSTRAINT fk_ui_config_header_logo_attachment
        FOREIGN KEY (header_logo_attachment_id) REFERENCES public.t_sys_attachment(id);

ALTER TABLE public.t_sys_file_config
    ADD COLUMN version integer DEFAULT 0 NOT NULL;

COMMENT ON COLUMN public.t_sys_file_config.version IS '乐观锁版本号';

DO $$
BEGIN
    IF (SELECT count(*) FROM public.t_sys_ui_config) > 1 THEN
        RAISE EXCEPTION '界面配置存在多条记录，无法收敛为单例，请先人工确认保留项';
    END IF;
    IF (SELECT count(*) FROM public.t_sys_file_config) > 1 THEN
        RAISE EXCEPTION '文件配置存在多条记录，无法收敛为单例，请先人工确认保留项';
    END IF;
END
$$;

CREATE UNIQUE INDEX uk_sys_ui_config_singleton ON public.t_sys_ui_config ((true));
CREATE UNIQUE INDEX uk_sys_file_config_singleton ON public.t_sys_file_config ((true));

-- 单例配置页以 detail 作为查看权限；保留既有角色的可访问性后再切换菜单权限。
DELETE FROM public.t_sys_role_perms list_role_permission
USING public.t_sys_permission list_permission,
      public.t_sys_permission detail_permission,
      public.t_sys_role_perms detail_role_permission
WHERE list_role_permission.permission_id = list_permission.id
  AND detail_permission.number = REPLACE(list_permission.number, ':listPage', ':detail')
  AND detail_role_permission.role_id = list_role_permission.role_id
  AND detail_role_permission.permission_id = detail_permission.id
  AND list_permission.number IN (
      'sys:base:ui-config:listPage',
      'sys:base:file-config:listPage'
  );

UPDATE public.t_sys_role_perms role_permission
SET permission_id = detail_permission.id
FROM public.t_sys_permission list_permission,
     public.t_sys_permission detail_permission
WHERE role_permission.permission_id = list_permission.id
  AND detail_permission.number = REPLACE(list_permission.number, ':listPage', ':detail')
  AND list_permission.number IN (
      'sys:base:ui-config:listPage',
      'sys:base:file-config:listPage'
  );

UPDATE public.t_sys_menu
SET permission_id = (
    SELECT id FROM public.t_sys_permission
    WHERE number = 'sys:base:ui-config:detail'
)
WHERE component = 'sys/base/ui-config';

UPDATE public.t_sys_menu
SET permission_id = (
    SELECT id FROM public.t_sys_permission
    WHERE number = 'sys:base:file-config:detail'
)
WHERE component = 'sys/base/file-config';

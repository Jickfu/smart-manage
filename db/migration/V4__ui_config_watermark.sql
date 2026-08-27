ALTER TABLE public.t_sys_ui_config
    ADD COLUMN watermark_enabled boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_content character varying(200),
    ADD COLUMN watermark_show_name boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_show_phone boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_show_email boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_show_number boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_show_root_org boolean NOT NULL DEFAULT false,
    ADD COLUMN watermark_gap_x smallint NOT NULL DEFAULT 100,
    ADD COLUMN watermark_gap_y smallint NOT NULL DEFAULT 100;

COMMENT ON COLUMN public.t_sys_ui_config.watermark_enabled IS '是否启用水印';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_content IS '水印固定内容';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_show_name IS '水印是否显示当前用户姓名';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_show_phone IS '水印是否显示脱敏手机号';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_show_email IS '水印是否显示脱敏邮箱';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_show_number IS '水印是否显示当前用户工号';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_show_root_org IS '水印是否显示当前用户绝对顶层组织名称';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_gap_x IS '水印水平间距（像素）';
COMMENT ON COLUMN public.t_sys_ui_config.watermark_gap_y IS '水印垂直间距（像素）';

ALTER TABLE public.t_sys_ui_config
    ADD CONSTRAINT ck_sys_ui_config_watermark_gap_x
        CHECK (watermark_gap_x BETWEEN 20 AND 500),
    ADD CONSTRAINT ck_sys_ui_config_watermark_gap_y
        CHECK (watermark_gap_y BETWEEN 20 AND 500);

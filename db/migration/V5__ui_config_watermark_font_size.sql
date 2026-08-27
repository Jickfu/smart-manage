ALTER TABLE public.t_sys_ui_config
    ADD COLUMN watermark_font_size smallint NOT NULL DEFAULT 16;

COMMENT ON COLUMN public.t_sys_ui_config.watermark_font_size IS '水印字体大小（像素）';

ALTER TABLE public.t_sys_ui_config
    ADD CONSTRAINT ck_sys_ui_config_watermark_font_size
        CHECK (watermark_font_size BETWEEN 12 AND 32);

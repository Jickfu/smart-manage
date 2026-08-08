-- 修复历史缺陷产生的界面配置悬空引用：非 ACTIVE 附件不能作为正式业务资源。
UPDATE public.t_sys_ui_config config
SET login_banner_attachment_id = CASE
        WHEN config.login_banner_attachment_id IS NULL OR EXISTS (
            SELECT 1
            FROM public.t_sys_attachment attachment
            WHERE attachment.id = config.login_banner_attachment_id
              AND attachment.status = 'ACTIVE'
        ) THEN config.login_banner_attachment_id
        ELSE NULL
    END,
    login_logo_attachment_id = CASE
        WHEN config.login_logo_attachment_id IS NULL OR EXISTS (
            SELECT 1
            FROM public.t_sys_attachment attachment
            WHERE attachment.id = config.login_logo_attachment_id
              AND attachment.status = 'ACTIVE'
        ) THEN config.login_logo_attachment_id
        ELSE NULL
    END,
    header_logo_attachment_id = CASE
        WHEN config.header_logo_attachment_id IS NULL OR EXISTS (
            SELECT 1
            FROM public.t_sys_attachment attachment
            WHERE attachment.id = config.header_logo_attachment_id
              AND attachment.status = 'ACTIVE'
        ) THEN config.header_logo_attachment_id
        ELSE NULL
    END,
    version = config.version + 1,
    update_time = CURRENT_TIMESTAMP
WHERE (config.login_banner_attachment_id IS NOT NULL AND NOT EXISTS (
           SELECT 1 FROM public.t_sys_attachment attachment
           WHERE attachment.id = config.login_banner_attachment_id
             AND attachment.status = 'ACTIVE'
       ))
   OR (config.login_logo_attachment_id IS NOT NULL AND NOT EXISTS (
           SELECT 1 FROM public.t_sys_attachment attachment
           WHERE attachment.id = config.login_logo_attachment_id
             AND attachment.status = 'ACTIVE'
       ))
   OR (config.header_logo_attachment_id IS NOT NULL AND NOT EXISTS (
           SELECT 1 FROM public.t_sys_attachment attachment
           WHERE attachment.id = config.header_logo_attachment_id
             AND attachment.status = 'ACTIVE'
       ));

-- 删除不可用附件遗留的业务映射；TEMP 附件仅允许保留尚未绑定业务 ID 的上传会话映射。
DELETE FROM public.t_sys_biz_attachment mapping
USING public.t_sys_attachment attachment
WHERE mapping.attachment_id = attachment.id
  AND (
      attachment.status IN ('PENDING_DELETE', 'DELETED')
      OR (attachment.status = 'TEMP' AND mapping.biz_id IS NOT NULL)
  );

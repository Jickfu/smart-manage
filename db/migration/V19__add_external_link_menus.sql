-- 页面菜单显式区分内部页面与外部链接，分组菜单不承载页面目标。
ALTER TABLE public.t_sys_menu
    ADD COLUMN target_type varchar(20),
    ADD COLUMN external_url varchar(2048),
    ADD COLUMN external_open_mode varchar(20);

COMMENT ON COLUMN public.t_sys_menu.target_type IS '页面目标类型：INTERNAL_PAGE、EXTERNAL_LINK';
COMMENT ON COLUMN public.t_sys_menu.external_url IS '外部链接地址';
COMMENT ON COLUMN public.t_sys_menu.external_open_mode IS '外链打开方式：NEW_TAB、IFRAME';

UPDATE public.t_sys_menu
SET target_type = 'INTERNAL_PAGE'
WHERE level = 1;

ALTER TABLE public.t_sys_menu
    ADD CONSTRAINT ck_sys_menu_page_target CHECK (
        (level = 0
            AND target_type IS NULL
            AND path IS NULL
            AND component IS NULL
            AND external_url IS NULL
            AND external_open_mode IS NULL)
        OR
        (level = 1
            AND target_type = 'INTERNAL_PAGE'
            AND NULLIF(BTRIM(path), '') IS NOT NULL
            AND NULLIF(BTRIM(component), '') IS NOT NULL
            AND external_url IS NULL
            AND external_open_mode IS NULL)
        OR
        (level = 1
            AND target_type = 'EXTERNAL_LINK'
            AND path IS NULL
            AND component IS NULL
            AND NULLIF(BTRIM(external_url), '') IS NOT NULL
            AND external_url ~* '^https?://[^[:space:]]+$'
            AND external_open_mode IN ('NEW_TAB', 'IFRAME'))
    );

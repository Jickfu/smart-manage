-- 应用入口权限不属于具体 Feature；Feature 入口权限应归入完整功能键，不能生成父级伪功能。
ALTER TABLE public.t_sys_permission ADD COLUMN app_id bigint;
COMMENT ON COLUMN public.t_sys_permission.app_id IS '所属应用ID，仅应用级权限使用';

ALTER TABLE public.t_sys_permission ALTER COLUMN feature_id DROP NOT NULL;
ALTER TABLE public.t_sys_menu ALTER COLUMN feature_id DROP NOT NULL;

UPDATE public.t_sys_permission a
SET app_id = b.app_id,
    feature_id = NULL
FROM public.t_sys_feature b
WHERE a.feature_id = b.id
  AND a.number IN ('sys:base:access', 'sys:log:access', 'sys:scheduler:category');

UPDATE public.t_sys_permission a
SET feature_id = target.id,
    app_id = NULL
FROM public.t_sys_feature target
WHERE a.number = 'scm:procurement:purchase-requisition'
  AND target.feature_key = 'scm/procurement/purchase-requisition';

-- 菜单归属跟随入口权限；应用级分组菜单不伪造 Feature。
UPDATE public.t_sys_menu a
SET feature_id = b.feature_id
FROM public.t_sys_permission b
WHERE b.id = a.permission_id
  AND a.feature_id IS DISTINCT FROM b.feature_id;

DELETE FROM public.t_sys_feature
WHERE feature_key IN ('sys/base', 'sys/log', 'sys/scheduler', 'scm/procurement');

ALTER TABLE public.t_sys_permission
    ADD CONSTRAINT fk_sys_permission_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id),
    ADD CONSTRAINT ck_sys_permission_owner CHECK (
        (feature_id IS NOT NULL AND app_id IS NULL)
        OR (feature_id IS NULL AND app_id IS NOT NULL)
    );
CREATE INDEX idx_sys_permission_app ON public.t_sys_permission(app_id);

ALTER TABLE public.t_sys_menu
    ADD CONSTRAINT ck_sys_menu_page_feature CHECK (level <> 1 OR feature_id IS NOT NULL);

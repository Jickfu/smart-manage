CREATE TABLE public.t_sys_feature (
    id bigint NOT NULL,
    feature_key varchar(200) NOT NULL,
    app_id bigint NOT NULL,
    default_name varchar(100) NOT NULL,
    custom_name varchar(100),
    default_seq integer NOT NULL DEFAULT 99,
    custom_seq integer,
    description varchar(500),
    visible boolean NOT NULL DEFAULT true,
    source varchar(20) NOT NULL DEFAULT 'SYSTEM',
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT t_sys_feature_pkey PRIMARY KEY (id),
    CONSTRAINT uk_sys_feature_key UNIQUE (feature_key),
    CONSTRAINT fk_sys_feature_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id),
    CONSTRAINT ck_sys_feature_source CHECK (source IN ('SYSTEM', 'PLUGIN', 'EXTERNAL'))
);

COMMENT ON TABLE public.t_sys_feature IS '系统功能目录';
COMMENT ON COLUMN public.t_sys_feature.id IS 'ID';
COMMENT ON COLUMN public.t_sys_feature.feature_key IS '稳定功能键';
COMMENT ON COLUMN public.t_sys_feature.app_id IS '所属应用ID';
COMMENT ON COLUMN public.t_sys_feature.default_name IS '系统默认名称';
COMMENT ON COLUMN public.t_sys_feature.custom_name IS '管理员自定义名称';
COMMENT ON COLUMN public.t_sys_feature.default_seq IS '系统默认排序';
COMMENT ON COLUMN public.t_sys_feature.custom_seq IS '管理员自定义排序';
COMMENT ON COLUMN public.t_sys_feature.description IS '描述';
COMMENT ON COLUMN public.t_sys_feature.visible IS '是否在功能目录中可见';
COMMENT ON COLUMN public.t_sys_feature.source IS '来源';
COMMENT ON COLUMN public.t_sys_feature.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_feature.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_feature.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_feature.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_feature.version IS '乐观锁版本号';

CREATE INDEX idx_sys_feature_app ON public.t_sys_feature(app_id);

WITH feature_source AS (
    SELECT DISTINCT ON (regexp_replace(a.number, ':[^:]+$', ''))
           regexp_replace(a.number, ':[^:]+$', '') AS permission_prefix,
           a.app_id,
           split_part(a.name, '-', 1) AS default_name
    FROM public.t_sys_permission a
    ORDER BY regexp_replace(a.number, ':[^:]+$', ''), a.id
), numbered AS (
    SELECT 450000000000000000 + row_number() OVER (ORDER BY permission_prefix) AS id,
           replace(permission_prefix, ':', '/') AS feature_key,
           app_id,
           default_name,
           row_number() OVER (PARTITION BY app_id ORDER BY permission_prefix) * 10 AS default_seq
    FROM feature_source
)
INSERT INTO public.t_sys_feature (id, feature_key, app_id, default_name, default_seq, visible, source, version)
SELECT id, feature_key, app_id, default_name, default_seq, true, 'SYSTEM', 0
FROM numbered;

-- 功能管理本身也是系统建模应用中的稳定功能。
INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, version)
VALUES
    (450000000000000100, 'sys/base/feature', 31, '功能管理', 40,
     '维护系统功能的展示名称、排序、描述和目录可见性', true, 'SYSTEM', 0);

ALTER TABLE public.t_sys_permission ADD COLUMN feature_id bigint;
COMMENT ON COLUMN public.t_sys_permission.feature_id IS '所属功能ID';

UPDATE public.t_sys_permission a
SET feature_id = b.id
FROM public.t_sys_feature b
WHERE b.feature_key = replace(regexp_replace(a.number, ':[^:]+$', ''), ':', '/');

INSERT INTO public.t_sys_permission (id, name, number, app_id, feature_id, version)
VALUES
    (450000000000000101, '功能管理-查询', 'sys:base:feature:listPage', 31, 450000000000000100, 0),
    (450000000000000102, '功能管理-详情', 'sys:base:feature:detail', 31, 450000000000000100, 0),
    (450000000000000103, '功能管理-保存', 'sys:base:feature:save', 31, 450000000000000100, 0),
    (450000000000000104, '功能管理-选择', 'sys:base:feature:select', 31, 450000000000000100, 0);

ALTER TABLE public.t_sys_permission ALTER COLUMN feature_id SET NOT NULL;
ALTER TABLE public.t_sys_permission
    ADD CONSTRAINT fk_sys_permission_feature FOREIGN KEY (feature_id) REFERENCES public.t_sys_feature(id);
CREATE INDEX idx_sys_permission_feature ON public.t_sys_permission(feature_id);
ALTER TABLE public.t_sys_permission DROP CONSTRAINT fk_sys_perm_app;
DROP INDEX public.idx_sys_perm_app;
ALTER TABLE public.t_sys_permission DROP COLUMN app_id;

ALTER TABLE public.t_sys_menu ADD COLUMN feature_id bigint;
COMMENT ON COLUMN public.t_sys_menu.feature_id IS '所属功能ID';
UPDATE public.t_sys_menu a
SET feature_id = b.feature_id
FROM public.t_sys_permission b
WHERE b.id = a.permission_id;

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, feature_id, permission_id, path, component,
     icon, description, sort, enabled, version)
VALUES
    (450000000000000110, 'feature', '功能管理', 1, 3101, 31, 450000000000000100,
     450000000000000101, '/sys/base/feature', 'sys/base/feature', 'ClusterOutlined',
     '维护系统功能目录的运营字段', 4, true, 0);

ALTER TABLE public.t_sys_menu ALTER COLUMN feature_id SET NOT NULL;
ALTER TABLE public.t_sys_menu
    ADD CONSTRAINT fk_sys_menu_feature FOREIGN KEY (feature_id) REFERENCES public.t_sys_feature(id);
CREATE INDEX idx_sys_menu_feature ON public.t_sys_menu(feature_id);

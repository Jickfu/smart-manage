ALTER TABLE public.t_sys_org
    ALTER COLUMN parent_id DROP DEFAULT,
    ALTER COLUMN parent_id DROP NOT NULL,
    ADD COLUMN number_path character varying(2000),
    ADD COLUMN name_path character varying(2000),
    ADD COLUMN org_type character varying(20) DEFAULT 'COMPANY' NOT NULL,
    ADD COLUMN enabled boolean DEFAULT true NOT NULL,
    ADD COLUMN archived boolean DEFAULT false NOT NULL,
    ADD COLUMN archived_at timestamp without time zone,
    ADD COLUMN description character varying(500),
    ADD COLUMN version integer DEFAULT 0 NOT NULL;

UPDATE public.t_sys_org SET parent_id = NULL WHERE parent_id = 0;

WITH RECURSIVE org_path AS (
    SELECT id, parent_id, number::text AS number_path, name::text AS name_path
    FROM public.t_sys_org
    WHERE parent_id IS NULL
    UNION ALL
    SELECT child.id, child.parent_id,
           parent.number_path || '/' || child.number,
           parent.name_path || '/' || child.name
    FROM public.t_sys_org child
    JOIN org_path parent ON parent.id = child.parent_id
)
UPDATE public.t_sys_org target
SET number_path = org_path.number_path, name_path = org_path.name_path
FROM org_path
WHERE target.id = org_path.id;

ALTER TABLE public.t_sys_org
    ALTER COLUMN number_path SET NOT NULL,
    ALTER COLUMN name_path SET NOT NULL,
    ADD CONSTRAINT ck_sys_org_type CHECK (org_type IN ('GROUP', 'COMPANY', 'DEPARTMENT')),
    ADD CONSTRAINT ck_sys_org_archive_state CHECK (NOT archived OR NOT enabled),
    ADD CONSTRAINT ck_sys_org_archive_time CHECK ((archived AND archived_at IS NOT NULL) OR (NOT archived AND archived_at IS NULL)),
    ADD CONSTRAINT ck_sys_org_parent_self CHECK (parent_id IS NULL OR parent_id <> id),
    ADD CONSTRAINT fk_sys_org_parent FOREIGN KEY (parent_id) REFERENCES public.t_sys_org(id);

CREATE UNIQUE INDEX uk_sys_org_number ON public.t_sys_org(number);
CREATE INDEX idx_sys_org_parent_sort ON public.t_sys_org(parent_id, sort, number);

COMMENT ON COLUMN public.t_sys_org.number_path IS '长编码';
COMMENT ON COLUMN public.t_sys_org.name_path IS '长名称';
COMMENT ON COLUMN public.t_sys_org.org_type IS '组织类型';
COMMENT ON COLUMN public.t_sys_org.enabled IS '使用状态';
COMMENT ON COLUMN public.t_sys_org.archived IS '封存状态';
COMMENT ON COLUMN public.t_sys_org.archived_at IS '封存时间';
COMMENT ON COLUMN public.t_sys_org.description IS '描述';
COMMENT ON COLUMN public.t_sys_org.version IS '乐观锁版本号';

INSERT INTO public.t_sys_permission (id, name, number, app_id, version) VALUES
    (450000000000000001, '组织管理-查询', 'sys:base:org:listPage', 31, 0),
    (450000000000000002, '组织管理-详情', 'sys:base:org:detail', 31, 0),
    (450000000000000003, '组织管理-保存', 'sys:base:org:save', 31, 0),
    (450000000000000004, '组织管理-启用', 'sys:base:org:enable', 31, 0),
    (450000000000000005, '组织管理-禁用', 'sys:base:org:disable', 31, 0),
    (450000000000000006, '组织管理-封存', 'sys:base:org:archive', 31, 0),
    (450000000000000007, '组织管理-解封', 'sys:base:org:unarchive', 31, 0);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon, description, sort, enabled, version)
VALUES
    (450000000000000010, 'org', '组织管理', 1, 3101, 31, 450000000000000001,
     '/sys/base/org', 'sys/base/org', 'ApartmentOutlined', '行政组织管理', 2, true, 0);

INSERT INTO public.t_sys_role_perms (id, role_id, permission_id)
SELECT 450000000000000100 + row_number() OVER (ORDER BY permission.id), 1, permission.id
FROM public.t_sys_permission permission
WHERE permission.number LIKE 'sys:base:org:%';

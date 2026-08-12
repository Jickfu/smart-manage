UPDATE public.t_sys_org
SET name = 'SM有限公司',
    number = 'SM',
    parent_id = NULL,
    sort = 1,
    number_path = 'SM',
    name_path = 'SM有限公司',
    org_type = 'COMPANY',
    enabled = true,
    archived = false,
    archived_at = NULL,
    description = NULL,
    update_time = now(),
    update_user = NULL,
    version = CASE WHEN number = 'SM' AND name = 'SM有限公司' THEN version ELSE version + 1 END
WHERE id = 1
  AND number IN ('DEFAULT_ORG', 'SM');

DO $$
BEGIN
    IF (SELECT count(*) FROM public.t_sys_org WHERE number = 'SM') <> 1 THEN
        RAISE EXCEPTION '初始化组织编码 SM 必须且只能存在一个';
    END IF;
    IF NOT EXISTS (
        SELECT 1
        FROM public.t_sys_org
        WHERE number = 'SM'
          AND parent_id IS NULL
          AND enabled
          AND NOT archived
    ) THEN
        RAISE EXCEPTION '初始化组织 SM 必须是启用且未封存的顶级组织';
    END IF;
END $$;

INSERT INTO public.t_sys_org
    (id, name, number, parent_id, sort, create_time, update_time, create_user, update_user,
     number_path, name_path, org_type, enabled, archived, archived_at, description, version)
VALUES
    (2087035058459361282, '领导层', '101',
     (SELECT id FROM public.t_sys_org WHERE number = 'SM'), 2,
     now(), now(), NULL, NULL, 'SM/101', 'SM有限公司/领导层',
     'DEPARTMENT', true, false, NULL, NULL, 1),
    (2087035439688040449, '财务部', '102',
     (SELECT id FROM public.t_sys_org WHERE number = 'SM'), 3,
     now(), now(), NULL, NULL, 'SM/102', 'SM有限公司/财务部',
     'DEPARTMENT', true, false, NULL, NULL, 1)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    number = EXCLUDED.number,
    parent_id = EXCLUDED.parent_id,
    sort = EXCLUDED.sort,
    number_path = EXCLUDED.number_path,
    name_path = EXCLUDED.name_path,
    org_type = EXCLUDED.org_type,
    enabled = EXCLUDED.enabled,
    archived = EXCLUDED.archived,
    archived_at = EXCLUDED.archived_at,
    description = EXCLUDED.description,
    update_time = now(),
    update_user = NULL;

UPDATE public.t_sys_user_assignment
SET is_primary = false,
    update_time = now(),
    update_user = NULL
WHERE user_id = (SELECT id FROM public.t_sys_user WHERE username = 'administrator')
  AND is_primary;

INSERT INTO public.t_sys_user_assignment
    (id, user_id, org_id, position, is_org_leader, is_primary,
     create_time, create_user, update_time, update_user)
SELECT 450000000000000001,
       a.id,
       b.id,
       '系统管理员',
       false,
       true,
       now(),
       NULL,
       now(),
       NULL
FROM public.t_sys_user a
CROSS JOIN public.t_sys_org b
WHERE a.username = 'administrator'
  AND b.number = 'SM'
ON CONFLICT (user_id, org_id) DO UPDATE
SET position = EXCLUDED.position,
    is_primary = true,
    update_time = now(),
    update_user = NULL;

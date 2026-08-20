-- Cloud 在本项目中即领域目录；统一数据库对象和系统内置稳定身份，不保留旧命名兼容。
ALTER TABLE public.t_sys_cloud RENAME TO t_sys_domain;
ALTER TABLE public.t_sys_domain RENAME CONSTRAINT t_sys_cloud_pkey TO t_sys_domain_pkey;
ALTER TABLE public.t_sys_domain RENAME CONSTRAINT uk_sys_cloud_number TO uk_sys_domain_number;
ALTER INDEX public.idx_sys_cloud_num RENAME TO idx_sys_domain_num;

ALTER TABLE public.t_sys_app RENAME COLUMN cloud_id TO domain_id;
ALTER TABLE public.t_sys_app RENAME CONSTRAINT fk_sys_app_cloud TO fk_sys_app_domain;
ALTER INDEX public.idx_sys_app_cloud RENAME TO idx_sys_app_domain;

ALTER TABLE public.t_sys_basic_data_category RENAME COLUMN cloud_id TO domain_id;
ALTER TABLE public.t_sys_basic_data_category
    RENAME CONSTRAINT fk_basic_data_category_cloud TO fk_basic_data_category_domain;
ALTER INDEX public.idx_basic_data_category_cloud_id RENAME TO idx_basic_data_category_domain_id;

COMMENT ON TABLE public.t_sys_domain IS '领域目录';
COMMENT ON COLUMN public.t_sys_app.domain_id IS '所属领域ID';
COMMENT ON COLUMN public.t_sys_basic_data_category.domain_id IS '所属领域ID';

-- 保留内置记录 ID 和角色授权关系，只迁移稳定业务键及展示语义。
UPDATE public.t_sys_feature
SET feature_key = 'sys/base/domain',
    default_name = '领域管理',
    description = CASE WHEN description = '云管理' THEN '领域目录管理' ELSE description END,
    update_time = now(),
    version = version + 1
WHERE feature_key = 'sys/base/cloud';

UPDATE public.t_sys_permission
SET number = regexp_replace(number, '^sys:base:cloud:', 'sys:base:domain:'),
    name = replace(name, '云管理', '领域管理'),
    update_time = now(),
    version = version + 1
WHERE number LIKE 'sys:base:cloud:%';

UPDATE public.t_sys_menu
SET number = 'domain',
    name = '领域管理',
    path = '/sys/base/domain',
    component = 'sys/base/domain',
    description = '领域目录管理',
    update_time = now(),
    version = version + 1
WHERE id = 3102;

UPDATE public.t_sys_menu
SET description = replace(description, '云、应用、功能与菜单结构', '领域、应用、功能与菜单结构'),
    update_time = now(),
    version = version + 1
WHERE description = '云、应用、功能与菜单结构';

UPDATE public.t_sys_app
SET description = replace(description, '云、应用、菜单、用户等基础数据', '领域、应用、菜单、用户等基础数据'),
    update_time = now(),
    version = version + 1
WHERE description = '云、应用、菜单、用户等基础数据';

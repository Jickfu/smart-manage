ALTER TABLE public.t_sys_openapi_release
    ADD COLUMN domain_key varchar(100),
    ADD COLUMN domain_name varchar(200),
    ADD COLUMN application_key varchar(100),
    ADD COLUMN application_name varchar(200),
    ADD COLUMN feature_key varchar(100),
    ADD COLUMN feature_name varchar(200);

UPDATE public.t_sys_openapi_release
SET domain_key = 'sys',
    domain_name = '系统管理',
    application_key = 'base',
    application_name = '基础平台',
    feature_key = 'basic-data',
    feature_name = '基础资料',
    path = '/openapi/sys/base/basic-data/v1/items/query'
WHERE operation_key = 'sys.basicData.items.queryByCategory';

ALTER TABLE public.t_sys_openapi_release
    ALTER COLUMN domain_key SET NOT NULL,
    ALTER COLUMN domain_name SET NOT NULL,
    ALTER COLUMN application_key SET NOT NULL,
    ALTER COLUMN application_name SET NOT NULL,
    ALTER COLUMN feature_key SET NOT NULL,
    ALTER COLUMN feature_name SET NOT NULL;

COMMENT ON COLUMN public.t_sys_openapi_release.domain_key IS 'API 归属领域稳定编码';
COMMENT ON COLUMN public.t_sys_openapi_release.application_key IS 'API 归属应用稳定编码';
COMMENT ON COLUMN public.t_sys_openapi_release.feature_key IS 'API 归属功能稳定编码';

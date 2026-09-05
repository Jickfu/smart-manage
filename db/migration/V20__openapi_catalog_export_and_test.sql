ALTER TABLE public.t_sys_openapi_release
    ADD COLUMN request_example jsonb;

UPDATE public.t_sys_openapi_release
SET request_example = '{"categoryNumber":"分类编码"}'::jsonb
WHERE operation_key = 'sys.basicData.items.queryByCategory';

COMMENT ON COLUMN public.t_sys_openapi_release.request_example IS
    'API 文档和管理端业务试调使用的显式请求示例';

INSERT INTO public.t_sys_permission
    (id, name, number, create_time, version, feature_id, app_id)
VALUES
    (520000000000000203, 'API文档-业务试调',
     'sys:base:openapi-catalog:test', now(), 0, 520000000000000020, NULL);

INSERT INTO public.t_sys_permission
    (id, name, number, app_id, feature_id, create_time, version)
SELECT 420000000000001025,
       '用户管理-查看敏感信息',
       'sys:base:user:sensitive:read',
       NULL,
       id,
       CURRENT_TIMESTAMP,
       0
FROM public.t_sys_feature
WHERE feature_key = 'sys/base/user';

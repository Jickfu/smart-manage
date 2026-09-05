ALTER TABLE public.t_sys_openapi_release
    ADD COLUMN response_example jsonb;

UPDATE public.t_sys_openapi_release
SET response_example = '{
  "categoryNumber": "分类编码",
  "items": [
    {
      "name": "资料名称",
      "number": "资料编码",
      "namePath": "分类名称/资料名称",
      "numberPath": "分类编码/资料编码",
      "parentNumber": null
    }
  ]
}'::jsonb
WHERE operation_key = 'sys.basicData.items.queryByCategory';

COMMENT ON COLUMN public.t_sys_openapi_release.response_example IS
    'API 文档使用的显式响应 JSON 示例';

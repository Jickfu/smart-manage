-- 系统参数只保留全局和功能两种作用域；领域、应用通过功能归属聚合查询。
INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, version)
VALUES
    (480000000000000100, 'sys/base/login-protection', 31, '登录保护', 65,
     '登录验证码、失败限制和短时保护等认证安全能力', true, 'SYSTEM', 0);

UPDATE public.t_sys_feature
SET default_name = '系统参数',
    version = version + 1
WHERE id = 450000000000000011;

ALTER TABLE public.t_sys_param
    ADD COLUMN feature_id bigint;

COMMENT ON COLUMN public.t_sys_param.feature_id IS '所属功能ID；为空表示全局参数';

UPDATE public.t_sys_param
SET feature_id = 450000000000000025
WHERE id = 425000000000000010;

UPDATE public.t_sys_param
SET feature_id = 450000000000000022
WHERE id IN (426000000000000010, 426000000000000011, 426000000000000012);

UPDATE public.t_sys_param
SET feature_id = 480000000000000100
WHERE id BETWEEN 480000000000000001 AND 480000000000000011;

ALTER TABLE public.t_sys_param
    ADD CONSTRAINT fk_sys_param_feature
    FOREIGN KEY (feature_id) REFERENCES public.t_sys_feature(id);

CREATE INDEX idx_sys_param_feature_id ON public.t_sys_param (feature_id);

ALTER TABLE public.t_sys_param DROP CONSTRAINT fk_sys_param_app;
DROP INDEX public.idx_sys_param_app_id;
ALTER TABLE public.t_sys_param DROP COLUMN app_id;

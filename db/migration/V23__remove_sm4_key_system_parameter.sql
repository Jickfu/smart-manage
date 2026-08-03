-- SM4 密钥属于部署密钥，禁止通过系统参数保存或管理。
-- 升级已有环境前必须先将原密钥安全迁移到 SMART_MANAGE_SM4_KEY，确保既有密文仍可解密。
DELETE FROM public.t_sys_param
WHERE number = 'SM4_KEY';

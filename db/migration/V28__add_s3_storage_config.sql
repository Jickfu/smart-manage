ALTER TABLE public.t_sys_file_config
    ADD COLUMN s3_endpoint varchar(500),
    ADD COLUMN s3_region varchar(100),
    ADD COLUMN s3_bucket varchar(255),
    ADD COLUMN s3_access_key varchar(255),
    ADD COLUMN s3_secret_key_cipher text,
    ADD COLUMN s3_path_style boolean NOT NULL DEFAULT true;

COMMENT ON COLUMN public.t_sys_file_config.s3_endpoint IS 'S3兼容服务地址';
COMMENT ON COLUMN public.t_sys_file_config.s3_region IS 'S3区域';
COMMENT ON COLUMN public.t_sys_file_config.s3_bucket IS 'S3私有Bucket';
COMMENT ON COLUMN public.t_sys_file_config.s3_access_key IS 'S3 Access Key';
COMMENT ON COLUMN public.t_sys_file_config.s3_secret_key_cipher IS 'S3 Secret Key密文';
COMMENT ON COLUMN public.t_sys_file_config.s3_path_style IS '是否使用Path Style';

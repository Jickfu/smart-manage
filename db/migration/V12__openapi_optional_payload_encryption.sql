ALTER TABLE t_sys_openapi_application
    DROP CONSTRAINT ck_sys_openapi_application_encryption;

ALTER TABLE t_sys_openapi_application
    ADD CONSTRAINT ck_sys_openapi_application_encryption
        CHECK (encryption_algorithm IN ('NONE', 'AES_256_GCM', 'SM4_GCM'));

ALTER TABLE t_sys_openapi_credential
    DROP CONSTRAINT ck_sys_openapi_credential_encryption;

ALTER TABLE t_sys_openapi_credential
    ALTER COLUMN request_encryption_key_cipher DROP NOT NULL,
    ALTER COLUMN response_encryption_key_cipher DROP NOT NULL;

ALTER TABLE t_sys_openapi_credential
    ADD CONSTRAINT ck_sys_openapi_credential_encryption
        CHECK (encryption_algorithm IN ('NONE', 'AES_256_GCM', 'SM4_GCM')),
    ADD CONSTRAINT ck_sys_openapi_credential_encryption_keys
        CHECK (
            (encryption_algorithm = 'NONE'
                AND request_encryption_key_cipher IS NULL
                AND response_encryption_key_cipher IS NULL)
            OR
            (encryption_algorithm IN ('AES_256_GCM', 'SM4_GCM')
                AND request_encryption_key_cipher IS NOT NULL
                AND response_encryption_key_cipher IS NOT NULL)
        );

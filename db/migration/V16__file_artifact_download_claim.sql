ALTER TABLE public.t_sys_file_artifact
    DROP CONSTRAINT ck_sys_file_artifact_status;

ALTER TABLE public.t_sys_file_artifact
    ADD COLUMN download_claim_token varchar(64),
    ADD COLUMN download_claimed_at timestamp without time zone,
    ADD CONSTRAINT ck_sys_file_artifact_status
        CHECK (status IN ('ACTIVE', 'DOWNLOADING', 'PENDING_DELETE', 'DELETED')),
    ADD CONSTRAINT ck_sys_file_artifact_download_claim
        CHECK ((status = 'DOWNLOADING' AND download_claim_token IS NOT NULL AND download_claimed_at IS NOT NULL)
            OR (status <> 'DOWNLOADING' AND download_claim_token IS NULL AND download_claimed_at IS NULL));

COMMENT ON COLUMN public.t_sys_file_artifact.download_claim_token IS '下载资格声明令牌';
COMMENT ON COLUMN public.t_sys_file_artifact.download_claimed_at IS '下载资格声明时间';

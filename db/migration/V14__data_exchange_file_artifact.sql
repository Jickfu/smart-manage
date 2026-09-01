CREATE TABLE public.t_sys_file_artifact (
    id bigint NOT NULL,
    purpose varchar(50) NOT NULL,
    owner_user_id bigint NOT NULL,
    original_name varchar(255) NOT NULL,
    storage_type varchar(20) NOT NULL,
    object_key varchar(1000) NOT NULL,
    mime_type varchar(200) NOT NULL,
    file_size bigint NOT NULL,
    status varchar(30) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    download_count integer NOT NULL DEFAULT 0,
    max_downloads integer,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT t_sys_file_artifact_pkey PRIMARY KEY (id),
    CONSTRAINT uk_sys_file_artifact_object_key UNIQUE (object_key),
    CONSTRAINT ck_sys_file_artifact_size CHECK (file_size >= 0),
    CONSTRAINT ck_sys_file_artifact_download_count CHECK (download_count >= 0),
    CONSTRAINT ck_sys_file_artifact_status CHECK (status IN ('ACTIVE', 'PENDING_DELETE', 'DELETED'))
);

CREATE INDEX idx_sys_file_artifact_cleanup
    ON public.t_sys_file_artifact (status, expires_at);
CREATE INDEX idx_sys_file_artifact_owner
    ON public.t_sys_file_artifact (owner_user_id, create_time DESC);

COMMENT ON TABLE public.t_sys_file_artifact IS '受管理文件制品';
COMMENT ON COLUMN public.t_sys_file_artifact.id IS 'ID';
COMMENT ON COLUMN public.t_sys_file_artifact.purpose IS '受控文件用途';
COMMENT ON COLUMN public.t_sys_file_artifact.owner_user_id IS '所有者用户ID';
COMMENT ON COLUMN public.t_sys_file_artifact.original_name IS '原始文件名';
COMMENT ON COLUMN public.t_sys_file_artifact.storage_type IS '存储类型';
COMMENT ON COLUMN public.t_sys_file_artifact.object_key IS '对象键';
COMMENT ON COLUMN public.t_sys_file_artifact.mime_type IS '媒体类型';
COMMENT ON COLUMN public.t_sys_file_artifact.file_size IS '文件大小';
COMMENT ON COLUMN public.t_sys_file_artifact.status IS '生命周期状态';
COMMENT ON COLUMN public.t_sys_file_artifact.expires_at IS '过期时间';
COMMENT ON COLUMN public.t_sys_file_artifact.download_count IS '已下载次数';
COMMENT ON COLUMN public.t_sys_file_artifact.max_downloads IS '最大下载次数';
COMMENT ON COLUMN public.t_sys_file_artifact.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_file_artifact.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_file_artifact.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_file_artifact.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_file_artifact.version IS '乐观锁版本号';

INSERT INTO public.t_sys_permission
    (id, name, number, version, feature_id, app_id)
VALUES
    (510000000000000001, '用户管理-导入', 'sys:base:user:import', 0, 450000000000000015, NULL),
    (510000000000000002, '采购申请-导出', 'scm:procurement:purchase-requisition:export', 0, 450000000000000002, NULL);

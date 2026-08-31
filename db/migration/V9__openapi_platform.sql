-- OpenAPI 平台一期：第三方应用、独立凭据、显式 API 目录、授权及调用审计。

CREATE TABLE public.t_sys_openapi_application (
    id bigint PRIMARY KEY,
    number varchar(100) NOT NULL,
    name varchar(200) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    proxy_user_id bigint NOT NULL,
    proxy_org_id bigint NOT NULL,
    authentication_type varchar(30) DEFAULT 'HMAC_SHA256' NOT NULL,
    encryption_algorithm varchar(30) NOT NULL,
    ip_policy_mode varchar(20) DEFAULT 'DISABLED' NOT NULL,
    ip_ranges text,
    description varchar(500),
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    CONSTRAINT uk_sys_openapi_application_number UNIQUE (number),
    CONSTRAINT fk_sys_openapi_application_user FOREIGN KEY (proxy_user_id) REFERENCES public.t_sys_user(id),
    CONSTRAINT fk_sys_openapi_application_org FOREIGN KEY (proxy_org_id) REFERENCES public.t_sys_org(id),
    CONSTRAINT ck_sys_openapi_application_auth CHECK (authentication_type IN ('HMAC_SHA256')),
    CONSTRAINT ck_sys_openapi_application_encryption CHECK (encryption_algorithm IN ('AES_256_GCM', 'SM4_GCM')),
    CONSTRAINT ck_sys_openapi_application_ip_mode CHECK (ip_policy_mode IN ('DISABLED', 'WHITELIST', 'BLACKLIST'))
);
COMMENT ON TABLE public.t_sys_openapi_application IS 'OpenAPI 第三方应用';
COMMENT ON COLUMN public.t_sys_openapi_application.proxy_user_id IS '外部调用建立业务上下文时使用的普通用户';
COMMENT ON COLUMN public.t_sys_openapi_application.proxy_org_id IS '外部调用固定组织，不跟随代理用户浏览器会话';
COMMENT ON COLUMN public.t_sys_openapi_application.ip_ranges IS '每行一个 IPv4/IPv6 地址或 CIDR 网段';

CREATE TABLE public.t_sys_openapi_credential (
    id bigint PRIMARY KEY,
    application_id bigint NOT NULL,
    key_id varchar(100) NOT NULL,
    name varchar(200) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    encryption_algorithm varchar(30) NOT NULL,
    signing_secret_cipher text NOT NULL,
    request_encryption_key_cipher text NOT NULL,
    response_encryption_key_cipher text NOT NULL,
    expires_at timestamp without time zone,
    last_used_at timestamp without time zone,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    CONSTRAINT fk_sys_openapi_credential_app FOREIGN KEY (application_id)
        REFERENCES public.t_sys_openapi_application(id),
    CONSTRAINT uk_sys_openapi_credential_key_id UNIQUE (key_id),
    CONSTRAINT ck_sys_openapi_credential_encryption CHECK (encryption_algorithm IN ('AES_256_GCM', 'SM4_GCM'))
);
CREATE INDEX idx_sys_openapi_credential_app ON public.t_sys_openapi_credential(application_id);
COMMENT ON TABLE public.t_sys_openapi_credential IS 'OpenAPI 独立凭据包；三个密钥均使用部署密钥加密存储';

CREATE TABLE public.t_sys_openapi_release (
    id bigint PRIMARY KEY,
    api_number varchar(120) NOT NULL,
    api_version varchar(30) NOT NULL,
    operation_key varchar(200) NOT NULL,
    name varchar(200) NOT NULL,
    http_method varchar(10) NOT NULL,
    path varchar(500) NOT NULL,
    status varchar(20) DEFAULT 'DRAFT' NOT NULL,
    description varchar(1000),
    request_schema jsonb NOT NULL,
    response_schema jsonb NOT NULL,
    documentation text,
    system_preset boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    CONSTRAINT uk_sys_openapi_release_operation UNIQUE (operation_key),
    CONSTRAINT uk_sys_openapi_release_number_version UNIQUE (api_number, api_version),
    CONSTRAINT uk_sys_openapi_release_route UNIQUE (http_method, path),
    CONSTRAINT ck_sys_openapi_release_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'OFFLINE')),
    CONSTRAINT ck_sys_openapi_release_method CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH'))
);
COMMENT ON TABLE public.t_sys_openapi_release IS '由代码显式注册并通过迁移发布的 OpenAPI 版本目录';

CREATE TABLE public.t_sys_openapi_grant (
    id bigint PRIMARY KEY,
    application_id bigint NOT NULL,
    operation_key varchar(200) NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    CONSTRAINT fk_sys_openapi_grant_app FOREIGN KEY (application_id)
        REFERENCES public.t_sys_openapi_application(id),
    CONSTRAINT fk_sys_openapi_grant_operation FOREIGN KEY (operation_key)
        REFERENCES public.t_sys_openapi_release(operation_key),
    CONSTRAINT uk_sys_openapi_grant UNIQUE (application_id, operation_key)
);
COMMENT ON TABLE public.t_sys_openapi_grant IS '第三方应用的 API 操作授权';

CREATE TABLE public.t_sys_openapi_invocation_log (
    id bigint NOT NULL,
    request_time timestamp without time zone NOT NULL,
    application_id bigint,
    application_number varchar(100),
    credential_key_id varchar(100),
    operation_key varchar(200),
    request_id varchar(100),
    trace_id varchar(100),
    client_ip varchar(128),
    result_type varchar(30) NOT NULL,
    result_code integer,
    duration_ms bigint NOT NULL,
    request_bytes integer NOT NULL,
    response_bytes integer NOT NULL,
    error_message varchar(500),
    CONSTRAINT pk_sys_openapi_invocation_log PRIMARY KEY (request_time, id),
    CONSTRAINT ck_sys_openapi_invocation_result CHECK (result_type IN ('SUCCESS', 'AUTHENTICATION_FAILED', 'ACCESS_DENIED', 'BUSINESS_FAILED', 'SYSTEM_FAILED'))
) PARTITION BY RANGE (request_time);
CREATE INDEX idx_sys_openapi_log_app_time
    ON public.t_sys_openapi_invocation_log(application_id, request_time DESC, id DESC);
CREATE INDEX idx_sys_openapi_log_operation_time
    ON public.t_sys_openapi_invocation_log(operation_key, request_time DESC, id DESC);
COMMENT ON TABLE public.t_sys_openapi_invocation_log IS 'OpenAPI 调用审计，不保存请求、响应正文及密钥';

DO $partition_creation$
DECLARE
    partition_start date := DATE '2026-01-01';
    partition_end date;
    partition_name text;
BEGIN
    WHILE partition_start < DATE '2030-01-01' LOOP
        partition_end := (partition_start + INTERVAL '1 month')::date;
        partition_name := 't_sys_openapi_invocation_log_p' || to_char(partition_start, 'YYYYMM');
        EXECUTE format(
            'CREATE TABLE public.%I PARTITION OF public.t_sys_openapi_invocation_log FOR VALUES FROM (%L) TO (%L)',
            partition_name, partition_start, partition_end
        );
        partition_start := partition_end;
    END LOOP;
END
$partition_creation$;
CREATE TABLE public.t_sys_openapi_invocation_log_default
    PARTITION OF public.t_sys_openapi_invocation_log DEFAULT;

CREATE TABLE public.t_sys_openapi_invocation_log_history (
    LIKE public.t_sys_openapi_invocation_log INCLUDING DEFAULTS INCLUDING CONSTRAINTS
) PARTITION BY RANGE (request_time);
COMMENT ON TABLE public.t_sys_openapi_invocation_log_history IS 'OpenAPI 历史调用审计（月度分区父表）';

UPDATE public.t_sys_job
SET job_data = (job_data::jsonb || '{"openApiLogHotDays":180}'::jsonb)::text,
    update_time = now(), version = version + 1
WHERE number = 'SYSTEM_LOG_ARCHIVE';

UPDATE public.t_sys_job
SET job_data = (job_data::jsonb || '{"openApiLogRetentionDays":730}'::jsonb)::text,
    update_time = now(), version = version + 1
WHERE number = 'SYSTEM_LOG_HISTORY_PURGE';

INSERT INTO public.t_sys_openapi_release
    (id, api_number, api_version, operation_key, name, http_method, path, status, description,
     request_schema, response_schema, documentation, system_preset, create_time, version)
VALUES
    (520000000000000001, 'sys.basic-data.items', 'v1', 'sys.basicData.items.queryByCategory',
     '按分类获取基础数据信息', 'POST', '/openapi/sys-basic-data/v1/items/query', 'PUBLISHED',
     '返回分类及全部祖先均启用的叶子资料，不暴露内部数据库主键。',
     '{"type":"object","required":["categoryNumber"],"properties":{"categoryNumber":{"type":"string","maxLength":100}}}'::jsonb,
     '{"type":"object","properties":{"categoryNumber":{"type":"string"},"items":{"type":"array","items":{"type":"object","properties":{"number":{"type":"string"},"name":{"type":"string"},"parentNumber":{"type":["string","null"]},"numberPath":{"type":"string"},"namePath":{"type":"string"}}}}}}'::jsonb,
     '请求明文：`{"categoryNumber":"分类编码"}`。响应仅包含稳定业务编码、名称和路径。',
     true, now(), 0);

INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, create_time, version)
VALUES
    (520000000000000010, 'sys/base/openapi-application', 31, '第三方应用', 10, '第三方调用方、凭据和 API 授权', true, 'SYSTEM', now(), 0),
    (520000000000000020, 'sys/base/openapi-catalog', 31, 'API 文档', 20, '显式注册的 API 版本、协议和文档', true, 'SYSTEM', now(), 0),
    (520000000000000030, 'sys/base/openapi-invocation', 31, '调用监控', 30, 'OpenAPI 调用日志与统计', true, 'SYSTEM', now(), 0);

INSERT INTO public.t_sys_permission
    (id, name, number, create_time, version, feature_id, app_id)
VALUES
    (520000000000000101, '第三方应用-查询', 'sys:base:openapi-application:listPage', now(), 0, 520000000000000010, NULL),
    (520000000000000102, '第三方应用-详情', 'sys:base:openapi-application:detail', now(), 0, 520000000000000010, NULL),
    (520000000000000103, '第三方应用-保存', 'sys:base:openapi-application:save', now(), 0, 520000000000000010, NULL),
    (520000000000000104, '第三方应用-启停', 'sys:base:openapi-application:enable', now(), 0, 520000000000000010, NULL),
    (520000000000000105, '第三方应用-凭据管理', 'sys:base:openapi-application:credential', now(), 0, 520000000000000010, NULL),
    (520000000000000106, '第三方应用-API授权', 'sys:base:openapi-application:grant', now(), 0, 520000000000000010, NULL),
    (520000000000000201, 'API文档-查询', 'sys:base:openapi-catalog:listPage', now(), 0, 520000000000000020, NULL),
    (520000000000000202, 'API版本-发布管理', 'sys:base:openapi-catalog:publish', now(), 0, 520000000000000020, NULL),
    (520000000000000301, '调用监控-查询', 'sys:base:openapi-invocation:listPage', now(), 0, 520000000000000030, NULL);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, permission_id, path, component, icon,
     description, sort, enabled, create_time, version, feature_id, target_type)
VALUES
    (520000000000000400, 'openapi_platform', '开放平台', 0, 0, 31, 10030, NULL, NULL,
     'ApiOutlined', '第三方应用、API 文档与调用监控', 60, true, now(), 0, NULL, NULL),
    (520000000000000401, 'openapi_application', '第三方应用', 1, 520000000000000400, 31,
     520000000000000101, '/sys/base/openapi-application', 'sys/base/openapi-application',
     'AppstoreAddOutlined', '维护调用方、访问策略、凭据和授权', 10, true, now(), 0,
     520000000000000010, 'INTERNAL_PAGE'),
    (520000000000000402, 'openapi_catalog', 'API 文档', 1, 520000000000000400, 31,
     520000000000000201, '/sys/base/openapi-catalog', 'sys/base/openapi-catalog',
     'FileTextOutlined', '查看 API 版本、协议与报文结构', 20, true, now(), 0,
     520000000000000020, 'INTERNAL_PAGE'),
    (520000000000000403, 'openapi_invocation', '调用监控', 1, 520000000000000400, 31,
     520000000000000301, '/sys/base/openapi-invocation', 'sys/base/openapi-invocation',
     'LineChartOutlined', '查看调用日志和统计', 30, true, now(), 0,
     520000000000000030, 'INTERNAL_PAGE');

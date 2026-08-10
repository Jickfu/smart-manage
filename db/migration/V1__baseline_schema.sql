--
-- PostgreSQL database dump
--


-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_calendars (
    sched_name character varying(120) NOT NULL,
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);


--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_cron_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);


--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_fired_triggers (
    sched_name character varying(120) NOT NULL,
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_nonconcurrent boolean,
    requests_recovery boolean
);


--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_job_details (
    sched_name character varying(120) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_nonconcurrent boolean NOT NULL,
    is_update_data boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);


--
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_locks (
    sched_name character varying(120) NOT NULL,
    lock_name character varying(40) NOT NULL
);


--
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_paused_trigger_grps (
    sched_name character varying(120) NOT NULL,
    trigger_group character varying(200) NOT NULL
);


--
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_scheduler_state (
    sched_name character varying(120) NOT NULL,
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_simple_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


--
-- Name: qrtz_simprop_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_simprop_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);


--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);


--
-- Name: t_scm_purchase_requisition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_scm_purchase_requisition (
    id bigint NOT NULL,
    number character varying(64) NOT NULL,
    subject character varying(255) NOT NULL,
    org_id bigint NOT NULL,
    applicant_id bigint NOT NULL,
    biz_date date NOT NULL,
    required_date date,
    reason character varying(1000),
    bill_status character(1) DEFAULT 'A'::bpchar NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_scm_purchase_requisition_status CHECK ((bill_status = ANY (ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar])))
);


--
-- Name: TABLE t_scm_purchase_requisition; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_scm_purchase_requisition IS '采购申请';


--
-- Name: COLUMN t_scm_purchase_requisition.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.id IS 'ID';


--
-- Name: COLUMN t_scm_purchase_requisition.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.number IS '编码';


--
-- Name: COLUMN t_scm_purchase_requisition.subject; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.subject IS '主题';


--
-- Name: COLUMN t_scm_purchase_requisition.org_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.org_id IS '单据所属组织ID';


--
-- Name: COLUMN t_scm_purchase_requisition.applicant_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.applicant_id IS '申请人ID';


--
-- Name: COLUMN t_scm_purchase_requisition.biz_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.biz_date IS '业务日期';


--
-- Name: COLUMN t_scm_purchase_requisition.required_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.required_date IS '需求日期';


--
-- Name: COLUMN t_scm_purchase_requisition.reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.reason IS '申请原因';


--
-- Name: COLUMN t_scm_purchase_requisition.bill_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.bill_status IS '单据状态：A暂存，B已提交，C审核通过，D已关闭';


--
-- Name: COLUMN t_scm_purchase_requisition.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.version IS '乐观锁版本号';


--
-- Name: COLUMN t_scm_purchase_requisition.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.create_time IS '创建时间';


--
-- Name: COLUMN t_scm_purchase_requisition.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.update_time IS '更新时间';


--
-- Name: COLUMN t_scm_purchase_requisition.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.create_user IS '创建人';


--
-- Name: COLUMN t_scm_purchase_requisition.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition.update_user IS '修改人';


--
-- Name: t_scm_purchase_requisition_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_scm_purchase_requisition_entry (
    id bigint NOT NULL,
    parent_id bigint NOT NULL,
    material_name character varying(255) NOT NULL,
    specification character varying(255),
    unit character varying(32) NOT NULL,
    quantity numeric(19,6) NOT NULL,
    required_date date,
    remark character varying(500),
    sort integer DEFAULT 99 NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_scm_purchase_requisition_entry_quantity CHECK ((quantity > (0)::numeric))
);


--
-- Name: TABLE t_scm_purchase_requisition_entry; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_scm_purchase_requisition_entry IS '采购申请明细';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.id IS 'ID';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.parent_id IS '父级ID';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.material_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.material_name IS '物料名称';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.specification; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.specification IS '规格型号';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.unit IS '单位';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.quantity IS '数量';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.required_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.required_date IS '需求日期';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.remark IS '备注';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.sort IS '排序';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.create_time IS '创建时间';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.update_time IS '更新时间';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.create_user IS '创建人';


--
-- Name: COLUMN t_scm_purchase_requisition_entry.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_scm_purchase_requisition_entry.update_user IS '修改人';


--
-- Name: t_sys_app; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_app (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    icon character varying(255) NOT NULL,
    seq integer DEFAULT 99,
    description character varying(255),
    cloud_id bigint NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone DEFAULT now(),
    icon_color character varying(32),
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_app; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_app IS '应用';


--
-- Name: COLUMN t_sys_app.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.id IS 'ID';


--
-- Name: COLUMN t_sys_app.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.name IS '名称';


--
-- Name: COLUMN t_sys_app.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.number IS '编码';


--
-- Name: COLUMN t_sys_app.icon; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.icon IS '图标';


--
-- Name: COLUMN t_sys_app.seq; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.seq IS '排序';


--
-- Name: COLUMN t_sys_app.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.description IS '描述';


--
-- Name: COLUMN t_sys_app.cloud_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.cloud_id IS '所属云ID';


--
-- Name: COLUMN t_sys_app.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_app.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_app.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_app.icon_color; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.icon_color IS '图标颜色';


--
-- Name: COLUMN t_sys_app.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.create_user IS '创建人';


--
-- Name: COLUMN t_sys_app.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.update_user IS '修改人';


--
-- Name: COLUMN t_sys_app.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_app.version IS '乐观锁版本号';


--
-- Name: t_sys_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_attachment (
    id bigint NOT NULL,
    original_name character varying(500) NOT NULL,
    file_size bigint,
    mime_type character varying(200),
    file_ext character varying(50),
    storage_type character varying(20) NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    object_key text NOT NULL,
    status character varying(30) NOT NULL,
    upload_session_id character varying(64),
    expires_at timestamp without time zone,
    sha256 character varying(64),
    CONSTRAINT ck_sys_attachment_status CHECK (((status)::text = ANY ((ARRAY['TEMP'::character varying, 'ACTIVE'::character varying, 'PENDING_DELETE'::character varying, 'DELETED'::character varying])::text[])))
);


--
-- Name: TABLE t_sys_attachment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_attachment IS '附件';


--
-- Name: COLUMN t_sys_attachment.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.id IS 'ID';


--
-- Name: COLUMN t_sys_attachment.original_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.original_name IS '原始文件名';


--
-- Name: COLUMN t_sys_attachment.file_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.file_size IS '文件大小';


--
-- Name: COLUMN t_sys_attachment.mime_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.mime_type IS 'MIME类型';


--
-- Name: COLUMN t_sys_attachment.file_ext; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.file_ext IS '文件扩展名';


--
-- Name: COLUMN t_sys_attachment.storage_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.storage_type IS '存储方式';


--
-- Name: COLUMN t_sys_attachment.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_attachment.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_attachment.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.create_user IS '创建人';


--
-- Name: COLUMN t_sys_attachment.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.update_user IS '修改人';


--
-- Name: COLUMN t_sys_attachment.object_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.object_key IS '稳定对象键或兼容存储定位键';


--
-- Name: COLUMN t_sys_attachment.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.status IS '生命周期状态';


--
-- Name: COLUMN t_sys_attachment.upload_session_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.upload_session_id IS '上传会话ID';


--
-- Name: COLUMN t_sys_attachment.expires_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.expires_at IS '临时附件过期时间';


--
-- Name: COLUMN t_sys_attachment.sha256; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment.sha256 IS '文件SHA-256摘要';


--
-- Name: t_sys_attachment_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_attachment_config (
    id bigint NOT NULL,
    max_upload_bytes bigint NOT NULL,
    allowed_extensions character varying(2000) NOT NULL,
    allowed_mime_types character varying(2000) NOT NULL,
    temp_expire_hours integer NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_sys_attachment_config_max_upload_bytes CHECK ((max_upload_bytes > 0)),
    CONSTRAINT ck_sys_attachment_config_temp_expire_hours CHECK (((temp_expire_hours >= 1) AND (temp_expire_hours <= 168)))
);


--
-- Name: TABLE t_sys_attachment_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_attachment_config IS '附件全局限制配置';


--
-- Name: COLUMN t_sys_attachment_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.id IS 'ID';


--
-- Name: COLUMN t_sys_attachment_config.max_upload_bytes; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.max_upload_bytes IS '单文件最大字节数';


--
-- Name: COLUMN t_sys_attachment_config.allowed_extensions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.allowed_extensions IS '允许扩展名，逗号分隔';


--
-- Name: COLUMN t_sys_attachment_config.allowed_mime_types; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.allowed_mime_types IS '允许MIME类型，逗号分隔';


--
-- Name: COLUMN t_sys_attachment_config.temp_expire_hours; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.temp_expire_hours IS '临时附件有效小时数';


--
-- Name: COLUMN t_sys_attachment_config.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_attachment_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_attachment_config.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_attachment_config.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.create_user IS '创建人';


--
-- Name: COLUMN t_sys_attachment_config.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_attachment_config.update_user IS '修改人';


--
-- Name: t_sys_basic_data_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_basic_data_category (
    id bigint NOT NULL,
    number character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    remark character varying(255),
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    cloud_id bigint NOT NULL,
    system_preset boolean DEFAULT false NOT NULL
);


--
-- Name: TABLE t_sys_basic_data_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_basic_data_category IS '基础资料分类';


--
-- Name: COLUMN t_sys_basic_data_category.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.id IS 'ID';


--
-- Name: COLUMN t_sys_basic_data_category.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.number IS '编码';


--
-- Name: COLUMN t_sys_basic_data_category.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.name IS '名称';


--
-- Name: COLUMN t_sys_basic_data_category.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.remark IS '备注';


--
-- Name: COLUMN t_sys_basic_data_category.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_basic_data_category.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_basic_data_category.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_basic_data_category.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.create_user IS '创建人';


--
-- Name: COLUMN t_sys_basic_data_category.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.update_user IS '修改人';


--
-- Name: COLUMN t_sys_basic_data_category.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_basic_data_category.cloud_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.cloud_id IS '所属云ID';


--
-- Name: COLUMN t_sys_basic_data_category.system_preset; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_category.system_preset IS '是否系统预置';


--
-- Name: t_sys_basic_data_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_basic_data_item (
    id bigint NOT NULL,
    category_id bigint NOT NULL,
    number character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    sort integer DEFAULT 0 NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    parent_id bigint,
    remark character varying(255),
    system_preset boolean DEFAULT false NOT NULL,
    level integer DEFAULT 1 NOT NULL,
    number_path character varying(1000) NOT NULL,
    name_path character varying(2000) NOT NULL,
    is_leaf boolean DEFAULT true NOT NULL,
    mutex integer DEFAULT 0 NOT NULL,
    CONSTRAINT ck_basic_data_item_level CHECK ((level >= 1))
);


--
-- Name: TABLE t_sys_basic_data_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_basic_data_item IS '基础资料节点';


--
-- Name: COLUMN t_sys_basic_data_item.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.id IS 'ID';


--
-- Name: COLUMN t_sys_basic_data_item.category_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.category_id IS '基础资料分类ID';


--
-- Name: COLUMN t_sys_basic_data_item.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.number IS '选项编码，在同一基础数据下唯一';


--
-- Name: COLUMN t_sys_basic_data_item.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.name IS '选项显示名称';


--
-- Name: COLUMN t_sys_basic_data_item.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.sort IS '排序';


--
-- Name: COLUMN t_sys_basic_data_item.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_basic_data_item.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_basic_data_item.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_basic_data_item.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.create_user IS '创建人';


--
-- Name: COLUMN t_sys_basic_data_item.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.update_user IS '修改人';


--
-- Name: COLUMN t_sys_basic_data_item.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.parent_id IS '上级基础资料ID';


--
-- Name: COLUMN t_sys_basic_data_item.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.remark IS '备注';


--
-- Name: COLUMN t_sys_basic_data_item.system_preset; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.system_preset IS '是否系统预置';


--
-- Name: COLUMN t_sys_basic_data_item.level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.level IS '级次';


--
-- Name: COLUMN t_sys_basic_data_item.number_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.number_path IS '长编码';


--
-- Name: COLUMN t_sys_basic_data_item.name_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.name_path IS '长名称';


--
-- Name: COLUMN t_sys_basic_data_item.is_leaf; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.is_leaf IS '是否叶子节点';


--
-- Name: COLUMN t_sys_basic_data_item.mutex; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_basic_data_item.mutex IS '乐观锁版本号';


--
-- Name: t_sys_biz_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_biz_attachment (
    id bigint NOT NULL,
    biz_type character varying(100) NOT NULL,
    biz_id character varying(64),
    attachment_id bigint NOT NULL,
    sort integer DEFAULT 0,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: TABLE t_sys_biz_attachment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_biz_attachment IS '业务附件关联';


--
-- Name: COLUMN t_sys_biz_attachment.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.id IS 'ID';


--
-- Name: COLUMN t_sys_biz_attachment.biz_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.biz_type IS '业务类型';


--
-- Name: COLUMN t_sys_biz_attachment.biz_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.biz_id IS '业务ID';


--
-- Name: COLUMN t_sys_biz_attachment.attachment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.attachment_id IS '附件ID';


--
-- Name: COLUMN t_sys_biz_attachment.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.sort IS '排序';


--
-- Name: COLUMN t_sys_biz_attachment.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_biz_attachment.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_biz_attachment.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.create_user IS '创建人';


--
-- Name: COLUMN t_sys_biz_attachment.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_biz_attachment.update_user IS '修改人';


--
-- Name: t_sys_cloud; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_cloud (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    seq integer DEFAULT 99,
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_cloud; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_cloud IS '云（应用分组）';


--
-- Name: COLUMN t_sys_cloud.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.id IS 'ID';


--
-- Name: COLUMN t_sys_cloud.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.name IS '名称';


--
-- Name: COLUMN t_sys_cloud.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.number IS '编码';


--
-- Name: COLUMN t_sys_cloud.seq; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.seq IS '排序';


--
-- Name: COLUMN t_sys_cloud.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_cloud.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_cloud.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_cloud.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.create_user IS '创建人';


--
-- Name: COLUMN t_sys_cloud.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.update_user IS '修改人';


--
-- Name: COLUMN t_sys_cloud.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_cloud.version IS '乐观锁版本号';


--
-- Name: t_sys_file_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_file_config (
    id bigint NOT NULL,
    storage_type character varying(20) DEFAULT 'LOCAL'::character varying NOT NULL,
    local_dir character varying(500),
    ftp_host character varying(200),
    ftp_port integer DEFAULT 21,
    ftp_username character varying(200),
    ftp_dir character varying(500),
    ftp_passive_mode boolean DEFAULT true,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    ftp_password_cipher character varying(1000),
    version integer DEFAULT 0 NOT NULL,
    s3_endpoint character varying(500),
    s3_region character varying(100),
    s3_bucket character varying(255),
    s3_access_key character varying(255),
    s3_secret_key_cipher text,
    s3_path_style boolean DEFAULT true NOT NULL
);


--
-- Name: TABLE t_sys_file_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_file_config IS '文件存储配置';


--
-- Name: COLUMN t_sys_file_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.id IS 'ID';


--
-- Name: COLUMN t_sys_file_config.storage_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.storage_type IS '存储方式';


--
-- Name: COLUMN t_sys_file_config.local_dir; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.local_dir IS '本地存储目录';


--
-- Name: COLUMN t_sys_file_config.ftp_host; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_host IS 'FTP主机';


--
-- Name: COLUMN t_sys_file_config.ftp_port; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_port IS 'FTP端口';


--
-- Name: COLUMN t_sys_file_config.ftp_username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_username IS 'FTP用户名';


--
-- Name: COLUMN t_sys_file_config.ftp_dir; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_dir IS 'FTP目录';


--
-- Name: COLUMN t_sys_file_config.ftp_passive_mode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_passive_mode IS 'FTP被动模式';


--
-- Name: COLUMN t_sys_file_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_file_config.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_file_config.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.create_user IS '创建人';


--
-- Name: COLUMN t_sys_file_config.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.update_user IS '修改人';


--
-- Name: COLUMN t_sys_file_config.ftp_password_cipher; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.ftp_password_cipher IS 'FTP密码密文';


--
-- Name: COLUMN t_sys_file_config.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_file_config.s3_endpoint; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_endpoint IS 'S3兼容服务地址';


--
-- Name: COLUMN t_sys_file_config.s3_region; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_region IS 'S3区域';


--
-- Name: COLUMN t_sys_file_config.s3_bucket; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_bucket IS 'S3私有Bucket';


--
-- Name: COLUMN t_sys_file_config.s3_access_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_access_key IS 'S3 Access Key';


--
-- Name: COLUMN t_sys_file_config.s3_secret_key_cipher; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_secret_key_cipher IS 'S3 Secret Key密文';


--
-- Name: COLUMN t_sys_file_config.s3_path_style; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_file_config.s3_path_style IS '是否使用Path Style';


--
-- Name: t_sys_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job (
    id bigint NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) DEFAULT 'DEFAULT'::character varying NOT NULL,
    description character varying(500),
    job_class_name character varying(500) NOT NULL,
    cron_expression character varying(100) NOT NULL,
    job_data text,
    status character varying(20) DEFAULT 'ENABLED'::character varying NOT NULL,
    remark character varying(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    number character varying(100) NOT NULL,
    is_system boolean DEFAULT false,
    version integer DEFAULT 0 NOT NULL,
    mutex_key character varying(100)
);


--
-- Name: TABLE t_sys_job; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_job IS '定时任务';


--
-- Name: COLUMN t_sys_job.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.id IS 'ID';


--
-- Name: COLUMN t_sys_job.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.job_name IS '任务名称';


--
-- Name: COLUMN t_sys_job.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.job_group IS '任务组';


--
-- Name: COLUMN t_sys_job.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.description IS '描述';


--
-- Name: COLUMN t_sys_job.job_class_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.job_class_name IS '执行类名';


--
-- Name: COLUMN t_sys_job.cron_expression; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.cron_expression IS 'Cron表达式';


--
-- Name: COLUMN t_sys_job.job_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.job_data IS '任务参数';


--
-- Name: COLUMN t_sys_job.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.status IS '状态';


--
-- Name: COLUMN t_sys_job.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.remark IS '备注';


--
-- Name: COLUMN t_sys_job.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_job.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_job.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.create_user IS '创建人';


--
-- Name: COLUMN t_sys_job.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.update_user IS '修改人';


--
-- Name: COLUMN t_sys_job.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.number IS '编码';


--
-- Name: COLUMN t_sys_job.is_system; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.is_system IS '是否系统内置';


--
-- Name: COLUMN t_sys_job.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_job.mutex_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job.mutex_key IS '共享资源互斥键；不同任务使用相同键时，同一时刻只执行一个';


--
-- Name: t_sys_job_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
)
PARTITION BY RANGE (start_time);


--
-- Name: TABLE t_sys_job_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_job_log IS '定时任务执行实例（在线分区父表）';


--
-- Name: COLUMN t_sys_job_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.id IS 'ID';


--
-- Name: COLUMN t_sys_job_log.job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.job_id IS '任务ID';


--
-- Name: COLUMN t_sys_job_log.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.job_name IS '任务名称';


--
-- Name: COLUMN t_sys_job_log.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.job_group IS '任务组';


--
-- Name: COLUMN t_sys_job_log.start_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.start_time IS '开始时间';


--
-- Name: COLUMN t_sys_job_log.end_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.end_time IS '结束时间';


--
-- Name: COLUMN t_sys_job_log.duration_ms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.duration_ms IS '耗时(ms)';


--
-- Name: COLUMN t_sys_job_log.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.status IS '状态';


--
-- Name: COLUMN t_sys_job_log.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_job_log.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_job_log.instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.instance_id IS '实际执行应用实例（Quartz instanceId）';


--
-- Name: COLUMN t_sys_job_log.fire_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.fire_instance_id IS 'Quartz本次触发实例ID';


--
-- Name: COLUMN t_sys_job_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log.create_time IS '创建时间';


--
-- Name: t_sys_job_log_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_default (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_history (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
)
PARTITION BY RANGE (start_time);


--
-- Name: TABLE t_sys_job_log_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_job_log_history IS '定时任务执行实例（历史分区父表）';


--
-- Name: COLUMN t_sys_job_log_history.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.id IS 'ID';


--
-- Name: COLUMN t_sys_job_log_history.job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.job_id IS '任务ID';


--
-- Name: COLUMN t_sys_job_log_history.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.job_name IS '任务名称';


--
-- Name: COLUMN t_sys_job_log_history.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.job_group IS '任务组';


--
-- Name: COLUMN t_sys_job_log_history.start_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.start_time IS '开始时间';


--
-- Name: COLUMN t_sys_job_log_history.end_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.end_time IS '结束时间';


--
-- Name: COLUMN t_sys_job_log_history.duration_ms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.duration_ms IS '耗时(ms)';


--
-- Name: COLUMN t_sys_job_log_history.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.status IS '状态';


--
-- Name: COLUMN t_sys_job_log_history.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_job_log_history.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_job_log_history.instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.instance_id IS '实际执行应用实例（Quartz instanceId）';


--
-- Name: COLUMN t_sys_job_log_history.fire_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.fire_instance_id IS 'Quartz本次触发实例ID';


--
-- Name: COLUMN t_sys_job_log_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_job_log_history.create_time IS '创建时间';


--
-- Name: t_sys_job_log_p202601; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202601 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202602; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202602 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202603; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202603 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202604; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202604 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202605; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202605 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202606; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202606 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202607; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202607 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202608; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202608 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202609; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202609 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202610; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202610 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202611; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202611 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202612; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202612 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202701; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202701 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202702; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202702 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202703; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202703 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202704; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202704 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202705; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202705 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202706; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202706 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202707; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202707 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202708; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202708 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202709; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202709 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202710; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202710 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202711; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202711 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202712; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202712 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202801; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202801 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202802; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202802 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202803; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202803 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202804; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202804 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202805; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202805 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202806; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202806 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202807; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202807 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202808; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202808 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202809; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202809 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202810; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202810 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202811; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202811 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202812; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202812 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202901; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202901 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202902; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202902 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202903; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202903 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202904; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202904 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202905; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202905 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202906; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202906 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202907; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202907 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202908; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202908 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202909; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202909 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202910; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202910 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202911; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202911 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_job_log_p202912; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_job_log_p202912 (
    id bigint NOT NULL,
    job_id bigint,
    job_name character varying(200),
    job_group character varying(200),
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    duration_ms bigint,
    status character varying(20),
    error_message text,
    trace_id character varying(64),
    instance_id character varying(200),
    fire_instance_id character varying(200),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_login_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_login_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_login_log IS '系统服务-登录登出日志（在线分区父表）';


--
-- Name: COLUMN t_sys_login_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.id IS 'ID';


--
-- Name: COLUMN t_sys_login_log.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.user_id IS '用户ID';


--
-- Name: COLUMN t_sys_login_log.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.username IS '用户名';


--
-- Name: COLUMN t_sys_login_log.nickname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.nickname IS '昵称';


--
-- Name: COLUMN t_sys_login_log.event_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.event_type IS '事件类型';


--
-- Name: COLUMN t_sys_login_log.success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.success IS '是否成功';


--
-- Name: COLUMN t_sys_login_log.fail_reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.fail_reason IS '失败原因';


--
-- Name: COLUMN t_sys_login_log.ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.ip IS 'IP地址';


--
-- Name: COLUMN t_sys_login_log.user_agent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.user_agent IS 'User-Agent';


--
-- Name: COLUMN t_sys_login_log.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_login_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_login_log.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_login_log.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.create_user IS '创建人';


--
-- Name: COLUMN t_sys_login_log.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log.update_user IS '修改人';


--
-- Name: t_sys_login_log_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_default (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_history (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_login_log_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_login_log_history IS '系统服务-登录登出日志（历史分区父表）';


--
-- Name: COLUMN t_sys_login_log_history.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.id IS 'ID';


--
-- Name: COLUMN t_sys_login_log_history.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.user_id IS '用户ID';


--
-- Name: COLUMN t_sys_login_log_history.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.username IS '用户名';


--
-- Name: COLUMN t_sys_login_log_history.nickname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.nickname IS '昵称';


--
-- Name: COLUMN t_sys_login_log_history.event_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.event_type IS '事件类型';


--
-- Name: COLUMN t_sys_login_log_history.success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.success IS '是否成功';


--
-- Name: COLUMN t_sys_login_log_history.fail_reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.fail_reason IS '失败原因';


--
-- Name: COLUMN t_sys_login_log_history.ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.ip IS 'IP地址';


--
-- Name: COLUMN t_sys_login_log_history.user_agent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.user_agent IS 'User-Agent';


--
-- Name: COLUMN t_sys_login_log_history.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_login_log_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_login_log_history.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_login_log_history.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.create_user IS '创建人';


--
-- Name: COLUMN t_sys_login_log_history.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_login_log_history.update_user IS '修改人';


--
-- Name: t_sys_login_log_p202601; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202601 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202602; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202602 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202603; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202603 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202604; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202604 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202605; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202605 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202606; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202606 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202607; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202607 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202608; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202608 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202609; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202609 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202610; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202610 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202611; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202611 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202612; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202612 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202701; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202701 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202702; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202702 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202703; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202703 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202704; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202704 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202705; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202705 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202706; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202706 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202707; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202707 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202708; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202708 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202709; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202709 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202710; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202710 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202711; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202711 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202712; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202712 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202801; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202801 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202802; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202802 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202803; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202803 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202804; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202804 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202805; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202805 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202806; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202806 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202807; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202807 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202808; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202808 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202809; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202809 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202810; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202810 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202811; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202811 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202812; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202812 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202901; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202901 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202902; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202902 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202903; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202903 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202904; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202904 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202905; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202905 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202906; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202906 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202907; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202907 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202908; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202908 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202909; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202909 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202910; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202910 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202911; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202911 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_login_log_p202912; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_login_log_p202912 (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(128),
    nickname character varying(255),
    event_type character varying(32) NOT NULL,
    success boolean DEFAULT true NOT NULL,
    fail_reason character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_menu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_menu (
    id bigint NOT NULL,
    number character varying(255),
    name character varying(255) NOT NULL,
    level integer NOT NULL,
    parent_id bigint DEFAULT 0 NOT NULL,
    app_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    path character varying(512),
    component character varying(512),
    icon character varying(255),
    description character varying(512),
    sort integer DEFAULT 99,
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_menu; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_menu IS '菜单';


--
-- Name: COLUMN t_sys_menu.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.id IS 'ID';


--
-- Name: COLUMN t_sys_menu.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.number IS '编码';


--
-- Name: COLUMN t_sys_menu.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.name IS '名称';


--
-- Name: COLUMN t_sys_menu.level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.level IS '级别';


--
-- Name: COLUMN t_sys_menu.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.parent_id IS '父级ID';


--
-- Name: COLUMN t_sys_menu.app_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.app_id IS '所属应用ID';


--
-- Name: COLUMN t_sys_menu.permission_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.permission_id IS '权限ID';


--
-- Name: COLUMN t_sys_menu.path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.path IS '路径';


--
-- Name: COLUMN t_sys_menu.component; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.component IS '组件';


--
-- Name: COLUMN t_sys_menu.icon; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.icon IS '图标';


--
-- Name: COLUMN t_sys_menu.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.description IS '描述';


--
-- Name: COLUMN t_sys_menu.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.sort IS '排序';


--
-- Name: COLUMN t_sys_menu.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_menu.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_menu.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_menu.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.create_user IS '创建人';


--
-- Name: COLUMN t_sys_menu.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.update_user IS '修改人';


--
-- Name: COLUMN t_sys_menu.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_menu.version IS '乐观锁版本';


--
-- Name: t_sys_operate_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_operate_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_operate_log IS '系统服务-操作日志（在线分区父表）';


--
-- Name: COLUMN t_sys_operate_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.id IS 'ID';


--
-- Name: COLUMN t_sys_operate_log.biz_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.biz_name IS '业务名称';


--
-- Name: COLUMN t_sys_operate_log.success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.success IS '是否成功';


--
-- Name: COLUMN t_sys_operate_log.error_msg; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.error_msg IS '错误信息';


--
-- Name: COLUMN t_sys_operate_log.request_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.request_method IS '请求方法';


--
-- Name: COLUMN t_sys_operate_log.request_uri; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.request_uri IS '请求URI';


--
-- Name: COLUMN t_sys_operate_log.ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.ip IS 'IP地址';


--
-- Name: COLUMN t_sys_operate_log.user_agent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.user_agent IS 'User-Agent';


--
-- Name: COLUMN t_sys_operate_log.class_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.class_name IS '类名';


--
-- Name: COLUMN t_sys_operate_log.method_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.method_name IS '方法名';


--
-- Name: COLUMN t_sys_operate_log.duration_ms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.duration_ms IS '耗时(ms)';


--
-- Name: COLUMN t_sys_operate_log.request_params; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.request_params IS '请求参数';


--
-- Name: COLUMN t_sys_operate_log.response_body; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.response_body IS '响应内容';


--
-- Name: COLUMN t_sys_operate_log.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.user_id IS '用户ID';


--
-- Name: COLUMN t_sys_operate_log.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.username IS '用户名';


--
-- Name: COLUMN t_sys_operate_log.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_operate_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_operate_log.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_operate_log.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.create_user IS '创建人';


--
-- Name: COLUMN t_sys_operate_log.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log.update_user IS '修改人';


--
-- Name: t_sys_operate_log_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_default (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_history (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_operate_log_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_operate_log_history IS '系统服务-操作日志（历史分区父表）';


--
-- Name: COLUMN t_sys_operate_log_history.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.id IS 'ID';


--
-- Name: COLUMN t_sys_operate_log_history.biz_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.biz_name IS '业务名称';


--
-- Name: COLUMN t_sys_operate_log_history.success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.success IS '是否成功';


--
-- Name: COLUMN t_sys_operate_log_history.error_msg; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.error_msg IS '错误信息';


--
-- Name: COLUMN t_sys_operate_log_history.request_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.request_method IS '请求方法';


--
-- Name: COLUMN t_sys_operate_log_history.request_uri; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.request_uri IS '请求URI';


--
-- Name: COLUMN t_sys_operate_log_history.ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.ip IS 'IP地址';


--
-- Name: COLUMN t_sys_operate_log_history.user_agent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.user_agent IS 'User-Agent';


--
-- Name: COLUMN t_sys_operate_log_history.class_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.class_name IS '类名';


--
-- Name: COLUMN t_sys_operate_log_history.method_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.method_name IS '方法名';


--
-- Name: COLUMN t_sys_operate_log_history.duration_ms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.duration_ms IS '耗时(ms)';


--
-- Name: COLUMN t_sys_operate_log_history.request_params; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.request_params IS '请求参数';


--
-- Name: COLUMN t_sys_operate_log_history.response_body; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.response_body IS '响应内容';


--
-- Name: COLUMN t_sys_operate_log_history.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.user_id IS '用户ID';


--
-- Name: COLUMN t_sys_operate_log_history.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.username IS '用户名';


--
-- Name: COLUMN t_sys_operate_log_history.trace_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.trace_id IS '链路追踪ID';


--
-- Name: COLUMN t_sys_operate_log_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_operate_log_history.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_operate_log_history.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.create_user IS '创建人';


--
-- Name: COLUMN t_sys_operate_log_history.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_operate_log_history.update_user IS '修改人';


--
-- Name: t_sys_operate_log_p202601; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202601 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202602; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202602 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202603; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202603 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202604; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202604 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202605; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202605 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202606; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202606 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202607; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202607 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202608; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202608 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202609; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202609 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202610; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202610 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202611; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202611 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202612; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202612 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202701; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202701 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202702; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202702 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202703; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202703 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202704; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202704 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202705; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202705 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202706; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202706 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202707; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202707 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202708; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202708 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202709; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202709 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202710; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202710 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202711; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202711 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202712; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202712 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202801; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202801 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202802; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202802 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202803; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202803 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202804; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202804 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202805; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202805 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202806; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202806 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202807; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202807 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202808; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202808 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202809; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202809 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202810; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202810 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202811; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202811 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202812; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202812 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202901; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202901 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202902; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202902 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202903; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202903 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202904; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202904 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202905; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202905 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202906; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202906 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202907; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202907 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202908; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202908 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202909; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202909 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202910; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202910 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202911; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202911 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_operate_log_p202912; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_operate_log_p202912 (
    id bigint NOT NULL,
    biz_name character varying(256),
    success boolean DEFAULT true NOT NULL,
    error_msg text,
    request_method character varying(32),
    request_uri character varying(512),
    ip character varying(64),
    user_agent character varying(1024),
    class_name character varying(256),
    method_name character varying(128),
    duration_ms bigint,
    request_params text,
    response_body text,
    user_id bigint,
    username character varying(128),
    trace_id character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: t_sys_org; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_org (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    parent_id bigint DEFAULT 0 NOT NULL,
    sort integer DEFAULT 99,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    update_user bigint
);


--
-- Name: TABLE t_sys_org; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_org IS '组织';


--
-- Name: COLUMN t_sys_org.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.id IS 'ID';


--
-- Name: COLUMN t_sys_org.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.name IS '名称';


--
-- Name: COLUMN t_sys_org.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.number IS '编码';


--
-- Name: COLUMN t_sys_org.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.parent_id IS '父级ID';


--
-- Name: COLUMN t_sys_org.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.sort IS '排序';


--
-- Name: COLUMN t_sys_org.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_org.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_org.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.create_user IS '创建人';


--
-- Name: COLUMN t_sys_org.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_org.update_user IS '修改人';


--
-- Name: t_sys_param; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_param (
    id bigint NOT NULL,
    number character varying(100) NOT NULL,
    name character varying(200) NOT NULL,
    value text,
    remark character varying(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    is_system boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    app_id bigint
);


--
-- Name: TABLE t_sys_param; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_param IS '系统参数';


--
-- Name: COLUMN t_sys_param.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.id IS 'ID';


--
-- Name: COLUMN t_sys_param.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.number IS '编码';


--
-- Name: COLUMN t_sys_param.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.name IS '名称';


--
-- Name: COLUMN t_sys_param.value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.value IS '参数值';


--
-- Name: COLUMN t_sys_param.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.remark IS '备注';


--
-- Name: COLUMN t_sys_param.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_param.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_param.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.create_user IS '创建人';


--
-- Name: COLUMN t_sys_param.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.update_user IS '修改人';


--
-- Name: COLUMN t_sys_param.is_system; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.is_system IS '是否系统内置';


--
-- Name: COLUMN t_sys_param.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_param.app_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_param.app_id IS '所属应用ID';


--
-- Name: t_sys_permission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_permission (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    app_id bigint NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_permission; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_permission IS '权限';


--
-- Name: COLUMN t_sys_permission.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.id IS 'ID';


--
-- Name: COLUMN t_sys_permission.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.name IS '名称';


--
-- Name: COLUMN t_sys_permission.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.number IS '编码';


--
-- Name: COLUMN t_sys_permission.app_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.app_id IS '所属应用ID';


--
-- Name: COLUMN t_sys_permission.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_permission.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_permission.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.create_user IS '创建人';


--
-- Name: COLUMN t_sys_permission.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.update_user IS '修改人';


--
-- Name: COLUMN t_sys_permission.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_permission.version IS '乐观锁版本号';


--
-- Name: t_sys_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_role (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_role IS '角色';


--
-- Name: COLUMN t_sys_role.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.id IS 'ID';


--
-- Name: COLUMN t_sys_role.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.name IS '名称';


--
-- Name: COLUMN t_sys_role.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.number IS '编码';


--
-- Name: COLUMN t_sys_role.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_role.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_role.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.create_user IS '创建人';


--
-- Name: COLUMN t_sys_role.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.update_user IS '修改人';


--
-- Name: COLUMN t_sys_role.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role.version IS '乐观锁版本号';


--
-- Name: t_sys_role_perms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_role_perms (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: TABLE t_sys_role_perms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_role_perms IS '角色拥有的权限';


--
-- Name: COLUMN t_sys_role_perms.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.id IS 'ID';


--
-- Name: COLUMN t_sys_role_perms.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.role_id IS '角色ID';


--
-- Name: COLUMN t_sys_role_perms.permission_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.permission_id IS '权限ID';


--
-- Name: COLUMN t_sys_role_perms.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_role_perms.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_role_perms.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.create_user IS '创建人';


--
-- Name: COLUMN t_sys_role_perms.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_role_perms.update_user IS '修改人';


--
-- Name: t_sys_script; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script (
    id bigint NOT NULL,
    number character varying(100) NOT NULL,
    name character varying(200) NOT NULL,
    content text NOT NULL,
    remark character varying(500),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE t_sys_script; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_script IS '脚本管理';


--
-- Name: COLUMN t_sys_script.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.id IS 'ID';


--
-- Name: COLUMN t_sys_script.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.number IS '编码';


--
-- Name: COLUMN t_sys_script.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.name IS '名称';


--
-- Name: COLUMN t_sys_script.content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.content IS '脚本内容';


--
-- Name: COLUMN t_sys_script.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.remark IS '备注';


--
-- Name: COLUMN t_sys_script.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_script.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_script.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.create_user IS '创建人';


--
-- Name: COLUMN t_sys_script.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.update_user IS '修改人';


--
-- Name: COLUMN t_sys_script.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script.version IS '乐观锁版本号';


--
-- Name: t_sys_script_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_script_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_script_log IS '脚本控制台执行审计（在线分区父表）';


--
-- Name: COLUMN t_sys_script_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.id IS 'ID';


--
-- Name: COLUMN t_sys_script_log.script_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.script_id IS '关联脚本ID';


--
-- Name: COLUMN t_sys_script_log.script_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.script_name IS '脚本名称快照';


--
-- Name: COLUMN t_sys_script_log.script_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.script_content IS '实际执行脚本快照';


--
-- Name: COLUMN t_sys_script_log.transaction_mode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.transaction_mode IS '事务模式';


--
-- Name: COLUMN t_sys_script_log.execute_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.execute_status IS '执行状态';


--
-- Name: COLUMN t_sys_script_log.execute_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.execute_duration IS '执行耗时（毫秒）';


--
-- Name: COLUMN t_sys_script_log.transaction_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.transaction_result IS '事务结果';


--
-- Name: COLUMN t_sys_script_log.output; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.output IS '截断后的执行输出';


--
-- Name: COLUMN t_sys_script_log.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_script_log.create_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.create_name IS '执行人';


--
-- Name: COLUMN t_sys_script_log.create_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.create_ip IS '执行IP';


--
-- Name: COLUMN t_sys_script_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log.create_time IS '执行时间';


--
-- Name: t_sys_script_log_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_default (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_history (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_script_log_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_script_log_history IS '脚本控制台执行审计（历史分区父表）';


--
-- Name: COLUMN t_sys_script_log_history.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.id IS 'ID';


--
-- Name: COLUMN t_sys_script_log_history.script_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.script_id IS '关联脚本ID';


--
-- Name: COLUMN t_sys_script_log_history.script_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.script_name IS '脚本名称快照';


--
-- Name: COLUMN t_sys_script_log_history.script_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.script_content IS '实际执行脚本快照';


--
-- Name: COLUMN t_sys_script_log_history.transaction_mode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.transaction_mode IS '事务模式';


--
-- Name: COLUMN t_sys_script_log_history.execute_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.execute_status IS '执行状态';


--
-- Name: COLUMN t_sys_script_log_history.execute_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.execute_duration IS '执行耗时（毫秒）';


--
-- Name: COLUMN t_sys_script_log_history.transaction_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.transaction_result IS '事务结果';


--
-- Name: COLUMN t_sys_script_log_history.output; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.output IS '截断后的执行输出';


--
-- Name: COLUMN t_sys_script_log_history.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_script_log_history.create_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.create_name IS '执行人';


--
-- Name: COLUMN t_sys_script_log_history.create_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.create_ip IS '执行IP';


--
-- Name: COLUMN t_sys_script_log_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_script_log_history.create_time IS '执行时间';


--
-- Name: t_sys_script_log_p202601; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202601 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202602; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202602 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202603; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202603 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202604; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202604 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202605; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202605 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202606; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202606 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202607; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202607 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202608; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202608 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202609; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202609 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202610; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202610 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202611; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202611 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202612; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202612 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202701; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202701 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202702; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202702 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202703; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202703 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202704; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202704 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202705; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202705 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202706; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202706 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202707; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202707 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202708; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202708 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202709; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202709 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202710; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202710 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202711; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202711 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202712; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202712 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202801; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202801 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202802; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202802 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202803; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202803 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202804; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202804 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202805; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202805 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202806; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202806 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202807; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202807 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202808; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202808 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202809; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202809 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202810; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202810 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202811; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202811 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202812; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202812 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202901; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202901 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202902; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202902 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202903; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202903 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202904; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202904 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202905; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202905 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202906; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202906 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202907; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202907 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202908; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202908 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202909; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202909 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202910; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202910 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202911; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202911 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_script_log_p202912; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_script_log_p202912 (
    id bigint NOT NULL,
    script_id bigint,
    script_name character varying(200),
    script_content text NOT NULL,
    transaction_mode character varying(20) NOT NULL,
    execute_status character varying(20) NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    transaction_result character varying(20) NOT NULL,
    output text,
    error_message text,
    create_name character varying(100),
    create_ip character varying(64),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: t_sys_sql_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_sql_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_sql_log IS 'SQL执行日志（在线分区父表）';


--
-- Name: COLUMN t_sys_sql_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.id IS 'ID';


--
-- Name: COLUMN t_sys_sql_log.sql_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.sql_text IS 'SQL语句';


--
-- Name: COLUMN t_sys_sql_log.execute_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.execute_duration IS '执行耗时(ms)';


--
-- Name: COLUMN t_sys_sql_log.result_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.result_type IS '结果类型';


--
-- Name: COLUMN t_sys_sql_log.row_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.row_count IS '影响行数';


--
-- Name: COLUMN t_sys_sql_log.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_sql_log.create_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.create_name IS '操作人';


--
-- Name: COLUMN t_sys_sql_log.create_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.create_ip IS '操作IP';


--
-- Name: COLUMN t_sys_sql_log.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.remark IS '备注';


--
-- Name: COLUMN t_sys_sql_log.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.create_user IS '创建人';


--
-- Name: COLUMN t_sys_sql_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_sql_log.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.update_user IS '修改人';


--
-- Name: COLUMN t_sys_sql_log.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log.update_time IS '更新时间';


--
-- Name: t_sys_sql_log_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_default (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_history (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
)
PARTITION BY RANGE (create_time);


--
-- Name: TABLE t_sys_sql_log_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_sql_log_history IS 'SQL执行日志（历史分区父表）';


--
-- Name: COLUMN t_sys_sql_log_history.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.id IS 'ID';


--
-- Name: COLUMN t_sys_sql_log_history.sql_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.sql_text IS 'SQL语句';


--
-- Name: COLUMN t_sys_sql_log_history.execute_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.execute_duration IS '执行耗时(ms)';


--
-- Name: COLUMN t_sys_sql_log_history.result_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.result_type IS '结果类型';


--
-- Name: COLUMN t_sys_sql_log_history.row_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.row_count IS '影响行数';


--
-- Name: COLUMN t_sys_sql_log_history.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.error_message IS '错误信息';


--
-- Name: COLUMN t_sys_sql_log_history.create_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.create_name IS '操作人';


--
-- Name: COLUMN t_sys_sql_log_history.create_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.create_ip IS '操作IP';


--
-- Name: COLUMN t_sys_sql_log_history.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.remark IS '备注';


--
-- Name: COLUMN t_sys_sql_log_history.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.create_user IS '创建人';


--
-- Name: COLUMN t_sys_sql_log_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_sql_log_history.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.update_user IS '修改人';


--
-- Name: COLUMN t_sys_sql_log_history.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_sql_log_history.update_time IS '更新时间';


--
-- Name: t_sys_sql_log_p202601; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202601 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202602; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202602 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202603; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202603 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202604; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202604 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202605; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202605 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202606; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202606 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202607; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202607 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202608; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202608 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202609; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202609 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202610; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202610 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202611; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202611 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202612; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202612 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202701; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202701 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202702; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202702 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202703; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202703 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202704; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202704 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202705; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202705 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202706; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202706 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202707; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202707 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202708; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202708 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202709; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202709 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202710; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202710 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202711; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202711 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202712; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202712 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202801; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202801 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202802; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202802 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202803; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202803 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202804; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202804 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202805; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202805 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202806; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202806 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202807; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202807 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202808; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202808 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202809; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202809 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202810; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202810 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202811; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202811 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202812; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202812 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202901; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202901 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202902; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202902 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202903; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202903 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202904; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202904 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202905; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202905 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202906; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202906 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202907; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202907 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202908; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202908 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202909; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202909 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202910; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202910 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202911; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202911 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_sql_log_p202912; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_sql_log_p202912 (
    id bigint NOT NULL,
    sql_text text NOT NULL,
    execute_duration integer DEFAULT 0 NOT NULL,
    result_type character varying(50) NOT NULL,
    row_count integer DEFAULT 0 NOT NULL,
    error_message text,
    create_name character varying(200),
    create_ip character varying(100),
    remark character varying(500),
    create_user bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_user bigint,
    update_time timestamp without time zone
);


--
-- Name: t_sys_ui_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_ui_config (
    id bigint NOT NULL,
    page_title character varying(200),
    login_banner character varying(500),
    login_logo character varying(500),
    system_name character varying(200),
    header_logo character varying(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    login_banner_attachment_id bigint,
    login_logo_attachment_id bigint,
    header_logo_attachment_id bigint
);


--
-- Name: TABLE t_sys_ui_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_ui_config IS '界面配置';


--
-- Name: COLUMN t_sys_ui_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.id IS 'ID';


--
-- Name: COLUMN t_sys_ui_config.page_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.page_title IS '页面标题';


--
-- Name: COLUMN t_sys_ui_config.login_banner; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.login_banner IS '登录页Banner';


--
-- Name: COLUMN t_sys_ui_config.login_logo; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.login_logo IS '登录页Logo';


--
-- Name: COLUMN t_sys_ui_config.system_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.system_name IS '系统名称';


--
-- Name: COLUMN t_sys_ui_config.header_logo; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.header_logo IS '顶部Logo';


--
-- Name: COLUMN t_sys_ui_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_ui_config.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_ui_config.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.create_user IS '创建人';


--
-- Name: COLUMN t_sys_ui_config.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.update_user IS '修改人';


--
-- Name: COLUMN t_sys_ui_config.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_ui_config.login_banner_attachment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.login_banner_attachment_id IS '登录页Banner附件ID';


--
-- Name: COLUMN t_sys_ui_config.login_logo_attachment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.login_logo_attachment_id IS '登录页Logo附件ID';


--
-- Name: COLUMN t_sys_ui_config.header_logo_attachment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_ui_config.header_logo_attachment_id IS '顶部Logo附件ID';


--
-- Name: t_sys_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_user (
    id bigint NOT NULL,
    username character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    nickname character varying(255),
    avatar character varying(255),
    email character varying(255),
    phone character varying(255),
    theme_color character varying(255),
    enabled boolean DEFAULT true NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    create_user bigint,
    update_time timestamp without time zone DEFAULT now(),
    update_user bigint,
    version integer DEFAULT 0 NOT NULL,
    password_reset boolean DEFAULT true NOT NULL
);


--
-- Name: TABLE t_sys_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_user IS '用户';


--
-- Name: COLUMN t_sys_user.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.id IS 'ID';


--
-- Name: COLUMN t_sys_user.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.username IS '用户名';


--
-- Name: COLUMN t_sys_user.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.password IS '密码';


--
-- Name: COLUMN t_sys_user.nickname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.nickname IS '昵称';


--
-- Name: COLUMN t_sys_user.avatar; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.avatar IS '头像';


--
-- Name: COLUMN t_sys_user.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.email IS '邮箱';


--
-- Name: COLUMN t_sys_user.phone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.phone IS '手机号';


--
-- Name: COLUMN t_sys_user.theme_color; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.theme_color IS '主题色';


--
-- Name: COLUMN t_sys_user.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.enabled IS '启用状态';


--
-- Name: COLUMN t_sys_user.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_user.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.create_user IS '创建人';


--
-- Name: COLUMN t_sys_user.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_user.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.update_user IS '修改人';


--
-- Name: COLUMN t_sys_user.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.version IS '乐观锁版本号';


--
-- Name: COLUMN t_sys_user.password_reset; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user.password_reset IS '是否必须修改密码';


--
-- Name: t_sys_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_sys_user_role (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    org_id bigint NOT NULL,
    role_id bigint NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint
);


--
-- Name: TABLE t_sys_user_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_sys_user_role IS '用户在组织下的角色';


--
-- Name: COLUMN t_sys_user_role.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.id IS 'ID';


--
-- Name: COLUMN t_sys_user_role.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.user_id IS '用户ID';


--
-- Name: COLUMN t_sys_user_role.org_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.org_id IS '组织ID';


--
-- Name: COLUMN t_sys_user_role.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.role_id IS '角色ID';


--
-- Name: COLUMN t_sys_user_role.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.create_time IS '创建时间';


--
-- Name: COLUMN t_sys_user_role.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.update_time IS '更新时间';


--
-- Name: COLUMN t_sys_user_role.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.create_user IS '创建人';


--
-- Name: COLUMN t_sys_user_role.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_sys_user_role.update_user IS '修改人';


--
-- Name: t_sys_job_log_default; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_default DEFAULT;


--
-- Name: t_sys_job_log_p202601; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202601 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');


--
-- Name: t_sys_job_log_p202602; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202602 FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');


--
-- Name: t_sys_job_log_p202603; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202603 FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');


--
-- Name: t_sys_job_log_p202604; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202604 FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');


--
-- Name: t_sys_job_log_p202605; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202605 FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');


--
-- Name: t_sys_job_log_p202606; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202606 FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');


--
-- Name: t_sys_job_log_p202607; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202607 FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');


--
-- Name: t_sys_job_log_p202608; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202608 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');


--
-- Name: t_sys_job_log_p202609; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202609 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');


--
-- Name: t_sys_job_log_p202610; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202610 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');


--
-- Name: t_sys_job_log_p202611; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202611 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');


--
-- Name: t_sys_job_log_p202612; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202612 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');


--
-- Name: t_sys_job_log_p202701; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202701 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');


--
-- Name: t_sys_job_log_p202702; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202702 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--
-- Name: t_sys_job_log_p202703; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202703 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');


--
-- Name: t_sys_job_log_p202704; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202704 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--
-- Name: t_sys_job_log_p202705; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202705 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--
-- Name: t_sys_job_log_p202706; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202706 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--
-- Name: t_sys_job_log_p202707; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202707 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--
-- Name: t_sys_job_log_p202708; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202708 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');


--
-- Name: t_sys_job_log_p202709; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202709 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--
-- Name: t_sys_job_log_p202710; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202710 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');


--
-- Name: t_sys_job_log_p202711; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202711 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');


--
-- Name: t_sys_job_log_p202712; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202712 FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');


--
-- Name: t_sys_job_log_p202801; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202801 FOR VALUES FROM ('2028-01-01 00:00:00') TO ('2028-02-01 00:00:00');


--
-- Name: t_sys_job_log_p202802; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202802 FOR VALUES FROM ('2028-02-01 00:00:00') TO ('2028-03-01 00:00:00');


--
-- Name: t_sys_job_log_p202803; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202803 FOR VALUES FROM ('2028-03-01 00:00:00') TO ('2028-04-01 00:00:00');


--
-- Name: t_sys_job_log_p202804; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202804 FOR VALUES FROM ('2028-04-01 00:00:00') TO ('2028-05-01 00:00:00');


--
-- Name: t_sys_job_log_p202805; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202805 FOR VALUES FROM ('2028-05-01 00:00:00') TO ('2028-06-01 00:00:00');


--
-- Name: t_sys_job_log_p202806; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202806 FOR VALUES FROM ('2028-06-01 00:00:00') TO ('2028-07-01 00:00:00');


--
-- Name: t_sys_job_log_p202807; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202807 FOR VALUES FROM ('2028-07-01 00:00:00') TO ('2028-08-01 00:00:00');


--
-- Name: t_sys_job_log_p202808; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202808 FOR VALUES FROM ('2028-08-01 00:00:00') TO ('2028-09-01 00:00:00');


--
-- Name: t_sys_job_log_p202809; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202809 FOR VALUES FROM ('2028-09-01 00:00:00') TO ('2028-10-01 00:00:00');


--
-- Name: t_sys_job_log_p202810; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202810 FOR VALUES FROM ('2028-10-01 00:00:00') TO ('2028-11-01 00:00:00');


--
-- Name: t_sys_job_log_p202811; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202811 FOR VALUES FROM ('2028-11-01 00:00:00') TO ('2028-12-01 00:00:00');


--
-- Name: t_sys_job_log_p202812; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202812 FOR VALUES FROM ('2028-12-01 00:00:00') TO ('2029-01-01 00:00:00');


--
-- Name: t_sys_job_log_p202901; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202901 FOR VALUES FROM ('2029-01-01 00:00:00') TO ('2029-02-01 00:00:00');


--
-- Name: t_sys_job_log_p202902; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202902 FOR VALUES FROM ('2029-02-01 00:00:00') TO ('2029-03-01 00:00:00');


--
-- Name: t_sys_job_log_p202903; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202903 FOR VALUES FROM ('2029-03-01 00:00:00') TO ('2029-04-01 00:00:00');


--
-- Name: t_sys_job_log_p202904; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202904 FOR VALUES FROM ('2029-04-01 00:00:00') TO ('2029-05-01 00:00:00');


--
-- Name: t_sys_job_log_p202905; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202905 FOR VALUES FROM ('2029-05-01 00:00:00') TO ('2029-06-01 00:00:00');


--
-- Name: t_sys_job_log_p202906; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202906 FOR VALUES FROM ('2029-06-01 00:00:00') TO ('2029-07-01 00:00:00');


--
-- Name: t_sys_job_log_p202907; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202907 FOR VALUES FROM ('2029-07-01 00:00:00') TO ('2029-08-01 00:00:00');


--
-- Name: t_sys_job_log_p202908; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202908 FOR VALUES FROM ('2029-08-01 00:00:00') TO ('2029-09-01 00:00:00');


--
-- Name: t_sys_job_log_p202909; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202909 FOR VALUES FROM ('2029-09-01 00:00:00') TO ('2029-10-01 00:00:00');


--
-- Name: t_sys_job_log_p202910; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202910 FOR VALUES FROM ('2029-10-01 00:00:00') TO ('2029-11-01 00:00:00');


--
-- Name: t_sys_job_log_p202911; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202911 FOR VALUES FROM ('2029-11-01 00:00:00') TO ('2029-12-01 00:00:00');


--
-- Name: t_sys_job_log_p202912; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202912 FOR VALUES FROM ('2029-12-01 00:00:00') TO ('2030-01-01 00:00:00');


--
-- Name: t_sys_login_log_default; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_default DEFAULT;


--
-- Name: t_sys_login_log_p202601; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202601 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');


--
-- Name: t_sys_login_log_p202602; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202602 FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');


--
-- Name: t_sys_login_log_p202603; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202603 FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');


--
-- Name: t_sys_login_log_p202604; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202604 FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');


--
-- Name: t_sys_login_log_p202605; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202605 FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');


--
-- Name: t_sys_login_log_p202606; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202606 FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');


--
-- Name: t_sys_login_log_p202607; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202607 FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');


--
-- Name: t_sys_login_log_p202608; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202608 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');


--
-- Name: t_sys_login_log_p202609; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202609 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');


--
-- Name: t_sys_login_log_p202610; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202610 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');


--
-- Name: t_sys_login_log_p202611; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202611 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');


--
-- Name: t_sys_login_log_p202612; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202612 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');


--
-- Name: t_sys_login_log_p202701; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202701 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');


--
-- Name: t_sys_login_log_p202702; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202702 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--
-- Name: t_sys_login_log_p202703; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202703 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');


--
-- Name: t_sys_login_log_p202704; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202704 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--
-- Name: t_sys_login_log_p202705; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202705 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--
-- Name: t_sys_login_log_p202706; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202706 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--
-- Name: t_sys_login_log_p202707; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202707 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--
-- Name: t_sys_login_log_p202708; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202708 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');


--
-- Name: t_sys_login_log_p202709; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202709 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--
-- Name: t_sys_login_log_p202710; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202710 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');


--
-- Name: t_sys_login_log_p202711; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202711 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');


--
-- Name: t_sys_login_log_p202712; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202712 FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');


--
-- Name: t_sys_login_log_p202801; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202801 FOR VALUES FROM ('2028-01-01 00:00:00') TO ('2028-02-01 00:00:00');


--
-- Name: t_sys_login_log_p202802; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202802 FOR VALUES FROM ('2028-02-01 00:00:00') TO ('2028-03-01 00:00:00');


--
-- Name: t_sys_login_log_p202803; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202803 FOR VALUES FROM ('2028-03-01 00:00:00') TO ('2028-04-01 00:00:00');


--
-- Name: t_sys_login_log_p202804; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202804 FOR VALUES FROM ('2028-04-01 00:00:00') TO ('2028-05-01 00:00:00');


--
-- Name: t_sys_login_log_p202805; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202805 FOR VALUES FROM ('2028-05-01 00:00:00') TO ('2028-06-01 00:00:00');


--
-- Name: t_sys_login_log_p202806; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202806 FOR VALUES FROM ('2028-06-01 00:00:00') TO ('2028-07-01 00:00:00');


--
-- Name: t_sys_login_log_p202807; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202807 FOR VALUES FROM ('2028-07-01 00:00:00') TO ('2028-08-01 00:00:00');


--
-- Name: t_sys_login_log_p202808; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202808 FOR VALUES FROM ('2028-08-01 00:00:00') TO ('2028-09-01 00:00:00');


--
-- Name: t_sys_login_log_p202809; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202809 FOR VALUES FROM ('2028-09-01 00:00:00') TO ('2028-10-01 00:00:00');


--
-- Name: t_sys_login_log_p202810; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202810 FOR VALUES FROM ('2028-10-01 00:00:00') TO ('2028-11-01 00:00:00');


--
-- Name: t_sys_login_log_p202811; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202811 FOR VALUES FROM ('2028-11-01 00:00:00') TO ('2028-12-01 00:00:00');


--
-- Name: t_sys_login_log_p202812; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202812 FOR VALUES FROM ('2028-12-01 00:00:00') TO ('2029-01-01 00:00:00');


--
-- Name: t_sys_login_log_p202901; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202901 FOR VALUES FROM ('2029-01-01 00:00:00') TO ('2029-02-01 00:00:00');


--
-- Name: t_sys_login_log_p202902; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202902 FOR VALUES FROM ('2029-02-01 00:00:00') TO ('2029-03-01 00:00:00');


--
-- Name: t_sys_login_log_p202903; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202903 FOR VALUES FROM ('2029-03-01 00:00:00') TO ('2029-04-01 00:00:00');


--
-- Name: t_sys_login_log_p202904; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202904 FOR VALUES FROM ('2029-04-01 00:00:00') TO ('2029-05-01 00:00:00');


--
-- Name: t_sys_login_log_p202905; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202905 FOR VALUES FROM ('2029-05-01 00:00:00') TO ('2029-06-01 00:00:00');


--
-- Name: t_sys_login_log_p202906; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202906 FOR VALUES FROM ('2029-06-01 00:00:00') TO ('2029-07-01 00:00:00');


--
-- Name: t_sys_login_log_p202907; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202907 FOR VALUES FROM ('2029-07-01 00:00:00') TO ('2029-08-01 00:00:00');


--
-- Name: t_sys_login_log_p202908; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202908 FOR VALUES FROM ('2029-08-01 00:00:00') TO ('2029-09-01 00:00:00');


--
-- Name: t_sys_login_log_p202909; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202909 FOR VALUES FROM ('2029-09-01 00:00:00') TO ('2029-10-01 00:00:00');


--
-- Name: t_sys_login_log_p202910; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202910 FOR VALUES FROM ('2029-10-01 00:00:00') TO ('2029-11-01 00:00:00');


--
-- Name: t_sys_login_log_p202911; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202911 FOR VALUES FROM ('2029-11-01 00:00:00') TO ('2029-12-01 00:00:00');


--
-- Name: t_sys_login_log_p202912; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202912 FOR VALUES FROM ('2029-12-01 00:00:00') TO ('2030-01-01 00:00:00');


--
-- Name: t_sys_operate_log_default; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_default DEFAULT;


--
-- Name: t_sys_operate_log_p202601; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202601 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');


--
-- Name: t_sys_operate_log_p202602; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202602 FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');


--
-- Name: t_sys_operate_log_p202603; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202603 FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');


--
-- Name: t_sys_operate_log_p202604; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202604 FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');


--
-- Name: t_sys_operate_log_p202605; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202605 FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');


--
-- Name: t_sys_operate_log_p202606; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202606 FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');


--
-- Name: t_sys_operate_log_p202607; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202607 FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');


--
-- Name: t_sys_operate_log_p202608; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202608 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');


--
-- Name: t_sys_operate_log_p202609; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202609 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');


--
-- Name: t_sys_operate_log_p202610; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202610 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');


--
-- Name: t_sys_operate_log_p202611; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202611 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');


--
-- Name: t_sys_operate_log_p202612; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202612 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');


--
-- Name: t_sys_operate_log_p202701; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202701 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');


--
-- Name: t_sys_operate_log_p202702; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202702 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--
-- Name: t_sys_operate_log_p202703; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202703 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');


--
-- Name: t_sys_operate_log_p202704; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202704 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--
-- Name: t_sys_operate_log_p202705; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202705 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--
-- Name: t_sys_operate_log_p202706; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202706 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--
-- Name: t_sys_operate_log_p202707; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202707 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--
-- Name: t_sys_operate_log_p202708; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202708 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');


--
-- Name: t_sys_operate_log_p202709; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202709 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--
-- Name: t_sys_operate_log_p202710; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202710 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');


--
-- Name: t_sys_operate_log_p202711; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202711 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');


--
-- Name: t_sys_operate_log_p202712; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202712 FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');


--
-- Name: t_sys_operate_log_p202801; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202801 FOR VALUES FROM ('2028-01-01 00:00:00') TO ('2028-02-01 00:00:00');


--
-- Name: t_sys_operate_log_p202802; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202802 FOR VALUES FROM ('2028-02-01 00:00:00') TO ('2028-03-01 00:00:00');


--
-- Name: t_sys_operate_log_p202803; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202803 FOR VALUES FROM ('2028-03-01 00:00:00') TO ('2028-04-01 00:00:00');


--
-- Name: t_sys_operate_log_p202804; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202804 FOR VALUES FROM ('2028-04-01 00:00:00') TO ('2028-05-01 00:00:00');


--
-- Name: t_sys_operate_log_p202805; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202805 FOR VALUES FROM ('2028-05-01 00:00:00') TO ('2028-06-01 00:00:00');


--
-- Name: t_sys_operate_log_p202806; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202806 FOR VALUES FROM ('2028-06-01 00:00:00') TO ('2028-07-01 00:00:00');


--
-- Name: t_sys_operate_log_p202807; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202807 FOR VALUES FROM ('2028-07-01 00:00:00') TO ('2028-08-01 00:00:00');


--
-- Name: t_sys_operate_log_p202808; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202808 FOR VALUES FROM ('2028-08-01 00:00:00') TO ('2028-09-01 00:00:00');


--
-- Name: t_sys_operate_log_p202809; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202809 FOR VALUES FROM ('2028-09-01 00:00:00') TO ('2028-10-01 00:00:00');


--
-- Name: t_sys_operate_log_p202810; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202810 FOR VALUES FROM ('2028-10-01 00:00:00') TO ('2028-11-01 00:00:00');


--
-- Name: t_sys_operate_log_p202811; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202811 FOR VALUES FROM ('2028-11-01 00:00:00') TO ('2028-12-01 00:00:00');


--
-- Name: t_sys_operate_log_p202812; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202812 FOR VALUES FROM ('2028-12-01 00:00:00') TO ('2029-01-01 00:00:00');


--
-- Name: t_sys_operate_log_p202901; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202901 FOR VALUES FROM ('2029-01-01 00:00:00') TO ('2029-02-01 00:00:00');


--
-- Name: t_sys_operate_log_p202902; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202902 FOR VALUES FROM ('2029-02-01 00:00:00') TO ('2029-03-01 00:00:00');


--
-- Name: t_sys_operate_log_p202903; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202903 FOR VALUES FROM ('2029-03-01 00:00:00') TO ('2029-04-01 00:00:00');


--
-- Name: t_sys_operate_log_p202904; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202904 FOR VALUES FROM ('2029-04-01 00:00:00') TO ('2029-05-01 00:00:00');


--
-- Name: t_sys_operate_log_p202905; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202905 FOR VALUES FROM ('2029-05-01 00:00:00') TO ('2029-06-01 00:00:00');


--
-- Name: t_sys_operate_log_p202906; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202906 FOR VALUES FROM ('2029-06-01 00:00:00') TO ('2029-07-01 00:00:00');


--
-- Name: t_sys_operate_log_p202907; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202907 FOR VALUES FROM ('2029-07-01 00:00:00') TO ('2029-08-01 00:00:00');


--
-- Name: t_sys_operate_log_p202908; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202908 FOR VALUES FROM ('2029-08-01 00:00:00') TO ('2029-09-01 00:00:00');


--
-- Name: t_sys_operate_log_p202909; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202909 FOR VALUES FROM ('2029-09-01 00:00:00') TO ('2029-10-01 00:00:00');


--
-- Name: t_sys_operate_log_p202910; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202910 FOR VALUES FROM ('2029-10-01 00:00:00') TO ('2029-11-01 00:00:00');


--
-- Name: t_sys_operate_log_p202911; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202911 FOR VALUES FROM ('2029-11-01 00:00:00') TO ('2029-12-01 00:00:00');


--
-- Name: t_sys_operate_log_p202912; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202912 FOR VALUES FROM ('2029-12-01 00:00:00') TO ('2030-01-01 00:00:00');


--
-- Name: t_sys_script_log_default; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_default DEFAULT;


--
-- Name: t_sys_script_log_p202601; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202601 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');


--
-- Name: t_sys_script_log_p202602; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202602 FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');


--
-- Name: t_sys_script_log_p202603; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202603 FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');


--
-- Name: t_sys_script_log_p202604; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202604 FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');


--
-- Name: t_sys_script_log_p202605; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202605 FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');


--
-- Name: t_sys_script_log_p202606; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202606 FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');


--
-- Name: t_sys_script_log_p202607; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202607 FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');


--
-- Name: t_sys_script_log_p202608; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202608 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');


--
-- Name: t_sys_script_log_p202609; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202609 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');


--
-- Name: t_sys_script_log_p202610; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202610 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');


--
-- Name: t_sys_script_log_p202611; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202611 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');


--
-- Name: t_sys_script_log_p202612; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202612 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');


--
-- Name: t_sys_script_log_p202701; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202701 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');


--
-- Name: t_sys_script_log_p202702; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202702 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--
-- Name: t_sys_script_log_p202703; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202703 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');


--
-- Name: t_sys_script_log_p202704; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202704 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--
-- Name: t_sys_script_log_p202705; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202705 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--
-- Name: t_sys_script_log_p202706; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202706 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--
-- Name: t_sys_script_log_p202707; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202707 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--
-- Name: t_sys_script_log_p202708; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202708 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');


--
-- Name: t_sys_script_log_p202709; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202709 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--
-- Name: t_sys_script_log_p202710; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202710 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');


--
-- Name: t_sys_script_log_p202711; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202711 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');


--
-- Name: t_sys_script_log_p202712; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202712 FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');


--
-- Name: t_sys_script_log_p202801; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202801 FOR VALUES FROM ('2028-01-01 00:00:00') TO ('2028-02-01 00:00:00');


--
-- Name: t_sys_script_log_p202802; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202802 FOR VALUES FROM ('2028-02-01 00:00:00') TO ('2028-03-01 00:00:00');


--
-- Name: t_sys_script_log_p202803; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202803 FOR VALUES FROM ('2028-03-01 00:00:00') TO ('2028-04-01 00:00:00');


--
-- Name: t_sys_script_log_p202804; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202804 FOR VALUES FROM ('2028-04-01 00:00:00') TO ('2028-05-01 00:00:00');


--
-- Name: t_sys_script_log_p202805; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202805 FOR VALUES FROM ('2028-05-01 00:00:00') TO ('2028-06-01 00:00:00');


--
-- Name: t_sys_script_log_p202806; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202806 FOR VALUES FROM ('2028-06-01 00:00:00') TO ('2028-07-01 00:00:00');


--
-- Name: t_sys_script_log_p202807; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202807 FOR VALUES FROM ('2028-07-01 00:00:00') TO ('2028-08-01 00:00:00');


--
-- Name: t_sys_script_log_p202808; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202808 FOR VALUES FROM ('2028-08-01 00:00:00') TO ('2028-09-01 00:00:00');


--
-- Name: t_sys_script_log_p202809; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202809 FOR VALUES FROM ('2028-09-01 00:00:00') TO ('2028-10-01 00:00:00');


--
-- Name: t_sys_script_log_p202810; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202810 FOR VALUES FROM ('2028-10-01 00:00:00') TO ('2028-11-01 00:00:00');


--
-- Name: t_sys_script_log_p202811; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202811 FOR VALUES FROM ('2028-11-01 00:00:00') TO ('2028-12-01 00:00:00');


--
-- Name: t_sys_script_log_p202812; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202812 FOR VALUES FROM ('2028-12-01 00:00:00') TO ('2029-01-01 00:00:00');


--
-- Name: t_sys_script_log_p202901; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202901 FOR VALUES FROM ('2029-01-01 00:00:00') TO ('2029-02-01 00:00:00');


--
-- Name: t_sys_script_log_p202902; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202902 FOR VALUES FROM ('2029-02-01 00:00:00') TO ('2029-03-01 00:00:00');


--
-- Name: t_sys_script_log_p202903; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202903 FOR VALUES FROM ('2029-03-01 00:00:00') TO ('2029-04-01 00:00:00');


--
-- Name: t_sys_script_log_p202904; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202904 FOR VALUES FROM ('2029-04-01 00:00:00') TO ('2029-05-01 00:00:00');


--
-- Name: t_sys_script_log_p202905; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202905 FOR VALUES FROM ('2029-05-01 00:00:00') TO ('2029-06-01 00:00:00');


--
-- Name: t_sys_script_log_p202906; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202906 FOR VALUES FROM ('2029-06-01 00:00:00') TO ('2029-07-01 00:00:00');


--
-- Name: t_sys_script_log_p202907; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202907 FOR VALUES FROM ('2029-07-01 00:00:00') TO ('2029-08-01 00:00:00');


--
-- Name: t_sys_script_log_p202908; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202908 FOR VALUES FROM ('2029-08-01 00:00:00') TO ('2029-09-01 00:00:00');


--
-- Name: t_sys_script_log_p202909; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202909 FOR VALUES FROM ('2029-09-01 00:00:00') TO ('2029-10-01 00:00:00');


--
-- Name: t_sys_script_log_p202910; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202910 FOR VALUES FROM ('2029-10-01 00:00:00') TO ('2029-11-01 00:00:00');


--
-- Name: t_sys_script_log_p202911; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202911 FOR VALUES FROM ('2029-11-01 00:00:00') TO ('2029-12-01 00:00:00');


--
-- Name: t_sys_script_log_p202912; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202912 FOR VALUES FROM ('2029-12-01 00:00:00') TO ('2030-01-01 00:00:00');


--
-- Name: t_sys_sql_log_default; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_default DEFAULT;


--
-- Name: t_sys_sql_log_p202601; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202601 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');


--
-- Name: t_sys_sql_log_p202602; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202602 FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');


--
-- Name: t_sys_sql_log_p202603; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202603 FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');


--
-- Name: t_sys_sql_log_p202604; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202604 FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');


--
-- Name: t_sys_sql_log_p202605; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202605 FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');


--
-- Name: t_sys_sql_log_p202606; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202606 FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');


--
-- Name: t_sys_sql_log_p202607; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202607 FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');


--
-- Name: t_sys_sql_log_p202608; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202608 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');


--
-- Name: t_sys_sql_log_p202609; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202609 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');


--
-- Name: t_sys_sql_log_p202610; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202610 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');


--
-- Name: t_sys_sql_log_p202611; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202611 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');


--
-- Name: t_sys_sql_log_p202612; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202612 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');


--
-- Name: t_sys_sql_log_p202701; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202701 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');


--
-- Name: t_sys_sql_log_p202702; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202702 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--
-- Name: t_sys_sql_log_p202703; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202703 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');


--
-- Name: t_sys_sql_log_p202704; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202704 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--
-- Name: t_sys_sql_log_p202705; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202705 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--
-- Name: t_sys_sql_log_p202706; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202706 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--
-- Name: t_sys_sql_log_p202707; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202707 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--
-- Name: t_sys_sql_log_p202708; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202708 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');


--
-- Name: t_sys_sql_log_p202709; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202709 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--
-- Name: t_sys_sql_log_p202710; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202710 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');


--
-- Name: t_sys_sql_log_p202711; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202711 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');


--
-- Name: t_sys_sql_log_p202712; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202712 FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');


--
-- Name: t_sys_sql_log_p202801; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202801 FOR VALUES FROM ('2028-01-01 00:00:00') TO ('2028-02-01 00:00:00');


--
-- Name: t_sys_sql_log_p202802; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202802 FOR VALUES FROM ('2028-02-01 00:00:00') TO ('2028-03-01 00:00:00');


--
-- Name: t_sys_sql_log_p202803; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202803 FOR VALUES FROM ('2028-03-01 00:00:00') TO ('2028-04-01 00:00:00');


--
-- Name: t_sys_sql_log_p202804; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202804 FOR VALUES FROM ('2028-04-01 00:00:00') TO ('2028-05-01 00:00:00');


--
-- Name: t_sys_sql_log_p202805; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202805 FOR VALUES FROM ('2028-05-01 00:00:00') TO ('2028-06-01 00:00:00');


--
-- Name: t_sys_sql_log_p202806; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202806 FOR VALUES FROM ('2028-06-01 00:00:00') TO ('2028-07-01 00:00:00');


--
-- Name: t_sys_sql_log_p202807; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202807 FOR VALUES FROM ('2028-07-01 00:00:00') TO ('2028-08-01 00:00:00');


--
-- Name: t_sys_sql_log_p202808; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202808 FOR VALUES FROM ('2028-08-01 00:00:00') TO ('2028-09-01 00:00:00');


--
-- Name: t_sys_sql_log_p202809; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202809 FOR VALUES FROM ('2028-09-01 00:00:00') TO ('2028-10-01 00:00:00');


--
-- Name: t_sys_sql_log_p202810; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202810 FOR VALUES FROM ('2028-10-01 00:00:00') TO ('2028-11-01 00:00:00');


--
-- Name: t_sys_sql_log_p202811; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202811 FOR VALUES FROM ('2028-11-01 00:00:00') TO ('2028-12-01 00:00:00');


--
-- Name: t_sys_sql_log_p202812; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202812 FOR VALUES FROM ('2028-12-01 00:00:00') TO ('2029-01-01 00:00:00');


--
-- Name: t_sys_sql_log_p202901; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202901 FOR VALUES FROM ('2029-01-01 00:00:00') TO ('2029-02-01 00:00:00');


--
-- Name: t_sys_sql_log_p202902; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202902 FOR VALUES FROM ('2029-02-01 00:00:00') TO ('2029-03-01 00:00:00');


--
-- Name: t_sys_sql_log_p202903; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202903 FOR VALUES FROM ('2029-03-01 00:00:00') TO ('2029-04-01 00:00:00');


--
-- Name: t_sys_sql_log_p202904; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202904 FOR VALUES FROM ('2029-04-01 00:00:00') TO ('2029-05-01 00:00:00');


--
-- Name: t_sys_sql_log_p202905; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202905 FOR VALUES FROM ('2029-05-01 00:00:00') TO ('2029-06-01 00:00:00');


--
-- Name: t_sys_sql_log_p202906; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202906 FOR VALUES FROM ('2029-06-01 00:00:00') TO ('2029-07-01 00:00:00');


--
-- Name: t_sys_sql_log_p202907; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202907 FOR VALUES FROM ('2029-07-01 00:00:00') TO ('2029-08-01 00:00:00');


--
-- Name: t_sys_sql_log_p202908; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202908 FOR VALUES FROM ('2029-08-01 00:00:00') TO ('2029-09-01 00:00:00');


--
-- Name: t_sys_sql_log_p202909; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202909 FOR VALUES FROM ('2029-09-01 00:00:00') TO ('2029-10-01 00:00:00');


--
-- Name: t_sys_sql_log_p202910; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202910 FOR VALUES FROM ('2029-10-01 00:00:00') TO ('2029-11-01 00:00:00');


--
-- Name: t_sys_sql_log_p202911; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202911 FOR VALUES FROM ('2029-11-01 00:00:00') TO ('2029-12-01 00:00:00');


--
-- Name: t_sys_sql_log_p202912; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202912 FOR VALUES FROM ('2029-12-01 00:00:00') TO ('2030-01-01 00:00:00');


--
-- Name: t_scm_purchase_requisition pk_scm_purchase_requisition; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition
    ADD CONSTRAINT pk_scm_purchase_requisition PRIMARY KEY (id);


--
-- Name: t_scm_purchase_requisition_entry pk_scm_purchase_requisition_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition_entry
    ADD CONSTRAINT pk_scm_purchase_requisition_entry PRIMARY KEY (id);


--
-- Name: t_sys_attachment_config pk_sys_attachment_config; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_attachment_config
    ADD CONSTRAINT pk_sys_attachment_config PRIMARY KEY (id);


--
-- Name: t_sys_job_log pk_sys_job_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log
    ADD CONSTRAINT pk_sys_job_log PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_history pk_sys_job_log_history; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_history
    ADD CONSTRAINT pk_sys_job_log_history PRIMARY KEY (start_time, id);


--
-- Name: t_sys_login_log pk_sys_login_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log
    ADD CONSTRAINT pk_sys_login_log PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_history pk_sys_login_log_history; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_history
    ADD CONSTRAINT pk_sys_login_log_history PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log pk_sys_operate_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log
    ADD CONSTRAINT pk_sys_operate_log PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_history pk_sys_operate_log_history; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_history
    ADD CONSTRAINT pk_sys_operate_log_history PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log pk_sys_script_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log
    ADD CONSTRAINT pk_sys_script_log PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_history pk_sys_script_log_history; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_history
    ADD CONSTRAINT pk_sys_script_log_history PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log pk_sys_sql_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log
    ADD CONSTRAINT pk_sys_sql_log PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_history pk_sys_sql_log_history; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_history
    ADD CONSTRAINT pk_sys_sql_log_history PRIMARY KEY (create_time, id);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_calendars qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_fired_triggers qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);


--
-- Name: qrtz_job_details qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);


--
-- Name: qrtz_locks qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);


--
-- Name: qrtz_paused_trigger_grps qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);


--
-- Name: qrtz_scheduler_state qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: t_sys_app t_sys_app_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_app
    ADD CONSTRAINT t_sys_app_pkey PRIMARY KEY (id);


--
-- Name: t_sys_attachment t_sys_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_attachment
    ADD CONSTRAINT t_sys_attachment_pkey PRIMARY KEY (id);


--
-- Name: t_sys_basic_data_item t_sys_basic_data_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_item
    ADD CONSTRAINT t_sys_basic_data_entry_pkey PRIMARY KEY (id);


--
-- Name: t_sys_basic_data_category t_sys_basic_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_category
    ADD CONSTRAINT t_sys_basic_data_pkey PRIMARY KEY (id);


--
-- Name: t_sys_biz_attachment t_sys_biz_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_biz_attachment
    ADD CONSTRAINT t_sys_biz_attachment_pkey PRIMARY KEY (id);


--
-- Name: t_sys_cloud t_sys_cloud_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_cloud
    ADD CONSTRAINT t_sys_cloud_pkey PRIMARY KEY (id);


--
-- Name: t_sys_file_config t_sys_file_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_file_config
    ADD CONSTRAINT t_sys_file_config_pkey PRIMARY KEY (id);


--
-- Name: t_sys_job_log_default t_sys_job_log_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_default
    ADD CONSTRAINT t_sys_job_log_default_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202601 t_sys_job_log_p202601_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202601
    ADD CONSTRAINT t_sys_job_log_p202601_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202602 t_sys_job_log_p202602_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202602
    ADD CONSTRAINT t_sys_job_log_p202602_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202603 t_sys_job_log_p202603_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202603
    ADD CONSTRAINT t_sys_job_log_p202603_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202604 t_sys_job_log_p202604_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202604
    ADD CONSTRAINT t_sys_job_log_p202604_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202605 t_sys_job_log_p202605_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202605
    ADD CONSTRAINT t_sys_job_log_p202605_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202606 t_sys_job_log_p202606_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202606
    ADD CONSTRAINT t_sys_job_log_p202606_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202607 t_sys_job_log_p202607_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202607
    ADD CONSTRAINT t_sys_job_log_p202607_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202608 t_sys_job_log_p202608_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202608
    ADD CONSTRAINT t_sys_job_log_p202608_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202609 t_sys_job_log_p202609_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202609
    ADD CONSTRAINT t_sys_job_log_p202609_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202610 t_sys_job_log_p202610_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202610
    ADD CONSTRAINT t_sys_job_log_p202610_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202611 t_sys_job_log_p202611_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202611
    ADD CONSTRAINT t_sys_job_log_p202611_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202612 t_sys_job_log_p202612_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202612
    ADD CONSTRAINT t_sys_job_log_p202612_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202701 t_sys_job_log_p202701_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202701
    ADD CONSTRAINT t_sys_job_log_p202701_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202702 t_sys_job_log_p202702_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202702
    ADD CONSTRAINT t_sys_job_log_p202702_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202703 t_sys_job_log_p202703_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202703
    ADD CONSTRAINT t_sys_job_log_p202703_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202704 t_sys_job_log_p202704_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202704
    ADD CONSTRAINT t_sys_job_log_p202704_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202705 t_sys_job_log_p202705_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202705
    ADD CONSTRAINT t_sys_job_log_p202705_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202706 t_sys_job_log_p202706_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202706
    ADD CONSTRAINT t_sys_job_log_p202706_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202707 t_sys_job_log_p202707_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202707
    ADD CONSTRAINT t_sys_job_log_p202707_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202708 t_sys_job_log_p202708_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202708
    ADD CONSTRAINT t_sys_job_log_p202708_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202709 t_sys_job_log_p202709_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202709
    ADD CONSTRAINT t_sys_job_log_p202709_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202710 t_sys_job_log_p202710_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202710
    ADD CONSTRAINT t_sys_job_log_p202710_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202711 t_sys_job_log_p202711_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202711
    ADD CONSTRAINT t_sys_job_log_p202711_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202712 t_sys_job_log_p202712_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202712
    ADD CONSTRAINT t_sys_job_log_p202712_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202801 t_sys_job_log_p202801_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202801
    ADD CONSTRAINT t_sys_job_log_p202801_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202802 t_sys_job_log_p202802_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202802
    ADD CONSTRAINT t_sys_job_log_p202802_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202803 t_sys_job_log_p202803_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202803
    ADD CONSTRAINT t_sys_job_log_p202803_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202804 t_sys_job_log_p202804_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202804
    ADD CONSTRAINT t_sys_job_log_p202804_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202805 t_sys_job_log_p202805_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202805
    ADD CONSTRAINT t_sys_job_log_p202805_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202806 t_sys_job_log_p202806_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202806
    ADD CONSTRAINT t_sys_job_log_p202806_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202807 t_sys_job_log_p202807_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202807
    ADD CONSTRAINT t_sys_job_log_p202807_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202808 t_sys_job_log_p202808_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202808
    ADD CONSTRAINT t_sys_job_log_p202808_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202809 t_sys_job_log_p202809_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202809
    ADD CONSTRAINT t_sys_job_log_p202809_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202810 t_sys_job_log_p202810_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202810
    ADD CONSTRAINT t_sys_job_log_p202810_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202811 t_sys_job_log_p202811_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202811
    ADD CONSTRAINT t_sys_job_log_p202811_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202812 t_sys_job_log_p202812_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202812
    ADD CONSTRAINT t_sys_job_log_p202812_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202901 t_sys_job_log_p202901_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202901
    ADD CONSTRAINT t_sys_job_log_p202901_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202902 t_sys_job_log_p202902_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202902
    ADD CONSTRAINT t_sys_job_log_p202902_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202903 t_sys_job_log_p202903_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202903
    ADD CONSTRAINT t_sys_job_log_p202903_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202904 t_sys_job_log_p202904_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202904
    ADD CONSTRAINT t_sys_job_log_p202904_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202905 t_sys_job_log_p202905_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202905
    ADD CONSTRAINT t_sys_job_log_p202905_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202906 t_sys_job_log_p202906_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202906
    ADD CONSTRAINT t_sys_job_log_p202906_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202907 t_sys_job_log_p202907_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202907
    ADD CONSTRAINT t_sys_job_log_p202907_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202908 t_sys_job_log_p202908_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202908
    ADD CONSTRAINT t_sys_job_log_p202908_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202909 t_sys_job_log_p202909_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202909
    ADD CONSTRAINT t_sys_job_log_p202909_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202910 t_sys_job_log_p202910_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202910
    ADD CONSTRAINT t_sys_job_log_p202910_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202911 t_sys_job_log_p202911_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202911
    ADD CONSTRAINT t_sys_job_log_p202911_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job_log_p202912 t_sys_job_log_p202912_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job_log_p202912
    ADD CONSTRAINT t_sys_job_log_p202912_pkey PRIMARY KEY (start_time, id);


--
-- Name: t_sys_job t_sys_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_job
    ADD CONSTRAINT t_sys_job_pkey PRIMARY KEY (id);


--
-- Name: t_sys_login_log_default t_sys_login_log_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_default
    ADD CONSTRAINT t_sys_login_log_default_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202601 t_sys_login_log_p202601_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202601
    ADD CONSTRAINT t_sys_login_log_p202601_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202602 t_sys_login_log_p202602_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202602
    ADD CONSTRAINT t_sys_login_log_p202602_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202603 t_sys_login_log_p202603_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202603
    ADD CONSTRAINT t_sys_login_log_p202603_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202604 t_sys_login_log_p202604_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202604
    ADD CONSTRAINT t_sys_login_log_p202604_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202605 t_sys_login_log_p202605_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202605
    ADD CONSTRAINT t_sys_login_log_p202605_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202606 t_sys_login_log_p202606_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202606
    ADD CONSTRAINT t_sys_login_log_p202606_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202607 t_sys_login_log_p202607_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202607
    ADD CONSTRAINT t_sys_login_log_p202607_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202608 t_sys_login_log_p202608_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202608
    ADD CONSTRAINT t_sys_login_log_p202608_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202609 t_sys_login_log_p202609_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202609
    ADD CONSTRAINT t_sys_login_log_p202609_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202610 t_sys_login_log_p202610_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202610
    ADD CONSTRAINT t_sys_login_log_p202610_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202611 t_sys_login_log_p202611_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202611
    ADD CONSTRAINT t_sys_login_log_p202611_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202612 t_sys_login_log_p202612_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202612
    ADD CONSTRAINT t_sys_login_log_p202612_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202701 t_sys_login_log_p202701_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202701
    ADD CONSTRAINT t_sys_login_log_p202701_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202702 t_sys_login_log_p202702_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202702
    ADD CONSTRAINT t_sys_login_log_p202702_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202703 t_sys_login_log_p202703_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202703
    ADD CONSTRAINT t_sys_login_log_p202703_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202704 t_sys_login_log_p202704_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202704
    ADD CONSTRAINT t_sys_login_log_p202704_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202705 t_sys_login_log_p202705_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202705
    ADD CONSTRAINT t_sys_login_log_p202705_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202706 t_sys_login_log_p202706_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202706
    ADD CONSTRAINT t_sys_login_log_p202706_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202707 t_sys_login_log_p202707_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202707
    ADD CONSTRAINT t_sys_login_log_p202707_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202708 t_sys_login_log_p202708_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202708
    ADD CONSTRAINT t_sys_login_log_p202708_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202709 t_sys_login_log_p202709_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202709
    ADD CONSTRAINT t_sys_login_log_p202709_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202710 t_sys_login_log_p202710_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202710
    ADD CONSTRAINT t_sys_login_log_p202710_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202711 t_sys_login_log_p202711_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202711
    ADD CONSTRAINT t_sys_login_log_p202711_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202712 t_sys_login_log_p202712_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202712
    ADD CONSTRAINT t_sys_login_log_p202712_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202801 t_sys_login_log_p202801_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202801
    ADD CONSTRAINT t_sys_login_log_p202801_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202802 t_sys_login_log_p202802_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202802
    ADD CONSTRAINT t_sys_login_log_p202802_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202803 t_sys_login_log_p202803_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202803
    ADD CONSTRAINT t_sys_login_log_p202803_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202804 t_sys_login_log_p202804_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202804
    ADD CONSTRAINT t_sys_login_log_p202804_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202805 t_sys_login_log_p202805_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202805
    ADD CONSTRAINT t_sys_login_log_p202805_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202806 t_sys_login_log_p202806_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202806
    ADD CONSTRAINT t_sys_login_log_p202806_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202807 t_sys_login_log_p202807_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202807
    ADD CONSTRAINT t_sys_login_log_p202807_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202808 t_sys_login_log_p202808_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202808
    ADD CONSTRAINT t_sys_login_log_p202808_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202809 t_sys_login_log_p202809_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202809
    ADD CONSTRAINT t_sys_login_log_p202809_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202810 t_sys_login_log_p202810_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202810
    ADD CONSTRAINT t_sys_login_log_p202810_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202811 t_sys_login_log_p202811_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202811
    ADD CONSTRAINT t_sys_login_log_p202811_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202812 t_sys_login_log_p202812_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202812
    ADD CONSTRAINT t_sys_login_log_p202812_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202901 t_sys_login_log_p202901_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202901
    ADD CONSTRAINT t_sys_login_log_p202901_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202902 t_sys_login_log_p202902_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202902
    ADD CONSTRAINT t_sys_login_log_p202902_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202903 t_sys_login_log_p202903_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202903
    ADD CONSTRAINT t_sys_login_log_p202903_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202904 t_sys_login_log_p202904_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202904
    ADD CONSTRAINT t_sys_login_log_p202904_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202905 t_sys_login_log_p202905_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202905
    ADD CONSTRAINT t_sys_login_log_p202905_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202906 t_sys_login_log_p202906_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202906
    ADD CONSTRAINT t_sys_login_log_p202906_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202907 t_sys_login_log_p202907_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202907
    ADD CONSTRAINT t_sys_login_log_p202907_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202908 t_sys_login_log_p202908_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202908
    ADD CONSTRAINT t_sys_login_log_p202908_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202909 t_sys_login_log_p202909_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202909
    ADD CONSTRAINT t_sys_login_log_p202909_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202910 t_sys_login_log_p202910_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202910
    ADD CONSTRAINT t_sys_login_log_p202910_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202911 t_sys_login_log_p202911_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202911
    ADD CONSTRAINT t_sys_login_log_p202911_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_login_log_p202912 t_sys_login_log_p202912_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_login_log_p202912
    ADD CONSTRAINT t_sys_login_log_p202912_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_menu t_sys_menu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_menu
    ADD CONSTRAINT t_sys_menu_pkey PRIMARY KEY (id);


--
-- Name: t_sys_operate_log_default t_sys_operate_log_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_default
    ADD CONSTRAINT t_sys_operate_log_default_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202601 t_sys_operate_log_p202601_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202601
    ADD CONSTRAINT t_sys_operate_log_p202601_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202602 t_sys_operate_log_p202602_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202602
    ADD CONSTRAINT t_sys_operate_log_p202602_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202603 t_sys_operate_log_p202603_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202603
    ADD CONSTRAINT t_sys_operate_log_p202603_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202604 t_sys_operate_log_p202604_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202604
    ADD CONSTRAINT t_sys_operate_log_p202604_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202605 t_sys_operate_log_p202605_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202605
    ADD CONSTRAINT t_sys_operate_log_p202605_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202606 t_sys_operate_log_p202606_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202606
    ADD CONSTRAINT t_sys_operate_log_p202606_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202607 t_sys_operate_log_p202607_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202607
    ADD CONSTRAINT t_sys_operate_log_p202607_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202608 t_sys_operate_log_p202608_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202608
    ADD CONSTRAINT t_sys_operate_log_p202608_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202609 t_sys_operate_log_p202609_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202609
    ADD CONSTRAINT t_sys_operate_log_p202609_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202610 t_sys_operate_log_p202610_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202610
    ADD CONSTRAINT t_sys_operate_log_p202610_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202611 t_sys_operate_log_p202611_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202611
    ADD CONSTRAINT t_sys_operate_log_p202611_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202612 t_sys_operate_log_p202612_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202612
    ADD CONSTRAINT t_sys_operate_log_p202612_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202701 t_sys_operate_log_p202701_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202701
    ADD CONSTRAINT t_sys_operate_log_p202701_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202702 t_sys_operate_log_p202702_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202702
    ADD CONSTRAINT t_sys_operate_log_p202702_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202703 t_sys_operate_log_p202703_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202703
    ADD CONSTRAINT t_sys_operate_log_p202703_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202704 t_sys_operate_log_p202704_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202704
    ADD CONSTRAINT t_sys_operate_log_p202704_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202705 t_sys_operate_log_p202705_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202705
    ADD CONSTRAINT t_sys_operate_log_p202705_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202706 t_sys_operate_log_p202706_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202706
    ADD CONSTRAINT t_sys_operate_log_p202706_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202707 t_sys_operate_log_p202707_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202707
    ADD CONSTRAINT t_sys_operate_log_p202707_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202708 t_sys_operate_log_p202708_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202708
    ADD CONSTRAINT t_sys_operate_log_p202708_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202709 t_sys_operate_log_p202709_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202709
    ADD CONSTRAINT t_sys_operate_log_p202709_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202710 t_sys_operate_log_p202710_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202710
    ADD CONSTRAINT t_sys_operate_log_p202710_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202711 t_sys_operate_log_p202711_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202711
    ADD CONSTRAINT t_sys_operate_log_p202711_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202712 t_sys_operate_log_p202712_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202712
    ADD CONSTRAINT t_sys_operate_log_p202712_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202801 t_sys_operate_log_p202801_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202801
    ADD CONSTRAINT t_sys_operate_log_p202801_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202802 t_sys_operate_log_p202802_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202802
    ADD CONSTRAINT t_sys_operate_log_p202802_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202803 t_sys_operate_log_p202803_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202803
    ADD CONSTRAINT t_sys_operate_log_p202803_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202804 t_sys_operate_log_p202804_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202804
    ADD CONSTRAINT t_sys_operate_log_p202804_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202805 t_sys_operate_log_p202805_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202805
    ADD CONSTRAINT t_sys_operate_log_p202805_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202806 t_sys_operate_log_p202806_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202806
    ADD CONSTRAINT t_sys_operate_log_p202806_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202807 t_sys_operate_log_p202807_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202807
    ADD CONSTRAINT t_sys_operate_log_p202807_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202808 t_sys_operate_log_p202808_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202808
    ADD CONSTRAINT t_sys_operate_log_p202808_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202809 t_sys_operate_log_p202809_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202809
    ADD CONSTRAINT t_sys_operate_log_p202809_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202810 t_sys_operate_log_p202810_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202810
    ADD CONSTRAINT t_sys_operate_log_p202810_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202811 t_sys_operate_log_p202811_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202811
    ADD CONSTRAINT t_sys_operate_log_p202811_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202812 t_sys_operate_log_p202812_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202812
    ADD CONSTRAINT t_sys_operate_log_p202812_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202901 t_sys_operate_log_p202901_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202901
    ADD CONSTRAINT t_sys_operate_log_p202901_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202902 t_sys_operate_log_p202902_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202902
    ADD CONSTRAINT t_sys_operate_log_p202902_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202903 t_sys_operate_log_p202903_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202903
    ADD CONSTRAINT t_sys_operate_log_p202903_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202904 t_sys_operate_log_p202904_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202904
    ADD CONSTRAINT t_sys_operate_log_p202904_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202905 t_sys_operate_log_p202905_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202905
    ADD CONSTRAINT t_sys_operate_log_p202905_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202906 t_sys_operate_log_p202906_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202906
    ADD CONSTRAINT t_sys_operate_log_p202906_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202907 t_sys_operate_log_p202907_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202907
    ADD CONSTRAINT t_sys_operate_log_p202907_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202908 t_sys_operate_log_p202908_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202908
    ADD CONSTRAINT t_sys_operate_log_p202908_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202909 t_sys_operate_log_p202909_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202909
    ADD CONSTRAINT t_sys_operate_log_p202909_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202910 t_sys_operate_log_p202910_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202910
    ADD CONSTRAINT t_sys_operate_log_p202910_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202911 t_sys_operate_log_p202911_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202911
    ADD CONSTRAINT t_sys_operate_log_p202911_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_operate_log_p202912 t_sys_operate_log_p202912_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_operate_log_p202912
    ADD CONSTRAINT t_sys_operate_log_p202912_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_org t_sys_org_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_org
    ADD CONSTRAINT t_sys_org_pkey PRIMARY KEY (id);


--
-- Name: t_sys_param t_sys_param_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_param
    ADD CONSTRAINT t_sys_param_pkey PRIMARY KEY (id);


--
-- Name: t_sys_permission t_sys_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_permission
    ADD CONSTRAINT t_sys_permission_pkey PRIMARY KEY (id);


--
-- Name: t_sys_role_perms t_sys_role_perms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_role_perms
    ADD CONSTRAINT t_sys_role_perms_pkey PRIMARY KEY (id);


--
-- Name: t_sys_role t_sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_role
    ADD CONSTRAINT t_sys_role_pkey PRIMARY KEY (id);


--
-- Name: t_sys_script_log_default t_sys_script_log_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_default
    ADD CONSTRAINT t_sys_script_log_default_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202601 t_sys_script_log_p202601_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202601
    ADD CONSTRAINT t_sys_script_log_p202601_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202602 t_sys_script_log_p202602_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202602
    ADD CONSTRAINT t_sys_script_log_p202602_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202603 t_sys_script_log_p202603_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202603
    ADD CONSTRAINT t_sys_script_log_p202603_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202604 t_sys_script_log_p202604_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202604
    ADD CONSTRAINT t_sys_script_log_p202604_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202605 t_sys_script_log_p202605_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202605
    ADD CONSTRAINT t_sys_script_log_p202605_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202606 t_sys_script_log_p202606_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202606
    ADD CONSTRAINT t_sys_script_log_p202606_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202607 t_sys_script_log_p202607_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202607
    ADD CONSTRAINT t_sys_script_log_p202607_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202608 t_sys_script_log_p202608_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202608
    ADD CONSTRAINT t_sys_script_log_p202608_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202609 t_sys_script_log_p202609_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202609
    ADD CONSTRAINT t_sys_script_log_p202609_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202610 t_sys_script_log_p202610_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202610
    ADD CONSTRAINT t_sys_script_log_p202610_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202611 t_sys_script_log_p202611_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202611
    ADD CONSTRAINT t_sys_script_log_p202611_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202612 t_sys_script_log_p202612_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202612
    ADD CONSTRAINT t_sys_script_log_p202612_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202701 t_sys_script_log_p202701_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202701
    ADD CONSTRAINT t_sys_script_log_p202701_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202702 t_sys_script_log_p202702_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202702
    ADD CONSTRAINT t_sys_script_log_p202702_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202703 t_sys_script_log_p202703_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202703
    ADD CONSTRAINT t_sys_script_log_p202703_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202704 t_sys_script_log_p202704_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202704
    ADD CONSTRAINT t_sys_script_log_p202704_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202705 t_sys_script_log_p202705_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202705
    ADD CONSTRAINT t_sys_script_log_p202705_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202706 t_sys_script_log_p202706_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202706
    ADD CONSTRAINT t_sys_script_log_p202706_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202707 t_sys_script_log_p202707_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202707
    ADD CONSTRAINT t_sys_script_log_p202707_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202708 t_sys_script_log_p202708_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202708
    ADD CONSTRAINT t_sys_script_log_p202708_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202709 t_sys_script_log_p202709_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202709
    ADD CONSTRAINT t_sys_script_log_p202709_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202710 t_sys_script_log_p202710_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202710
    ADD CONSTRAINT t_sys_script_log_p202710_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202711 t_sys_script_log_p202711_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202711
    ADD CONSTRAINT t_sys_script_log_p202711_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202712 t_sys_script_log_p202712_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202712
    ADD CONSTRAINT t_sys_script_log_p202712_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202801 t_sys_script_log_p202801_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202801
    ADD CONSTRAINT t_sys_script_log_p202801_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202802 t_sys_script_log_p202802_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202802
    ADD CONSTRAINT t_sys_script_log_p202802_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202803 t_sys_script_log_p202803_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202803
    ADD CONSTRAINT t_sys_script_log_p202803_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202804 t_sys_script_log_p202804_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202804
    ADD CONSTRAINT t_sys_script_log_p202804_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202805 t_sys_script_log_p202805_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202805
    ADD CONSTRAINT t_sys_script_log_p202805_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202806 t_sys_script_log_p202806_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202806
    ADD CONSTRAINT t_sys_script_log_p202806_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202807 t_sys_script_log_p202807_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202807
    ADD CONSTRAINT t_sys_script_log_p202807_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202808 t_sys_script_log_p202808_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202808
    ADD CONSTRAINT t_sys_script_log_p202808_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202809 t_sys_script_log_p202809_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202809
    ADD CONSTRAINT t_sys_script_log_p202809_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202810 t_sys_script_log_p202810_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202810
    ADD CONSTRAINT t_sys_script_log_p202810_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202811 t_sys_script_log_p202811_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202811
    ADD CONSTRAINT t_sys_script_log_p202811_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202812 t_sys_script_log_p202812_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202812
    ADD CONSTRAINT t_sys_script_log_p202812_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202901 t_sys_script_log_p202901_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202901
    ADD CONSTRAINT t_sys_script_log_p202901_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202902 t_sys_script_log_p202902_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202902
    ADD CONSTRAINT t_sys_script_log_p202902_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202903 t_sys_script_log_p202903_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202903
    ADD CONSTRAINT t_sys_script_log_p202903_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202904 t_sys_script_log_p202904_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202904
    ADD CONSTRAINT t_sys_script_log_p202904_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202905 t_sys_script_log_p202905_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202905
    ADD CONSTRAINT t_sys_script_log_p202905_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202906 t_sys_script_log_p202906_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202906
    ADD CONSTRAINT t_sys_script_log_p202906_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202907 t_sys_script_log_p202907_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202907
    ADD CONSTRAINT t_sys_script_log_p202907_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202908 t_sys_script_log_p202908_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202908
    ADD CONSTRAINT t_sys_script_log_p202908_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202909 t_sys_script_log_p202909_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202909
    ADD CONSTRAINT t_sys_script_log_p202909_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202910 t_sys_script_log_p202910_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202910
    ADD CONSTRAINT t_sys_script_log_p202910_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202911 t_sys_script_log_p202911_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202911
    ADD CONSTRAINT t_sys_script_log_p202911_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script_log_p202912 t_sys_script_log_p202912_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script_log_p202912
    ADD CONSTRAINT t_sys_script_log_p202912_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_script t_sys_script_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script
    ADD CONSTRAINT t_sys_script_pkey PRIMARY KEY (id);


--
-- Name: t_sys_sql_log_default t_sys_sql_log_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_default
    ADD CONSTRAINT t_sys_sql_log_default_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202601 t_sys_sql_log_p202601_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202601
    ADD CONSTRAINT t_sys_sql_log_p202601_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202602 t_sys_sql_log_p202602_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202602
    ADD CONSTRAINT t_sys_sql_log_p202602_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202603 t_sys_sql_log_p202603_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202603
    ADD CONSTRAINT t_sys_sql_log_p202603_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202604 t_sys_sql_log_p202604_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202604
    ADD CONSTRAINT t_sys_sql_log_p202604_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202605 t_sys_sql_log_p202605_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202605
    ADD CONSTRAINT t_sys_sql_log_p202605_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202606 t_sys_sql_log_p202606_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202606
    ADD CONSTRAINT t_sys_sql_log_p202606_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202607 t_sys_sql_log_p202607_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202607
    ADD CONSTRAINT t_sys_sql_log_p202607_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202608 t_sys_sql_log_p202608_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202608
    ADD CONSTRAINT t_sys_sql_log_p202608_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202609 t_sys_sql_log_p202609_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202609
    ADD CONSTRAINT t_sys_sql_log_p202609_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202610 t_sys_sql_log_p202610_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202610
    ADD CONSTRAINT t_sys_sql_log_p202610_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202611 t_sys_sql_log_p202611_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202611
    ADD CONSTRAINT t_sys_sql_log_p202611_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202612 t_sys_sql_log_p202612_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202612
    ADD CONSTRAINT t_sys_sql_log_p202612_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202701 t_sys_sql_log_p202701_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202701
    ADD CONSTRAINT t_sys_sql_log_p202701_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202702 t_sys_sql_log_p202702_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202702
    ADD CONSTRAINT t_sys_sql_log_p202702_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202703 t_sys_sql_log_p202703_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202703
    ADD CONSTRAINT t_sys_sql_log_p202703_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202704 t_sys_sql_log_p202704_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202704
    ADD CONSTRAINT t_sys_sql_log_p202704_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202705 t_sys_sql_log_p202705_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202705
    ADD CONSTRAINT t_sys_sql_log_p202705_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202706 t_sys_sql_log_p202706_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202706
    ADD CONSTRAINT t_sys_sql_log_p202706_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202707 t_sys_sql_log_p202707_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202707
    ADD CONSTRAINT t_sys_sql_log_p202707_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202708 t_sys_sql_log_p202708_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202708
    ADD CONSTRAINT t_sys_sql_log_p202708_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202709 t_sys_sql_log_p202709_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202709
    ADD CONSTRAINT t_sys_sql_log_p202709_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202710 t_sys_sql_log_p202710_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202710
    ADD CONSTRAINT t_sys_sql_log_p202710_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202711 t_sys_sql_log_p202711_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202711
    ADD CONSTRAINT t_sys_sql_log_p202711_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202712 t_sys_sql_log_p202712_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202712
    ADD CONSTRAINT t_sys_sql_log_p202712_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202801 t_sys_sql_log_p202801_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202801
    ADD CONSTRAINT t_sys_sql_log_p202801_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202802 t_sys_sql_log_p202802_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202802
    ADD CONSTRAINT t_sys_sql_log_p202802_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202803 t_sys_sql_log_p202803_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202803
    ADD CONSTRAINT t_sys_sql_log_p202803_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202804 t_sys_sql_log_p202804_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202804
    ADD CONSTRAINT t_sys_sql_log_p202804_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202805 t_sys_sql_log_p202805_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202805
    ADD CONSTRAINT t_sys_sql_log_p202805_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202806 t_sys_sql_log_p202806_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202806
    ADD CONSTRAINT t_sys_sql_log_p202806_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202807 t_sys_sql_log_p202807_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202807
    ADD CONSTRAINT t_sys_sql_log_p202807_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202808 t_sys_sql_log_p202808_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202808
    ADD CONSTRAINT t_sys_sql_log_p202808_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202809 t_sys_sql_log_p202809_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202809
    ADD CONSTRAINT t_sys_sql_log_p202809_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202810 t_sys_sql_log_p202810_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202810
    ADD CONSTRAINT t_sys_sql_log_p202810_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202811 t_sys_sql_log_p202811_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202811
    ADD CONSTRAINT t_sys_sql_log_p202811_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202812 t_sys_sql_log_p202812_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202812
    ADD CONSTRAINT t_sys_sql_log_p202812_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202901 t_sys_sql_log_p202901_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202901
    ADD CONSTRAINT t_sys_sql_log_p202901_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202902 t_sys_sql_log_p202902_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202902
    ADD CONSTRAINT t_sys_sql_log_p202902_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202903 t_sys_sql_log_p202903_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202903
    ADD CONSTRAINT t_sys_sql_log_p202903_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202904 t_sys_sql_log_p202904_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202904
    ADD CONSTRAINT t_sys_sql_log_p202904_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202905 t_sys_sql_log_p202905_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202905
    ADD CONSTRAINT t_sys_sql_log_p202905_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202906 t_sys_sql_log_p202906_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202906
    ADD CONSTRAINT t_sys_sql_log_p202906_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202907 t_sys_sql_log_p202907_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202907
    ADD CONSTRAINT t_sys_sql_log_p202907_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202908 t_sys_sql_log_p202908_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202908
    ADD CONSTRAINT t_sys_sql_log_p202908_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202909 t_sys_sql_log_p202909_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202909
    ADD CONSTRAINT t_sys_sql_log_p202909_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202910 t_sys_sql_log_p202910_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202910
    ADD CONSTRAINT t_sys_sql_log_p202910_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202911 t_sys_sql_log_p202911_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202911
    ADD CONSTRAINT t_sys_sql_log_p202911_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_sql_log_p202912 t_sys_sql_log_p202912_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_sql_log_p202912
    ADD CONSTRAINT t_sys_sql_log_p202912_pkey PRIMARY KEY (create_time, id);


--
-- Name: t_sys_ui_config t_sys_ui_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_ui_config
    ADD CONSTRAINT t_sys_ui_config_pkey PRIMARY KEY (id);


--
-- Name: t_sys_user t_sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user
    ADD CONSTRAINT t_sys_user_pkey PRIMARY KEY (id);


--
-- Name: t_sys_user_role t_sys_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user_role
    ADD CONSTRAINT t_sys_user_role_pkey PRIMARY KEY (id);


--
-- Name: t_sys_basic_data_category uk_basic_data_category_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_category
    ADD CONSTRAINT uk_basic_data_category_number UNIQUE (number);


--
-- Name: t_scm_purchase_requisition uk_scm_purchase_requisition_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition
    ADD CONSTRAINT uk_scm_purchase_requisition_number UNIQUE (number);


--
-- Name: t_sys_app uk_sys_app_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_app
    ADD CONSTRAINT uk_sys_app_number UNIQUE (number);


--
-- Name: t_sys_cloud uk_sys_cloud_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_cloud
    ADD CONSTRAINT uk_sys_cloud_number UNIQUE (number);


--
-- Name: t_sys_role_perms uk_sys_rp_role_perm; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_role_perms
    ADD CONSTRAINT uk_sys_rp_role_perm UNIQUE (role_id, permission_id);


--
-- Name: t_sys_script uk_sys_script_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_script
    ADD CONSTRAINT uk_sys_script_number UNIQUE (number);


--
-- Name: t_sys_user_role uk_sys_ur_user_org_role; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user_role
    ADD CONSTRAINT uk_sys_ur_user_org_role UNIQUE (user_id, org_id, role_id);


--
-- Name: t_sys_user uk_sys_user_username; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user
    ADD CONSTRAINT uk_sys_user_username UNIQUE (username);


--
-- Name: idx_basic_data_category_cloud_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_basic_data_category_cloud_id ON public.t_sys_basic_data_category USING btree (cloud_id);


--
-- Name: idx_basic_data_item_category_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_basic_data_item_category_id ON public.t_sys_basic_data_item USING btree (category_id);


--
-- Name: idx_basic_data_item_parent_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_basic_data_item_parent_id ON public.t_sys_basic_data_item USING btree (parent_id);


--
-- Name: idx_biz_attachment_att; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_biz_attachment_att ON public.t_sys_biz_attachment USING btree (attachment_id);


--
-- Name: idx_biz_attachment_biz; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_biz_attachment_biz ON public.t_sys_biz_attachment USING btree (biz_type, biz_id);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON public.qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_j_g ON public.qrtz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_jg ON public.qrtz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_t_g ON public.qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_tg ON public.qrtz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON public.qrtz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_j_grp ON public.qrtz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_j_req_recovery ON public.qrtz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_c ON public.qrtz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_g; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_g ON public.qrtz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_j ON public.qrtz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_jg ON public.qrtz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_n_g_state ON public.qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_n_state ON public.qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_next_fire_time ON public.qrtz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_nft_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_nft_st ON public.qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_state; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_t_state ON public.qrtz_triggers USING btree (sched_name, trigger_state);


--
-- Name: idx_scm_purchase_requisition_entry_parent_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_scm_purchase_requisition_entry_parent_id ON public.t_scm_purchase_requisition_entry USING btree (parent_id);


--
-- Name: idx_sys_app_cloud; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_app_cloud ON public.t_sys_app USING btree (cloud_id);


--
-- Name: idx_sys_app_num; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_app_num ON public.t_sys_app USING btree (number);


--
-- Name: idx_sys_attachment_cleanup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_attachment_cleanup ON public.t_sys_attachment USING btree (status, expires_at);


--
-- Name: idx_sys_cloud_num; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_cloud_num ON public.t_sys_cloud USING btree (number);


--
-- Name: idx_sys_job_log_fire_instance_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_fire_instance_id ON ONLY public.t_sys_job_log USING btree (fire_instance_id);


--
-- Name: idx_sys_job_log_history_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_history_id ON ONLY public.t_sys_job_log_history USING btree (id);


--
-- Name: idx_sys_job_log_history_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_history_trace_id ON ONLY public.t_sys_job_log_history USING btree (trace_id);


--
-- Name: idx_sys_job_log_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_id ON ONLY public.t_sys_job_log USING btree (id);


--
-- Name: idx_sys_job_log_job_start; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_job_start ON ONLY public.t_sys_job_log USING btree (job_id, start_time DESC, id DESC);


--
-- Name: idx_sys_job_log_status_start; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_status_start ON ONLY public.t_sys_job_log USING btree (status, start_time DESC, id DESC);


--
-- Name: idx_sys_job_log_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_log_trace_id ON ONLY public.t_sys_job_log USING btree (trace_id);


--
-- Name: idx_sys_job_mutex_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_job_mutex_key ON public.t_sys_job USING btree (mutex_key) WHERE (mutex_key IS NOT NULL);


--
-- Name: idx_sys_job_name_group; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_sys_job_name_group ON public.t_sys_job USING btree (job_name, job_group);


--
-- Name: idx_sys_job_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_sys_job_number ON public.t_sys_job USING btree (number);


--
-- Name: idx_sys_login_log_history_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_history_id ON ONLY public.t_sys_login_log_history USING btree (id);


--
-- Name: idx_sys_login_log_history_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_history_trace_id ON ONLY public.t_sys_login_log_history USING btree (trace_id);


--
-- Name: idx_sys_login_log_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_id ON ONLY public.t_sys_login_log USING btree (id);


--
-- Name: idx_sys_login_log_name_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_name_time ON ONLY public.t_sys_login_log USING btree (username, create_time DESC);


--
-- Name: idx_sys_login_log_result_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_result_time ON ONLY public.t_sys_login_log USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: idx_sys_login_log_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_trace_id ON ONLY public.t_sys_login_log USING btree (trace_id);


--
-- Name: idx_sys_login_log_user_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_login_log_user_time ON ONLY public.t_sys_login_log USING btree (user_id, create_time DESC);


--
-- Name: idx_sys_menu_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_menu_app ON public.t_sys_menu USING btree (app_id);


--
-- Name: idx_sys_menu_perm; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_menu_perm ON public.t_sys_menu USING btree (permission_id);


--
-- Name: idx_sys_operate_log_history_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_history_id ON ONLY public.t_sys_operate_log_history USING btree (id);


--
-- Name: idx_sys_operate_log_history_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_history_trace_id ON ONLY public.t_sys_operate_log_history USING btree (trace_id);


--
-- Name: idx_sys_operate_log_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_id ON ONLY public.t_sys_operate_log USING btree (id);


--
-- Name: idx_sys_operate_log_result_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_result_time ON ONLY public.t_sys_operate_log USING btree (success, create_time DESC, id DESC);


--
-- Name: idx_sys_operate_log_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_trace_id ON ONLY public.t_sys_operate_log USING btree (trace_id);


--
-- Name: idx_sys_operate_log_user_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_operate_log_user_time ON ONLY public.t_sys_operate_log USING btree (user_id, create_time DESC);


--
-- Name: idx_sys_org_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_org_parent ON public.t_sys_org USING btree (parent_id);


--
-- Name: idx_sys_param_app_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_param_app_id ON public.t_sys_param USING btree (app_id);


--
-- Name: idx_sys_param_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_sys_param_number ON public.t_sys_param USING btree (number);


--
-- Name: idx_sys_perm_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_perm_app ON public.t_sys_permission USING btree (app_id);


--
-- Name: idx_sys_perm_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_sys_perm_number ON public.t_sys_permission USING btree (number);


--
-- Name: idx_sys_role_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_sys_role_number ON public.t_sys_role USING btree (number);


--
-- Name: idx_sys_rp_perm; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_rp_perm ON public.t_sys_role_perms USING btree (permission_id);


--
-- Name: idx_sys_rp_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_rp_role ON public.t_sys_role_perms USING btree (role_id);


--
-- Name: idx_sys_script_log_history_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_script_log_history_id ON ONLY public.t_sys_script_log_history USING btree (id DESC);


--
-- Name: idx_sys_script_log_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_script_log_id ON ONLY public.t_sys_script_log USING btree (id DESC);


--
-- Name: idx_sys_script_log_status_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_script_log_status_time ON ONLY public.t_sys_script_log USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: idx_sys_sql_log_history_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_sql_log_history_id ON ONLY public.t_sys_sql_log_history USING btree (id DESC);


--
-- Name: idx_sys_sql_log_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_sql_log_id ON ONLY public.t_sys_sql_log USING btree (id DESC);


--
-- Name: idx_sys_sql_log_result_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_sql_log_result_time ON ONLY public.t_sys_sql_log USING btree (result_type, create_time DESC, id DESC);


--
-- Name: idx_sys_ur_org; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_ur_org ON public.t_sys_user_role USING btree (org_id);


--
-- Name: idx_sys_ur_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_ur_user ON public.t_sys_user_role USING btree (user_id);


--
-- Name: idx_sys_user_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_user_username ON public.t_sys_user USING btree (username);


--
-- Name: t_sys_job_log_default_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_default_fire_instance_id_idx ON public.t_sys_job_log_default USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_default_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_default_id_idx ON public.t_sys_job_log_default USING btree (id);


--
-- Name: t_sys_job_log_default_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_default_job_id_start_time_id_idx ON public.t_sys_job_log_default USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_default_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_default_status_start_time_id_idx ON public.t_sys_job_log_default USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_default_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_default_trace_id_idx ON public.t_sys_job_log_default USING btree (trace_id);


--
-- Name: t_sys_job_log_p202601_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202601_fire_instance_id_idx ON public.t_sys_job_log_p202601 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202601_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202601_id_idx ON public.t_sys_job_log_p202601 USING btree (id);


--
-- Name: t_sys_job_log_p202601_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202601_job_id_start_time_id_idx ON public.t_sys_job_log_p202601 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202601_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202601_status_start_time_id_idx ON public.t_sys_job_log_p202601 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202601_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202601_trace_id_idx ON public.t_sys_job_log_p202601 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202602_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202602_fire_instance_id_idx ON public.t_sys_job_log_p202602 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202602_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202602_id_idx ON public.t_sys_job_log_p202602 USING btree (id);


--
-- Name: t_sys_job_log_p202602_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202602_job_id_start_time_id_idx ON public.t_sys_job_log_p202602 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202602_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202602_status_start_time_id_idx ON public.t_sys_job_log_p202602 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202602_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202602_trace_id_idx ON public.t_sys_job_log_p202602 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202603_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202603_fire_instance_id_idx ON public.t_sys_job_log_p202603 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202603_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202603_id_idx ON public.t_sys_job_log_p202603 USING btree (id);


--
-- Name: t_sys_job_log_p202603_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202603_job_id_start_time_id_idx ON public.t_sys_job_log_p202603 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202603_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202603_status_start_time_id_idx ON public.t_sys_job_log_p202603 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202603_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202603_trace_id_idx ON public.t_sys_job_log_p202603 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202604_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202604_fire_instance_id_idx ON public.t_sys_job_log_p202604 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202604_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202604_id_idx ON public.t_sys_job_log_p202604 USING btree (id);


--
-- Name: t_sys_job_log_p202604_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202604_job_id_start_time_id_idx ON public.t_sys_job_log_p202604 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202604_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202604_status_start_time_id_idx ON public.t_sys_job_log_p202604 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202604_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202604_trace_id_idx ON public.t_sys_job_log_p202604 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202605_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202605_fire_instance_id_idx ON public.t_sys_job_log_p202605 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202605_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202605_id_idx ON public.t_sys_job_log_p202605 USING btree (id);


--
-- Name: t_sys_job_log_p202605_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202605_job_id_start_time_id_idx ON public.t_sys_job_log_p202605 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202605_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202605_status_start_time_id_idx ON public.t_sys_job_log_p202605 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202605_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202605_trace_id_idx ON public.t_sys_job_log_p202605 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202606_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202606_fire_instance_id_idx ON public.t_sys_job_log_p202606 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202606_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202606_id_idx ON public.t_sys_job_log_p202606 USING btree (id);


--
-- Name: t_sys_job_log_p202606_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202606_job_id_start_time_id_idx ON public.t_sys_job_log_p202606 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202606_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202606_status_start_time_id_idx ON public.t_sys_job_log_p202606 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202606_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202606_trace_id_idx ON public.t_sys_job_log_p202606 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202607_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202607_fire_instance_id_idx ON public.t_sys_job_log_p202607 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202607_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202607_id_idx ON public.t_sys_job_log_p202607 USING btree (id);


--
-- Name: t_sys_job_log_p202607_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202607_job_id_start_time_id_idx ON public.t_sys_job_log_p202607 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202607_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202607_status_start_time_id_idx ON public.t_sys_job_log_p202607 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202607_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202607_trace_id_idx ON public.t_sys_job_log_p202607 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202608_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202608_fire_instance_id_idx ON public.t_sys_job_log_p202608 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202608_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202608_id_idx ON public.t_sys_job_log_p202608 USING btree (id);


--
-- Name: t_sys_job_log_p202608_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202608_job_id_start_time_id_idx ON public.t_sys_job_log_p202608 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202608_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202608_status_start_time_id_idx ON public.t_sys_job_log_p202608 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202608_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202608_trace_id_idx ON public.t_sys_job_log_p202608 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202609_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202609_fire_instance_id_idx ON public.t_sys_job_log_p202609 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202609_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202609_id_idx ON public.t_sys_job_log_p202609 USING btree (id);


--
-- Name: t_sys_job_log_p202609_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202609_job_id_start_time_id_idx ON public.t_sys_job_log_p202609 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202609_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202609_status_start_time_id_idx ON public.t_sys_job_log_p202609 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202609_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202609_trace_id_idx ON public.t_sys_job_log_p202609 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202610_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202610_fire_instance_id_idx ON public.t_sys_job_log_p202610 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202610_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202610_id_idx ON public.t_sys_job_log_p202610 USING btree (id);


--
-- Name: t_sys_job_log_p202610_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202610_job_id_start_time_id_idx ON public.t_sys_job_log_p202610 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202610_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202610_status_start_time_id_idx ON public.t_sys_job_log_p202610 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202610_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202610_trace_id_idx ON public.t_sys_job_log_p202610 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202611_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202611_fire_instance_id_idx ON public.t_sys_job_log_p202611 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202611_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202611_id_idx ON public.t_sys_job_log_p202611 USING btree (id);


--
-- Name: t_sys_job_log_p202611_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202611_job_id_start_time_id_idx ON public.t_sys_job_log_p202611 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202611_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202611_status_start_time_id_idx ON public.t_sys_job_log_p202611 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202611_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202611_trace_id_idx ON public.t_sys_job_log_p202611 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202612_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202612_fire_instance_id_idx ON public.t_sys_job_log_p202612 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202612_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202612_id_idx ON public.t_sys_job_log_p202612 USING btree (id);


--
-- Name: t_sys_job_log_p202612_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202612_job_id_start_time_id_idx ON public.t_sys_job_log_p202612 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202612_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202612_status_start_time_id_idx ON public.t_sys_job_log_p202612 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202612_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202612_trace_id_idx ON public.t_sys_job_log_p202612 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202701_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202701_fire_instance_id_idx ON public.t_sys_job_log_p202701 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202701_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202701_id_idx ON public.t_sys_job_log_p202701 USING btree (id);


--
-- Name: t_sys_job_log_p202701_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202701_job_id_start_time_id_idx ON public.t_sys_job_log_p202701 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202701_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202701_status_start_time_id_idx ON public.t_sys_job_log_p202701 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202701_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202701_trace_id_idx ON public.t_sys_job_log_p202701 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202702_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202702_fire_instance_id_idx ON public.t_sys_job_log_p202702 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202702_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202702_id_idx ON public.t_sys_job_log_p202702 USING btree (id);


--
-- Name: t_sys_job_log_p202702_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202702_job_id_start_time_id_idx ON public.t_sys_job_log_p202702 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202702_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202702_status_start_time_id_idx ON public.t_sys_job_log_p202702 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202702_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202702_trace_id_idx ON public.t_sys_job_log_p202702 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202703_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202703_fire_instance_id_idx ON public.t_sys_job_log_p202703 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202703_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202703_id_idx ON public.t_sys_job_log_p202703 USING btree (id);


--
-- Name: t_sys_job_log_p202703_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202703_job_id_start_time_id_idx ON public.t_sys_job_log_p202703 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202703_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202703_status_start_time_id_idx ON public.t_sys_job_log_p202703 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202703_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202703_trace_id_idx ON public.t_sys_job_log_p202703 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202704_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202704_fire_instance_id_idx ON public.t_sys_job_log_p202704 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202704_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202704_id_idx ON public.t_sys_job_log_p202704 USING btree (id);


--
-- Name: t_sys_job_log_p202704_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202704_job_id_start_time_id_idx ON public.t_sys_job_log_p202704 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202704_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202704_status_start_time_id_idx ON public.t_sys_job_log_p202704 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202704_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202704_trace_id_idx ON public.t_sys_job_log_p202704 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202705_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202705_fire_instance_id_idx ON public.t_sys_job_log_p202705 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202705_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202705_id_idx ON public.t_sys_job_log_p202705 USING btree (id);


--
-- Name: t_sys_job_log_p202705_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202705_job_id_start_time_id_idx ON public.t_sys_job_log_p202705 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202705_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202705_status_start_time_id_idx ON public.t_sys_job_log_p202705 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202705_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202705_trace_id_idx ON public.t_sys_job_log_p202705 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202706_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202706_fire_instance_id_idx ON public.t_sys_job_log_p202706 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202706_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202706_id_idx ON public.t_sys_job_log_p202706 USING btree (id);


--
-- Name: t_sys_job_log_p202706_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202706_job_id_start_time_id_idx ON public.t_sys_job_log_p202706 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202706_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202706_status_start_time_id_idx ON public.t_sys_job_log_p202706 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202706_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202706_trace_id_idx ON public.t_sys_job_log_p202706 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202707_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202707_fire_instance_id_idx ON public.t_sys_job_log_p202707 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202707_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202707_id_idx ON public.t_sys_job_log_p202707 USING btree (id);


--
-- Name: t_sys_job_log_p202707_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202707_job_id_start_time_id_idx ON public.t_sys_job_log_p202707 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202707_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202707_status_start_time_id_idx ON public.t_sys_job_log_p202707 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202707_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202707_trace_id_idx ON public.t_sys_job_log_p202707 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202708_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202708_fire_instance_id_idx ON public.t_sys_job_log_p202708 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202708_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202708_id_idx ON public.t_sys_job_log_p202708 USING btree (id);


--
-- Name: t_sys_job_log_p202708_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202708_job_id_start_time_id_idx ON public.t_sys_job_log_p202708 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202708_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202708_status_start_time_id_idx ON public.t_sys_job_log_p202708 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202708_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202708_trace_id_idx ON public.t_sys_job_log_p202708 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202709_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202709_fire_instance_id_idx ON public.t_sys_job_log_p202709 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202709_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202709_id_idx ON public.t_sys_job_log_p202709 USING btree (id);


--
-- Name: t_sys_job_log_p202709_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202709_job_id_start_time_id_idx ON public.t_sys_job_log_p202709 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202709_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202709_status_start_time_id_idx ON public.t_sys_job_log_p202709 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202709_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202709_trace_id_idx ON public.t_sys_job_log_p202709 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202710_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202710_fire_instance_id_idx ON public.t_sys_job_log_p202710 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202710_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202710_id_idx ON public.t_sys_job_log_p202710 USING btree (id);


--
-- Name: t_sys_job_log_p202710_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202710_job_id_start_time_id_idx ON public.t_sys_job_log_p202710 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202710_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202710_status_start_time_id_idx ON public.t_sys_job_log_p202710 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202710_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202710_trace_id_idx ON public.t_sys_job_log_p202710 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202711_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202711_fire_instance_id_idx ON public.t_sys_job_log_p202711 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202711_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202711_id_idx ON public.t_sys_job_log_p202711 USING btree (id);


--
-- Name: t_sys_job_log_p202711_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202711_job_id_start_time_id_idx ON public.t_sys_job_log_p202711 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202711_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202711_status_start_time_id_idx ON public.t_sys_job_log_p202711 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202711_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202711_trace_id_idx ON public.t_sys_job_log_p202711 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202712_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202712_fire_instance_id_idx ON public.t_sys_job_log_p202712 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202712_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202712_id_idx ON public.t_sys_job_log_p202712 USING btree (id);


--
-- Name: t_sys_job_log_p202712_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202712_job_id_start_time_id_idx ON public.t_sys_job_log_p202712 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202712_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202712_status_start_time_id_idx ON public.t_sys_job_log_p202712 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202712_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202712_trace_id_idx ON public.t_sys_job_log_p202712 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202801_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202801_fire_instance_id_idx ON public.t_sys_job_log_p202801 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202801_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202801_id_idx ON public.t_sys_job_log_p202801 USING btree (id);


--
-- Name: t_sys_job_log_p202801_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202801_job_id_start_time_id_idx ON public.t_sys_job_log_p202801 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202801_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202801_status_start_time_id_idx ON public.t_sys_job_log_p202801 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202801_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202801_trace_id_idx ON public.t_sys_job_log_p202801 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202802_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202802_fire_instance_id_idx ON public.t_sys_job_log_p202802 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202802_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202802_id_idx ON public.t_sys_job_log_p202802 USING btree (id);


--
-- Name: t_sys_job_log_p202802_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202802_job_id_start_time_id_idx ON public.t_sys_job_log_p202802 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202802_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202802_status_start_time_id_idx ON public.t_sys_job_log_p202802 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202802_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202802_trace_id_idx ON public.t_sys_job_log_p202802 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202803_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202803_fire_instance_id_idx ON public.t_sys_job_log_p202803 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202803_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202803_id_idx ON public.t_sys_job_log_p202803 USING btree (id);


--
-- Name: t_sys_job_log_p202803_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202803_job_id_start_time_id_idx ON public.t_sys_job_log_p202803 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202803_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202803_status_start_time_id_idx ON public.t_sys_job_log_p202803 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202803_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202803_trace_id_idx ON public.t_sys_job_log_p202803 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202804_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202804_fire_instance_id_idx ON public.t_sys_job_log_p202804 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202804_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202804_id_idx ON public.t_sys_job_log_p202804 USING btree (id);


--
-- Name: t_sys_job_log_p202804_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202804_job_id_start_time_id_idx ON public.t_sys_job_log_p202804 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202804_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202804_status_start_time_id_idx ON public.t_sys_job_log_p202804 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202804_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202804_trace_id_idx ON public.t_sys_job_log_p202804 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202805_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202805_fire_instance_id_idx ON public.t_sys_job_log_p202805 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202805_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202805_id_idx ON public.t_sys_job_log_p202805 USING btree (id);


--
-- Name: t_sys_job_log_p202805_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202805_job_id_start_time_id_idx ON public.t_sys_job_log_p202805 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202805_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202805_status_start_time_id_idx ON public.t_sys_job_log_p202805 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202805_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202805_trace_id_idx ON public.t_sys_job_log_p202805 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202806_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202806_fire_instance_id_idx ON public.t_sys_job_log_p202806 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202806_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202806_id_idx ON public.t_sys_job_log_p202806 USING btree (id);


--
-- Name: t_sys_job_log_p202806_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202806_job_id_start_time_id_idx ON public.t_sys_job_log_p202806 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202806_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202806_status_start_time_id_idx ON public.t_sys_job_log_p202806 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202806_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202806_trace_id_idx ON public.t_sys_job_log_p202806 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202807_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202807_fire_instance_id_idx ON public.t_sys_job_log_p202807 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202807_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202807_id_idx ON public.t_sys_job_log_p202807 USING btree (id);


--
-- Name: t_sys_job_log_p202807_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202807_job_id_start_time_id_idx ON public.t_sys_job_log_p202807 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202807_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202807_status_start_time_id_idx ON public.t_sys_job_log_p202807 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202807_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202807_trace_id_idx ON public.t_sys_job_log_p202807 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202808_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202808_fire_instance_id_idx ON public.t_sys_job_log_p202808 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202808_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202808_id_idx ON public.t_sys_job_log_p202808 USING btree (id);


--
-- Name: t_sys_job_log_p202808_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202808_job_id_start_time_id_idx ON public.t_sys_job_log_p202808 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202808_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202808_status_start_time_id_idx ON public.t_sys_job_log_p202808 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202808_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202808_trace_id_idx ON public.t_sys_job_log_p202808 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202809_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202809_fire_instance_id_idx ON public.t_sys_job_log_p202809 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202809_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202809_id_idx ON public.t_sys_job_log_p202809 USING btree (id);


--
-- Name: t_sys_job_log_p202809_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202809_job_id_start_time_id_idx ON public.t_sys_job_log_p202809 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202809_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202809_status_start_time_id_idx ON public.t_sys_job_log_p202809 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202809_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202809_trace_id_idx ON public.t_sys_job_log_p202809 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202810_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202810_fire_instance_id_idx ON public.t_sys_job_log_p202810 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202810_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202810_id_idx ON public.t_sys_job_log_p202810 USING btree (id);


--
-- Name: t_sys_job_log_p202810_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202810_job_id_start_time_id_idx ON public.t_sys_job_log_p202810 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202810_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202810_status_start_time_id_idx ON public.t_sys_job_log_p202810 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202810_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202810_trace_id_idx ON public.t_sys_job_log_p202810 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202811_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202811_fire_instance_id_idx ON public.t_sys_job_log_p202811 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202811_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202811_id_idx ON public.t_sys_job_log_p202811 USING btree (id);


--
-- Name: t_sys_job_log_p202811_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202811_job_id_start_time_id_idx ON public.t_sys_job_log_p202811 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202811_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202811_status_start_time_id_idx ON public.t_sys_job_log_p202811 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202811_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202811_trace_id_idx ON public.t_sys_job_log_p202811 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202812_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202812_fire_instance_id_idx ON public.t_sys_job_log_p202812 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202812_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202812_id_idx ON public.t_sys_job_log_p202812 USING btree (id);


--
-- Name: t_sys_job_log_p202812_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202812_job_id_start_time_id_idx ON public.t_sys_job_log_p202812 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202812_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202812_status_start_time_id_idx ON public.t_sys_job_log_p202812 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202812_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202812_trace_id_idx ON public.t_sys_job_log_p202812 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202901_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202901_fire_instance_id_idx ON public.t_sys_job_log_p202901 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202901_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202901_id_idx ON public.t_sys_job_log_p202901 USING btree (id);


--
-- Name: t_sys_job_log_p202901_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202901_job_id_start_time_id_idx ON public.t_sys_job_log_p202901 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202901_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202901_status_start_time_id_idx ON public.t_sys_job_log_p202901 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202901_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202901_trace_id_idx ON public.t_sys_job_log_p202901 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202902_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202902_fire_instance_id_idx ON public.t_sys_job_log_p202902 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202902_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202902_id_idx ON public.t_sys_job_log_p202902 USING btree (id);


--
-- Name: t_sys_job_log_p202902_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202902_job_id_start_time_id_idx ON public.t_sys_job_log_p202902 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202902_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202902_status_start_time_id_idx ON public.t_sys_job_log_p202902 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202902_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202902_trace_id_idx ON public.t_sys_job_log_p202902 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202903_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202903_fire_instance_id_idx ON public.t_sys_job_log_p202903 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202903_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202903_id_idx ON public.t_sys_job_log_p202903 USING btree (id);


--
-- Name: t_sys_job_log_p202903_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202903_job_id_start_time_id_idx ON public.t_sys_job_log_p202903 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202903_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202903_status_start_time_id_idx ON public.t_sys_job_log_p202903 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202903_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202903_trace_id_idx ON public.t_sys_job_log_p202903 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202904_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202904_fire_instance_id_idx ON public.t_sys_job_log_p202904 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202904_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202904_id_idx ON public.t_sys_job_log_p202904 USING btree (id);


--
-- Name: t_sys_job_log_p202904_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202904_job_id_start_time_id_idx ON public.t_sys_job_log_p202904 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202904_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202904_status_start_time_id_idx ON public.t_sys_job_log_p202904 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202904_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202904_trace_id_idx ON public.t_sys_job_log_p202904 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202905_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202905_fire_instance_id_idx ON public.t_sys_job_log_p202905 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202905_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202905_id_idx ON public.t_sys_job_log_p202905 USING btree (id);


--
-- Name: t_sys_job_log_p202905_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202905_job_id_start_time_id_idx ON public.t_sys_job_log_p202905 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202905_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202905_status_start_time_id_idx ON public.t_sys_job_log_p202905 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202905_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202905_trace_id_idx ON public.t_sys_job_log_p202905 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202906_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202906_fire_instance_id_idx ON public.t_sys_job_log_p202906 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202906_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202906_id_idx ON public.t_sys_job_log_p202906 USING btree (id);


--
-- Name: t_sys_job_log_p202906_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202906_job_id_start_time_id_idx ON public.t_sys_job_log_p202906 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202906_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202906_status_start_time_id_idx ON public.t_sys_job_log_p202906 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202906_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202906_trace_id_idx ON public.t_sys_job_log_p202906 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202907_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202907_fire_instance_id_idx ON public.t_sys_job_log_p202907 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202907_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202907_id_idx ON public.t_sys_job_log_p202907 USING btree (id);


--
-- Name: t_sys_job_log_p202907_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202907_job_id_start_time_id_idx ON public.t_sys_job_log_p202907 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202907_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202907_status_start_time_id_idx ON public.t_sys_job_log_p202907 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202907_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202907_trace_id_idx ON public.t_sys_job_log_p202907 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202908_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202908_fire_instance_id_idx ON public.t_sys_job_log_p202908 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202908_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202908_id_idx ON public.t_sys_job_log_p202908 USING btree (id);


--
-- Name: t_sys_job_log_p202908_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202908_job_id_start_time_id_idx ON public.t_sys_job_log_p202908 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202908_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202908_status_start_time_id_idx ON public.t_sys_job_log_p202908 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202908_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202908_trace_id_idx ON public.t_sys_job_log_p202908 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202909_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202909_fire_instance_id_idx ON public.t_sys_job_log_p202909 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202909_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202909_id_idx ON public.t_sys_job_log_p202909 USING btree (id);


--
-- Name: t_sys_job_log_p202909_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202909_job_id_start_time_id_idx ON public.t_sys_job_log_p202909 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202909_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202909_status_start_time_id_idx ON public.t_sys_job_log_p202909 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202909_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202909_trace_id_idx ON public.t_sys_job_log_p202909 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202910_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202910_fire_instance_id_idx ON public.t_sys_job_log_p202910 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202910_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202910_id_idx ON public.t_sys_job_log_p202910 USING btree (id);


--
-- Name: t_sys_job_log_p202910_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202910_job_id_start_time_id_idx ON public.t_sys_job_log_p202910 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202910_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202910_status_start_time_id_idx ON public.t_sys_job_log_p202910 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202910_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202910_trace_id_idx ON public.t_sys_job_log_p202910 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202911_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202911_fire_instance_id_idx ON public.t_sys_job_log_p202911 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202911_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202911_id_idx ON public.t_sys_job_log_p202911 USING btree (id);


--
-- Name: t_sys_job_log_p202911_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202911_job_id_start_time_id_idx ON public.t_sys_job_log_p202911 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202911_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202911_status_start_time_id_idx ON public.t_sys_job_log_p202911 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202911_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202911_trace_id_idx ON public.t_sys_job_log_p202911 USING btree (trace_id);


--
-- Name: t_sys_job_log_p202912_fire_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202912_fire_instance_id_idx ON public.t_sys_job_log_p202912 USING btree (fire_instance_id);


--
-- Name: t_sys_job_log_p202912_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202912_id_idx ON public.t_sys_job_log_p202912 USING btree (id);


--
-- Name: t_sys_job_log_p202912_job_id_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202912_job_id_start_time_id_idx ON public.t_sys_job_log_p202912 USING btree (job_id, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202912_status_start_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202912_status_start_time_id_idx ON public.t_sys_job_log_p202912 USING btree (status, start_time DESC, id DESC);


--
-- Name: t_sys_job_log_p202912_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_job_log_p202912_trace_id_idx ON public.t_sys_job_log_p202912 USING btree (trace_id);


--
-- Name: t_sys_login_log_default_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_default_id_idx ON public.t_sys_login_log_default USING btree (id);


--
-- Name: t_sys_login_log_default_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_default_success_event_type_create_time_id_idx ON public.t_sys_login_log_default USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_default_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_default_trace_id_idx ON public.t_sys_login_log_default USING btree (trace_id);


--
-- Name: t_sys_login_log_default_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_default_user_id_create_time_idx ON public.t_sys_login_log_default USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_default_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_default_username_create_time_idx ON public.t_sys_login_log_default USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202601_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202601_id_idx ON public.t_sys_login_log_p202601 USING btree (id);


--
-- Name: t_sys_login_log_p202601_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202601_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202601 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202601_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202601_trace_id_idx ON public.t_sys_login_log_p202601 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202601_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202601_user_id_create_time_idx ON public.t_sys_login_log_p202601 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202601_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202601_username_create_time_idx ON public.t_sys_login_log_p202601 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202602_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202602_id_idx ON public.t_sys_login_log_p202602 USING btree (id);


--
-- Name: t_sys_login_log_p202602_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202602_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202602 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202602_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202602_trace_id_idx ON public.t_sys_login_log_p202602 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202602_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202602_user_id_create_time_idx ON public.t_sys_login_log_p202602 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202602_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202602_username_create_time_idx ON public.t_sys_login_log_p202602 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202603_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202603_id_idx ON public.t_sys_login_log_p202603 USING btree (id);


--
-- Name: t_sys_login_log_p202603_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202603_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202603 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202603_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202603_trace_id_idx ON public.t_sys_login_log_p202603 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202603_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202603_user_id_create_time_idx ON public.t_sys_login_log_p202603 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202603_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202603_username_create_time_idx ON public.t_sys_login_log_p202603 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202604_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202604_id_idx ON public.t_sys_login_log_p202604 USING btree (id);


--
-- Name: t_sys_login_log_p202604_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202604_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202604 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202604_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202604_trace_id_idx ON public.t_sys_login_log_p202604 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202604_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202604_user_id_create_time_idx ON public.t_sys_login_log_p202604 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202604_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202604_username_create_time_idx ON public.t_sys_login_log_p202604 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202605_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202605_id_idx ON public.t_sys_login_log_p202605 USING btree (id);


--
-- Name: t_sys_login_log_p202605_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202605_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202605 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202605_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202605_trace_id_idx ON public.t_sys_login_log_p202605 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202605_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202605_user_id_create_time_idx ON public.t_sys_login_log_p202605 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202605_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202605_username_create_time_idx ON public.t_sys_login_log_p202605 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202606_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202606_id_idx ON public.t_sys_login_log_p202606 USING btree (id);


--
-- Name: t_sys_login_log_p202606_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202606_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202606 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202606_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202606_trace_id_idx ON public.t_sys_login_log_p202606 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202606_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202606_user_id_create_time_idx ON public.t_sys_login_log_p202606 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202606_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202606_username_create_time_idx ON public.t_sys_login_log_p202606 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202607_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202607_id_idx ON public.t_sys_login_log_p202607 USING btree (id);


--
-- Name: t_sys_login_log_p202607_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202607_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202607 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202607_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202607_trace_id_idx ON public.t_sys_login_log_p202607 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202607_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202607_user_id_create_time_idx ON public.t_sys_login_log_p202607 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202607_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202607_username_create_time_idx ON public.t_sys_login_log_p202607 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202608_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202608_id_idx ON public.t_sys_login_log_p202608 USING btree (id);


--
-- Name: t_sys_login_log_p202608_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202608_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202608 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202608_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202608_trace_id_idx ON public.t_sys_login_log_p202608 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202608_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202608_user_id_create_time_idx ON public.t_sys_login_log_p202608 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202608_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202608_username_create_time_idx ON public.t_sys_login_log_p202608 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202609_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202609_id_idx ON public.t_sys_login_log_p202609 USING btree (id);


--
-- Name: t_sys_login_log_p202609_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202609_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202609 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202609_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202609_trace_id_idx ON public.t_sys_login_log_p202609 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202609_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202609_user_id_create_time_idx ON public.t_sys_login_log_p202609 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202609_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202609_username_create_time_idx ON public.t_sys_login_log_p202609 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202610_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202610_id_idx ON public.t_sys_login_log_p202610 USING btree (id);


--
-- Name: t_sys_login_log_p202610_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202610_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202610 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202610_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202610_trace_id_idx ON public.t_sys_login_log_p202610 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202610_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202610_user_id_create_time_idx ON public.t_sys_login_log_p202610 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202610_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202610_username_create_time_idx ON public.t_sys_login_log_p202610 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202611_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202611_id_idx ON public.t_sys_login_log_p202611 USING btree (id);


--
-- Name: t_sys_login_log_p202611_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202611_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202611 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202611_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202611_trace_id_idx ON public.t_sys_login_log_p202611 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202611_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202611_user_id_create_time_idx ON public.t_sys_login_log_p202611 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202611_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202611_username_create_time_idx ON public.t_sys_login_log_p202611 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202612_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202612_id_idx ON public.t_sys_login_log_p202612 USING btree (id);


--
-- Name: t_sys_login_log_p202612_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202612_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202612 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202612_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202612_trace_id_idx ON public.t_sys_login_log_p202612 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202612_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202612_user_id_create_time_idx ON public.t_sys_login_log_p202612 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202612_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202612_username_create_time_idx ON public.t_sys_login_log_p202612 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202701_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202701_id_idx ON public.t_sys_login_log_p202701 USING btree (id);


--
-- Name: t_sys_login_log_p202701_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202701_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202701 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202701_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202701_trace_id_idx ON public.t_sys_login_log_p202701 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202701_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202701_user_id_create_time_idx ON public.t_sys_login_log_p202701 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202701_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202701_username_create_time_idx ON public.t_sys_login_log_p202701 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202702_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202702_id_idx ON public.t_sys_login_log_p202702 USING btree (id);


--
-- Name: t_sys_login_log_p202702_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202702_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202702 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202702_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202702_trace_id_idx ON public.t_sys_login_log_p202702 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202702_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202702_user_id_create_time_idx ON public.t_sys_login_log_p202702 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202702_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202702_username_create_time_idx ON public.t_sys_login_log_p202702 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202703_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202703_id_idx ON public.t_sys_login_log_p202703 USING btree (id);


--
-- Name: t_sys_login_log_p202703_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202703_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202703 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202703_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202703_trace_id_idx ON public.t_sys_login_log_p202703 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202703_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202703_user_id_create_time_idx ON public.t_sys_login_log_p202703 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202703_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202703_username_create_time_idx ON public.t_sys_login_log_p202703 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202704_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202704_id_idx ON public.t_sys_login_log_p202704 USING btree (id);


--
-- Name: t_sys_login_log_p202704_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202704_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202704 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202704_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202704_trace_id_idx ON public.t_sys_login_log_p202704 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202704_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202704_user_id_create_time_idx ON public.t_sys_login_log_p202704 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202704_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202704_username_create_time_idx ON public.t_sys_login_log_p202704 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202705_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202705_id_idx ON public.t_sys_login_log_p202705 USING btree (id);


--
-- Name: t_sys_login_log_p202705_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202705_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202705 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202705_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202705_trace_id_idx ON public.t_sys_login_log_p202705 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202705_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202705_user_id_create_time_idx ON public.t_sys_login_log_p202705 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202705_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202705_username_create_time_idx ON public.t_sys_login_log_p202705 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202706_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202706_id_idx ON public.t_sys_login_log_p202706 USING btree (id);


--
-- Name: t_sys_login_log_p202706_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202706_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202706 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202706_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202706_trace_id_idx ON public.t_sys_login_log_p202706 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202706_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202706_user_id_create_time_idx ON public.t_sys_login_log_p202706 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202706_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202706_username_create_time_idx ON public.t_sys_login_log_p202706 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202707_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202707_id_idx ON public.t_sys_login_log_p202707 USING btree (id);


--
-- Name: t_sys_login_log_p202707_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202707_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202707 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202707_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202707_trace_id_idx ON public.t_sys_login_log_p202707 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202707_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202707_user_id_create_time_idx ON public.t_sys_login_log_p202707 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202707_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202707_username_create_time_idx ON public.t_sys_login_log_p202707 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202708_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202708_id_idx ON public.t_sys_login_log_p202708 USING btree (id);


--
-- Name: t_sys_login_log_p202708_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202708_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202708 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202708_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202708_trace_id_idx ON public.t_sys_login_log_p202708 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202708_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202708_user_id_create_time_idx ON public.t_sys_login_log_p202708 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202708_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202708_username_create_time_idx ON public.t_sys_login_log_p202708 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202709_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202709_id_idx ON public.t_sys_login_log_p202709 USING btree (id);


--
-- Name: t_sys_login_log_p202709_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202709_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202709 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202709_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202709_trace_id_idx ON public.t_sys_login_log_p202709 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202709_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202709_user_id_create_time_idx ON public.t_sys_login_log_p202709 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202709_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202709_username_create_time_idx ON public.t_sys_login_log_p202709 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202710_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202710_id_idx ON public.t_sys_login_log_p202710 USING btree (id);


--
-- Name: t_sys_login_log_p202710_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202710_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202710 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202710_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202710_trace_id_idx ON public.t_sys_login_log_p202710 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202710_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202710_user_id_create_time_idx ON public.t_sys_login_log_p202710 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202710_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202710_username_create_time_idx ON public.t_sys_login_log_p202710 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202711_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202711_id_idx ON public.t_sys_login_log_p202711 USING btree (id);


--
-- Name: t_sys_login_log_p202711_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202711_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202711 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202711_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202711_trace_id_idx ON public.t_sys_login_log_p202711 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202711_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202711_user_id_create_time_idx ON public.t_sys_login_log_p202711 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202711_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202711_username_create_time_idx ON public.t_sys_login_log_p202711 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202712_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202712_id_idx ON public.t_sys_login_log_p202712 USING btree (id);


--
-- Name: t_sys_login_log_p202712_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202712_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202712 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202712_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202712_trace_id_idx ON public.t_sys_login_log_p202712 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202712_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202712_user_id_create_time_idx ON public.t_sys_login_log_p202712 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202712_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202712_username_create_time_idx ON public.t_sys_login_log_p202712 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202801_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202801_id_idx ON public.t_sys_login_log_p202801 USING btree (id);


--
-- Name: t_sys_login_log_p202801_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202801_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202801 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202801_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202801_trace_id_idx ON public.t_sys_login_log_p202801 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202801_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202801_user_id_create_time_idx ON public.t_sys_login_log_p202801 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202801_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202801_username_create_time_idx ON public.t_sys_login_log_p202801 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202802_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202802_id_idx ON public.t_sys_login_log_p202802 USING btree (id);


--
-- Name: t_sys_login_log_p202802_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202802_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202802 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202802_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202802_trace_id_idx ON public.t_sys_login_log_p202802 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202802_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202802_user_id_create_time_idx ON public.t_sys_login_log_p202802 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202802_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202802_username_create_time_idx ON public.t_sys_login_log_p202802 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202803_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202803_id_idx ON public.t_sys_login_log_p202803 USING btree (id);


--
-- Name: t_sys_login_log_p202803_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202803_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202803 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202803_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202803_trace_id_idx ON public.t_sys_login_log_p202803 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202803_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202803_user_id_create_time_idx ON public.t_sys_login_log_p202803 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202803_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202803_username_create_time_idx ON public.t_sys_login_log_p202803 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202804_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202804_id_idx ON public.t_sys_login_log_p202804 USING btree (id);


--
-- Name: t_sys_login_log_p202804_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202804_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202804 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202804_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202804_trace_id_idx ON public.t_sys_login_log_p202804 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202804_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202804_user_id_create_time_idx ON public.t_sys_login_log_p202804 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202804_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202804_username_create_time_idx ON public.t_sys_login_log_p202804 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202805_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202805_id_idx ON public.t_sys_login_log_p202805 USING btree (id);


--
-- Name: t_sys_login_log_p202805_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202805_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202805 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202805_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202805_trace_id_idx ON public.t_sys_login_log_p202805 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202805_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202805_user_id_create_time_idx ON public.t_sys_login_log_p202805 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202805_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202805_username_create_time_idx ON public.t_sys_login_log_p202805 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202806_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202806_id_idx ON public.t_sys_login_log_p202806 USING btree (id);


--
-- Name: t_sys_login_log_p202806_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202806_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202806 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202806_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202806_trace_id_idx ON public.t_sys_login_log_p202806 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202806_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202806_user_id_create_time_idx ON public.t_sys_login_log_p202806 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202806_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202806_username_create_time_idx ON public.t_sys_login_log_p202806 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202807_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202807_id_idx ON public.t_sys_login_log_p202807 USING btree (id);


--
-- Name: t_sys_login_log_p202807_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202807_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202807 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202807_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202807_trace_id_idx ON public.t_sys_login_log_p202807 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202807_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202807_user_id_create_time_idx ON public.t_sys_login_log_p202807 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202807_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202807_username_create_time_idx ON public.t_sys_login_log_p202807 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202808_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202808_id_idx ON public.t_sys_login_log_p202808 USING btree (id);


--
-- Name: t_sys_login_log_p202808_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202808_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202808 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202808_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202808_trace_id_idx ON public.t_sys_login_log_p202808 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202808_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202808_user_id_create_time_idx ON public.t_sys_login_log_p202808 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202808_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202808_username_create_time_idx ON public.t_sys_login_log_p202808 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202809_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202809_id_idx ON public.t_sys_login_log_p202809 USING btree (id);


--
-- Name: t_sys_login_log_p202809_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202809_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202809 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202809_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202809_trace_id_idx ON public.t_sys_login_log_p202809 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202809_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202809_user_id_create_time_idx ON public.t_sys_login_log_p202809 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202809_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202809_username_create_time_idx ON public.t_sys_login_log_p202809 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202810_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202810_id_idx ON public.t_sys_login_log_p202810 USING btree (id);


--
-- Name: t_sys_login_log_p202810_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202810_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202810 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202810_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202810_trace_id_idx ON public.t_sys_login_log_p202810 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202810_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202810_user_id_create_time_idx ON public.t_sys_login_log_p202810 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202810_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202810_username_create_time_idx ON public.t_sys_login_log_p202810 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202811_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202811_id_idx ON public.t_sys_login_log_p202811 USING btree (id);


--
-- Name: t_sys_login_log_p202811_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202811_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202811 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202811_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202811_trace_id_idx ON public.t_sys_login_log_p202811 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202811_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202811_user_id_create_time_idx ON public.t_sys_login_log_p202811 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202811_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202811_username_create_time_idx ON public.t_sys_login_log_p202811 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202812_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202812_id_idx ON public.t_sys_login_log_p202812 USING btree (id);


--
-- Name: t_sys_login_log_p202812_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202812_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202812 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202812_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202812_trace_id_idx ON public.t_sys_login_log_p202812 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202812_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202812_user_id_create_time_idx ON public.t_sys_login_log_p202812 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202812_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202812_username_create_time_idx ON public.t_sys_login_log_p202812 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202901_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202901_id_idx ON public.t_sys_login_log_p202901 USING btree (id);


--
-- Name: t_sys_login_log_p202901_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202901_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202901 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202901_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202901_trace_id_idx ON public.t_sys_login_log_p202901 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202901_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202901_user_id_create_time_idx ON public.t_sys_login_log_p202901 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202901_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202901_username_create_time_idx ON public.t_sys_login_log_p202901 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202902_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202902_id_idx ON public.t_sys_login_log_p202902 USING btree (id);


--
-- Name: t_sys_login_log_p202902_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202902_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202902 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202902_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202902_trace_id_idx ON public.t_sys_login_log_p202902 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202902_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202902_user_id_create_time_idx ON public.t_sys_login_log_p202902 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202902_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202902_username_create_time_idx ON public.t_sys_login_log_p202902 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202903_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202903_id_idx ON public.t_sys_login_log_p202903 USING btree (id);


--
-- Name: t_sys_login_log_p202903_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202903_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202903 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202903_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202903_trace_id_idx ON public.t_sys_login_log_p202903 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202903_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202903_user_id_create_time_idx ON public.t_sys_login_log_p202903 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202903_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202903_username_create_time_idx ON public.t_sys_login_log_p202903 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202904_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202904_id_idx ON public.t_sys_login_log_p202904 USING btree (id);


--
-- Name: t_sys_login_log_p202904_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202904_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202904 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202904_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202904_trace_id_idx ON public.t_sys_login_log_p202904 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202904_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202904_user_id_create_time_idx ON public.t_sys_login_log_p202904 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202904_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202904_username_create_time_idx ON public.t_sys_login_log_p202904 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202905_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202905_id_idx ON public.t_sys_login_log_p202905 USING btree (id);


--
-- Name: t_sys_login_log_p202905_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202905_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202905 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202905_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202905_trace_id_idx ON public.t_sys_login_log_p202905 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202905_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202905_user_id_create_time_idx ON public.t_sys_login_log_p202905 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202905_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202905_username_create_time_idx ON public.t_sys_login_log_p202905 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202906_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202906_id_idx ON public.t_sys_login_log_p202906 USING btree (id);


--
-- Name: t_sys_login_log_p202906_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202906_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202906 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202906_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202906_trace_id_idx ON public.t_sys_login_log_p202906 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202906_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202906_user_id_create_time_idx ON public.t_sys_login_log_p202906 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202906_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202906_username_create_time_idx ON public.t_sys_login_log_p202906 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202907_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202907_id_idx ON public.t_sys_login_log_p202907 USING btree (id);


--
-- Name: t_sys_login_log_p202907_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202907_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202907 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202907_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202907_trace_id_idx ON public.t_sys_login_log_p202907 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202907_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202907_user_id_create_time_idx ON public.t_sys_login_log_p202907 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202907_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202907_username_create_time_idx ON public.t_sys_login_log_p202907 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202908_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202908_id_idx ON public.t_sys_login_log_p202908 USING btree (id);


--
-- Name: t_sys_login_log_p202908_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202908_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202908 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202908_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202908_trace_id_idx ON public.t_sys_login_log_p202908 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202908_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202908_user_id_create_time_idx ON public.t_sys_login_log_p202908 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202908_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202908_username_create_time_idx ON public.t_sys_login_log_p202908 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202909_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202909_id_idx ON public.t_sys_login_log_p202909 USING btree (id);


--
-- Name: t_sys_login_log_p202909_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202909_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202909 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202909_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202909_trace_id_idx ON public.t_sys_login_log_p202909 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202909_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202909_user_id_create_time_idx ON public.t_sys_login_log_p202909 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202909_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202909_username_create_time_idx ON public.t_sys_login_log_p202909 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202910_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202910_id_idx ON public.t_sys_login_log_p202910 USING btree (id);


--
-- Name: t_sys_login_log_p202910_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202910_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202910 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202910_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202910_trace_id_idx ON public.t_sys_login_log_p202910 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202910_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202910_user_id_create_time_idx ON public.t_sys_login_log_p202910 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202910_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202910_username_create_time_idx ON public.t_sys_login_log_p202910 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202911_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202911_id_idx ON public.t_sys_login_log_p202911 USING btree (id);


--
-- Name: t_sys_login_log_p202911_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202911_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202911 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202911_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202911_trace_id_idx ON public.t_sys_login_log_p202911 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202911_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202911_user_id_create_time_idx ON public.t_sys_login_log_p202911 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202911_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202911_username_create_time_idx ON public.t_sys_login_log_p202911 USING btree (username, create_time DESC);


--
-- Name: t_sys_login_log_p202912_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202912_id_idx ON public.t_sys_login_log_p202912 USING btree (id);


--
-- Name: t_sys_login_log_p202912_success_event_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202912_success_event_type_create_time_id_idx ON public.t_sys_login_log_p202912 USING btree (success, event_type, create_time DESC, id DESC);


--
-- Name: t_sys_login_log_p202912_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202912_trace_id_idx ON public.t_sys_login_log_p202912 USING btree (trace_id);


--
-- Name: t_sys_login_log_p202912_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202912_user_id_create_time_idx ON public.t_sys_login_log_p202912 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_login_log_p202912_username_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_login_log_p202912_username_create_time_idx ON public.t_sys_login_log_p202912 USING btree (username, create_time DESC);


--
-- Name: t_sys_operate_log_default_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_default_id_idx ON public.t_sys_operate_log_default USING btree (id);


--
-- Name: t_sys_operate_log_default_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_default_success_create_time_id_idx ON public.t_sys_operate_log_default USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_default_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_default_trace_id_idx ON public.t_sys_operate_log_default USING btree (trace_id);


--
-- Name: t_sys_operate_log_default_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_default_user_id_create_time_idx ON public.t_sys_operate_log_default USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202601_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202601_id_idx ON public.t_sys_operate_log_p202601 USING btree (id);


--
-- Name: t_sys_operate_log_p202601_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202601_success_create_time_id_idx ON public.t_sys_operate_log_p202601 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202601_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202601_trace_id_idx ON public.t_sys_operate_log_p202601 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202601_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202601_user_id_create_time_idx ON public.t_sys_operate_log_p202601 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202602_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202602_id_idx ON public.t_sys_operate_log_p202602 USING btree (id);


--
-- Name: t_sys_operate_log_p202602_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202602_success_create_time_id_idx ON public.t_sys_operate_log_p202602 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202602_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202602_trace_id_idx ON public.t_sys_operate_log_p202602 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202602_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202602_user_id_create_time_idx ON public.t_sys_operate_log_p202602 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202603_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202603_id_idx ON public.t_sys_operate_log_p202603 USING btree (id);


--
-- Name: t_sys_operate_log_p202603_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202603_success_create_time_id_idx ON public.t_sys_operate_log_p202603 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202603_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202603_trace_id_idx ON public.t_sys_operate_log_p202603 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202603_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202603_user_id_create_time_idx ON public.t_sys_operate_log_p202603 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202604_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202604_id_idx ON public.t_sys_operate_log_p202604 USING btree (id);


--
-- Name: t_sys_operate_log_p202604_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202604_success_create_time_id_idx ON public.t_sys_operate_log_p202604 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202604_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202604_trace_id_idx ON public.t_sys_operate_log_p202604 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202604_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202604_user_id_create_time_idx ON public.t_sys_operate_log_p202604 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202605_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202605_id_idx ON public.t_sys_operate_log_p202605 USING btree (id);


--
-- Name: t_sys_operate_log_p202605_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202605_success_create_time_id_idx ON public.t_sys_operate_log_p202605 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202605_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202605_trace_id_idx ON public.t_sys_operate_log_p202605 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202605_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202605_user_id_create_time_idx ON public.t_sys_operate_log_p202605 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202606_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202606_id_idx ON public.t_sys_operate_log_p202606 USING btree (id);


--
-- Name: t_sys_operate_log_p202606_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202606_success_create_time_id_idx ON public.t_sys_operate_log_p202606 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202606_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202606_trace_id_idx ON public.t_sys_operate_log_p202606 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202606_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202606_user_id_create_time_idx ON public.t_sys_operate_log_p202606 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202607_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202607_id_idx ON public.t_sys_operate_log_p202607 USING btree (id);


--
-- Name: t_sys_operate_log_p202607_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202607_success_create_time_id_idx ON public.t_sys_operate_log_p202607 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202607_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202607_trace_id_idx ON public.t_sys_operate_log_p202607 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202607_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202607_user_id_create_time_idx ON public.t_sys_operate_log_p202607 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202608_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202608_id_idx ON public.t_sys_operate_log_p202608 USING btree (id);


--
-- Name: t_sys_operate_log_p202608_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202608_success_create_time_id_idx ON public.t_sys_operate_log_p202608 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202608_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202608_trace_id_idx ON public.t_sys_operate_log_p202608 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202608_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202608_user_id_create_time_idx ON public.t_sys_operate_log_p202608 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202609_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202609_id_idx ON public.t_sys_operate_log_p202609 USING btree (id);


--
-- Name: t_sys_operate_log_p202609_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202609_success_create_time_id_idx ON public.t_sys_operate_log_p202609 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202609_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202609_trace_id_idx ON public.t_sys_operate_log_p202609 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202609_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202609_user_id_create_time_idx ON public.t_sys_operate_log_p202609 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202610_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202610_id_idx ON public.t_sys_operate_log_p202610 USING btree (id);


--
-- Name: t_sys_operate_log_p202610_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202610_success_create_time_id_idx ON public.t_sys_operate_log_p202610 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202610_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202610_trace_id_idx ON public.t_sys_operate_log_p202610 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202610_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202610_user_id_create_time_idx ON public.t_sys_operate_log_p202610 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202611_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202611_id_idx ON public.t_sys_operate_log_p202611 USING btree (id);


--
-- Name: t_sys_operate_log_p202611_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202611_success_create_time_id_idx ON public.t_sys_operate_log_p202611 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202611_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202611_trace_id_idx ON public.t_sys_operate_log_p202611 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202611_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202611_user_id_create_time_idx ON public.t_sys_operate_log_p202611 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202612_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202612_id_idx ON public.t_sys_operate_log_p202612 USING btree (id);


--
-- Name: t_sys_operate_log_p202612_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202612_success_create_time_id_idx ON public.t_sys_operate_log_p202612 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202612_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202612_trace_id_idx ON public.t_sys_operate_log_p202612 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202612_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202612_user_id_create_time_idx ON public.t_sys_operate_log_p202612 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202701_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202701_id_idx ON public.t_sys_operate_log_p202701 USING btree (id);


--
-- Name: t_sys_operate_log_p202701_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202701_success_create_time_id_idx ON public.t_sys_operate_log_p202701 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202701_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202701_trace_id_idx ON public.t_sys_operate_log_p202701 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202701_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202701_user_id_create_time_idx ON public.t_sys_operate_log_p202701 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202702_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202702_id_idx ON public.t_sys_operate_log_p202702 USING btree (id);


--
-- Name: t_sys_operate_log_p202702_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202702_success_create_time_id_idx ON public.t_sys_operate_log_p202702 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202702_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202702_trace_id_idx ON public.t_sys_operate_log_p202702 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202702_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202702_user_id_create_time_idx ON public.t_sys_operate_log_p202702 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202703_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202703_id_idx ON public.t_sys_operate_log_p202703 USING btree (id);


--
-- Name: t_sys_operate_log_p202703_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202703_success_create_time_id_idx ON public.t_sys_operate_log_p202703 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202703_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202703_trace_id_idx ON public.t_sys_operate_log_p202703 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202703_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202703_user_id_create_time_idx ON public.t_sys_operate_log_p202703 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202704_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202704_id_idx ON public.t_sys_operate_log_p202704 USING btree (id);


--
-- Name: t_sys_operate_log_p202704_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202704_success_create_time_id_idx ON public.t_sys_operate_log_p202704 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202704_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202704_trace_id_idx ON public.t_sys_operate_log_p202704 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202704_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202704_user_id_create_time_idx ON public.t_sys_operate_log_p202704 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202705_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202705_id_idx ON public.t_sys_operate_log_p202705 USING btree (id);


--
-- Name: t_sys_operate_log_p202705_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202705_success_create_time_id_idx ON public.t_sys_operate_log_p202705 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202705_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202705_trace_id_idx ON public.t_sys_operate_log_p202705 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202705_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202705_user_id_create_time_idx ON public.t_sys_operate_log_p202705 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202706_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202706_id_idx ON public.t_sys_operate_log_p202706 USING btree (id);


--
-- Name: t_sys_operate_log_p202706_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202706_success_create_time_id_idx ON public.t_sys_operate_log_p202706 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202706_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202706_trace_id_idx ON public.t_sys_operate_log_p202706 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202706_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202706_user_id_create_time_idx ON public.t_sys_operate_log_p202706 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202707_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202707_id_idx ON public.t_sys_operate_log_p202707 USING btree (id);


--
-- Name: t_sys_operate_log_p202707_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202707_success_create_time_id_idx ON public.t_sys_operate_log_p202707 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202707_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202707_trace_id_idx ON public.t_sys_operate_log_p202707 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202707_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202707_user_id_create_time_idx ON public.t_sys_operate_log_p202707 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202708_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202708_id_idx ON public.t_sys_operate_log_p202708 USING btree (id);


--
-- Name: t_sys_operate_log_p202708_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202708_success_create_time_id_idx ON public.t_sys_operate_log_p202708 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202708_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202708_trace_id_idx ON public.t_sys_operate_log_p202708 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202708_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202708_user_id_create_time_idx ON public.t_sys_operate_log_p202708 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202709_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202709_id_idx ON public.t_sys_operate_log_p202709 USING btree (id);


--
-- Name: t_sys_operate_log_p202709_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202709_success_create_time_id_idx ON public.t_sys_operate_log_p202709 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202709_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202709_trace_id_idx ON public.t_sys_operate_log_p202709 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202709_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202709_user_id_create_time_idx ON public.t_sys_operate_log_p202709 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202710_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202710_id_idx ON public.t_sys_operate_log_p202710 USING btree (id);


--
-- Name: t_sys_operate_log_p202710_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202710_success_create_time_id_idx ON public.t_sys_operate_log_p202710 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202710_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202710_trace_id_idx ON public.t_sys_operate_log_p202710 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202710_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202710_user_id_create_time_idx ON public.t_sys_operate_log_p202710 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202711_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202711_id_idx ON public.t_sys_operate_log_p202711 USING btree (id);


--
-- Name: t_sys_operate_log_p202711_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202711_success_create_time_id_idx ON public.t_sys_operate_log_p202711 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202711_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202711_trace_id_idx ON public.t_sys_operate_log_p202711 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202711_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202711_user_id_create_time_idx ON public.t_sys_operate_log_p202711 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202712_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202712_id_idx ON public.t_sys_operate_log_p202712 USING btree (id);


--
-- Name: t_sys_operate_log_p202712_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202712_success_create_time_id_idx ON public.t_sys_operate_log_p202712 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202712_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202712_trace_id_idx ON public.t_sys_operate_log_p202712 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202712_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202712_user_id_create_time_idx ON public.t_sys_operate_log_p202712 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202801_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202801_id_idx ON public.t_sys_operate_log_p202801 USING btree (id);


--
-- Name: t_sys_operate_log_p202801_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202801_success_create_time_id_idx ON public.t_sys_operate_log_p202801 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202801_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202801_trace_id_idx ON public.t_sys_operate_log_p202801 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202801_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202801_user_id_create_time_idx ON public.t_sys_operate_log_p202801 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202802_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202802_id_idx ON public.t_sys_operate_log_p202802 USING btree (id);


--
-- Name: t_sys_operate_log_p202802_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202802_success_create_time_id_idx ON public.t_sys_operate_log_p202802 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202802_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202802_trace_id_idx ON public.t_sys_operate_log_p202802 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202802_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202802_user_id_create_time_idx ON public.t_sys_operate_log_p202802 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202803_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202803_id_idx ON public.t_sys_operate_log_p202803 USING btree (id);


--
-- Name: t_sys_operate_log_p202803_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202803_success_create_time_id_idx ON public.t_sys_operate_log_p202803 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202803_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202803_trace_id_idx ON public.t_sys_operate_log_p202803 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202803_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202803_user_id_create_time_idx ON public.t_sys_operate_log_p202803 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202804_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202804_id_idx ON public.t_sys_operate_log_p202804 USING btree (id);


--
-- Name: t_sys_operate_log_p202804_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202804_success_create_time_id_idx ON public.t_sys_operate_log_p202804 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202804_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202804_trace_id_idx ON public.t_sys_operate_log_p202804 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202804_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202804_user_id_create_time_idx ON public.t_sys_operate_log_p202804 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202805_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202805_id_idx ON public.t_sys_operate_log_p202805 USING btree (id);


--
-- Name: t_sys_operate_log_p202805_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202805_success_create_time_id_idx ON public.t_sys_operate_log_p202805 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202805_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202805_trace_id_idx ON public.t_sys_operate_log_p202805 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202805_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202805_user_id_create_time_idx ON public.t_sys_operate_log_p202805 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202806_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202806_id_idx ON public.t_sys_operate_log_p202806 USING btree (id);


--
-- Name: t_sys_operate_log_p202806_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202806_success_create_time_id_idx ON public.t_sys_operate_log_p202806 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202806_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202806_trace_id_idx ON public.t_sys_operate_log_p202806 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202806_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202806_user_id_create_time_idx ON public.t_sys_operate_log_p202806 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202807_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202807_id_idx ON public.t_sys_operate_log_p202807 USING btree (id);


--
-- Name: t_sys_operate_log_p202807_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202807_success_create_time_id_idx ON public.t_sys_operate_log_p202807 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202807_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202807_trace_id_idx ON public.t_sys_operate_log_p202807 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202807_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202807_user_id_create_time_idx ON public.t_sys_operate_log_p202807 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202808_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202808_id_idx ON public.t_sys_operate_log_p202808 USING btree (id);


--
-- Name: t_sys_operate_log_p202808_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202808_success_create_time_id_idx ON public.t_sys_operate_log_p202808 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202808_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202808_trace_id_idx ON public.t_sys_operate_log_p202808 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202808_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202808_user_id_create_time_idx ON public.t_sys_operate_log_p202808 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202809_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202809_id_idx ON public.t_sys_operate_log_p202809 USING btree (id);


--
-- Name: t_sys_operate_log_p202809_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202809_success_create_time_id_idx ON public.t_sys_operate_log_p202809 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202809_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202809_trace_id_idx ON public.t_sys_operate_log_p202809 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202809_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202809_user_id_create_time_idx ON public.t_sys_operate_log_p202809 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202810_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202810_id_idx ON public.t_sys_operate_log_p202810 USING btree (id);


--
-- Name: t_sys_operate_log_p202810_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202810_success_create_time_id_idx ON public.t_sys_operate_log_p202810 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202810_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202810_trace_id_idx ON public.t_sys_operate_log_p202810 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202810_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202810_user_id_create_time_idx ON public.t_sys_operate_log_p202810 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202811_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202811_id_idx ON public.t_sys_operate_log_p202811 USING btree (id);


--
-- Name: t_sys_operate_log_p202811_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202811_success_create_time_id_idx ON public.t_sys_operate_log_p202811 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202811_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202811_trace_id_idx ON public.t_sys_operate_log_p202811 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202811_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202811_user_id_create_time_idx ON public.t_sys_operate_log_p202811 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202812_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202812_id_idx ON public.t_sys_operate_log_p202812 USING btree (id);


--
-- Name: t_sys_operate_log_p202812_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202812_success_create_time_id_idx ON public.t_sys_operate_log_p202812 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202812_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202812_trace_id_idx ON public.t_sys_operate_log_p202812 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202812_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202812_user_id_create_time_idx ON public.t_sys_operate_log_p202812 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202901_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202901_id_idx ON public.t_sys_operate_log_p202901 USING btree (id);


--
-- Name: t_sys_operate_log_p202901_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202901_success_create_time_id_idx ON public.t_sys_operate_log_p202901 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202901_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202901_trace_id_idx ON public.t_sys_operate_log_p202901 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202901_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202901_user_id_create_time_idx ON public.t_sys_operate_log_p202901 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202902_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202902_id_idx ON public.t_sys_operate_log_p202902 USING btree (id);


--
-- Name: t_sys_operate_log_p202902_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202902_success_create_time_id_idx ON public.t_sys_operate_log_p202902 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202902_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202902_trace_id_idx ON public.t_sys_operate_log_p202902 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202902_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202902_user_id_create_time_idx ON public.t_sys_operate_log_p202902 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202903_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202903_id_idx ON public.t_sys_operate_log_p202903 USING btree (id);


--
-- Name: t_sys_operate_log_p202903_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202903_success_create_time_id_idx ON public.t_sys_operate_log_p202903 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202903_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202903_trace_id_idx ON public.t_sys_operate_log_p202903 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202903_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202903_user_id_create_time_idx ON public.t_sys_operate_log_p202903 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202904_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202904_id_idx ON public.t_sys_operate_log_p202904 USING btree (id);


--
-- Name: t_sys_operate_log_p202904_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202904_success_create_time_id_idx ON public.t_sys_operate_log_p202904 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202904_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202904_trace_id_idx ON public.t_sys_operate_log_p202904 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202904_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202904_user_id_create_time_idx ON public.t_sys_operate_log_p202904 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202905_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202905_id_idx ON public.t_sys_operate_log_p202905 USING btree (id);


--
-- Name: t_sys_operate_log_p202905_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202905_success_create_time_id_idx ON public.t_sys_operate_log_p202905 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202905_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202905_trace_id_idx ON public.t_sys_operate_log_p202905 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202905_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202905_user_id_create_time_idx ON public.t_sys_operate_log_p202905 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202906_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202906_id_idx ON public.t_sys_operate_log_p202906 USING btree (id);


--
-- Name: t_sys_operate_log_p202906_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202906_success_create_time_id_idx ON public.t_sys_operate_log_p202906 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202906_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202906_trace_id_idx ON public.t_sys_operate_log_p202906 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202906_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202906_user_id_create_time_idx ON public.t_sys_operate_log_p202906 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202907_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202907_id_idx ON public.t_sys_operate_log_p202907 USING btree (id);


--
-- Name: t_sys_operate_log_p202907_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202907_success_create_time_id_idx ON public.t_sys_operate_log_p202907 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202907_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202907_trace_id_idx ON public.t_sys_operate_log_p202907 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202907_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202907_user_id_create_time_idx ON public.t_sys_operate_log_p202907 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202908_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202908_id_idx ON public.t_sys_operate_log_p202908 USING btree (id);


--
-- Name: t_sys_operate_log_p202908_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202908_success_create_time_id_idx ON public.t_sys_operate_log_p202908 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202908_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202908_trace_id_idx ON public.t_sys_operate_log_p202908 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202908_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202908_user_id_create_time_idx ON public.t_sys_operate_log_p202908 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202909_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202909_id_idx ON public.t_sys_operate_log_p202909 USING btree (id);


--
-- Name: t_sys_operate_log_p202909_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202909_success_create_time_id_idx ON public.t_sys_operate_log_p202909 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202909_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202909_trace_id_idx ON public.t_sys_operate_log_p202909 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202909_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202909_user_id_create_time_idx ON public.t_sys_operate_log_p202909 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202910_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202910_id_idx ON public.t_sys_operate_log_p202910 USING btree (id);


--
-- Name: t_sys_operate_log_p202910_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202910_success_create_time_id_idx ON public.t_sys_operate_log_p202910 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202910_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202910_trace_id_idx ON public.t_sys_operate_log_p202910 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202910_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202910_user_id_create_time_idx ON public.t_sys_operate_log_p202910 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202911_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202911_id_idx ON public.t_sys_operate_log_p202911 USING btree (id);


--
-- Name: t_sys_operate_log_p202911_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202911_success_create_time_id_idx ON public.t_sys_operate_log_p202911 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202911_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202911_trace_id_idx ON public.t_sys_operate_log_p202911 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202911_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202911_user_id_create_time_idx ON public.t_sys_operate_log_p202911 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_operate_log_p202912_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202912_id_idx ON public.t_sys_operate_log_p202912 USING btree (id);


--
-- Name: t_sys_operate_log_p202912_success_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202912_success_create_time_id_idx ON public.t_sys_operate_log_p202912 USING btree (success, create_time DESC, id DESC);


--
-- Name: t_sys_operate_log_p202912_trace_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202912_trace_id_idx ON public.t_sys_operate_log_p202912 USING btree (trace_id);


--
-- Name: t_sys_operate_log_p202912_user_id_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_operate_log_p202912_user_id_create_time_idx ON public.t_sys_operate_log_p202912 USING btree (user_id, create_time DESC);


--
-- Name: t_sys_script_log_default_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_default_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_default USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_default_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_default_id_idx ON public.t_sys_script_log_default USING btree (id DESC);


--
-- Name: t_sys_script_log_p202601_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202601_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202601 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202601_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202601_id_idx ON public.t_sys_script_log_p202601 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202602_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202602_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202602 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202602_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202602_id_idx ON public.t_sys_script_log_p202602 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202603_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202603_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202603 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202603_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202603_id_idx ON public.t_sys_script_log_p202603 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202604_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202604_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202604 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202604_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202604_id_idx ON public.t_sys_script_log_p202604 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202605_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202605_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202605 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202605_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202605_id_idx ON public.t_sys_script_log_p202605 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202606_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202606_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202606 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202606_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202606_id_idx ON public.t_sys_script_log_p202606 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202607_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202607_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202607 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202607_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202607_id_idx ON public.t_sys_script_log_p202607 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202608_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202608_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202608 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202608_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202608_id_idx ON public.t_sys_script_log_p202608 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202609_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202609_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202609 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202609_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202609_id_idx ON public.t_sys_script_log_p202609 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202610_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202610_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202610 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202610_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202610_id_idx ON public.t_sys_script_log_p202610 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202611_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202611_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202611 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202611_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202611_id_idx ON public.t_sys_script_log_p202611 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202612_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202612_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202612 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202612_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202612_id_idx ON public.t_sys_script_log_p202612 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202701_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202701_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202701 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202701_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202701_id_idx ON public.t_sys_script_log_p202701 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202702_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202702_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202702 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202702_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202702_id_idx ON public.t_sys_script_log_p202702 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202703_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202703_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202703 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202703_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202703_id_idx ON public.t_sys_script_log_p202703 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202704_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202704_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202704 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202704_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202704_id_idx ON public.t_sys_script_log_p202704 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202705_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202705_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202705 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202705_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202705_id_idx ON public.t_sys_script_log_p202705 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202706_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202706_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202706 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202706_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202706_id_idx ON public.t_sys_script_log_p202706 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202707_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202707_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202707 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202707_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202707_id_idx ON public.t_sys_script_log_p202707 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202708_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202708_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202708 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202708_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202708_id_idx ON public.t_sys_script_log_p202708 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202709_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202709_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202709 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202709_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202709_id_idx ON public.t_sys_script_log_p202709 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202710_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202710_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202710 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202710_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202710_id_idx ON public.t_sys_script_log_p202710 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202711_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202711_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202711 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202711_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202711_id_idx ON public.t_sys_script_log_p202711 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202712_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202712_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202712 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202712_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202712_id_idx ON public.t_sys_script_log_p202712 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202801_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202801_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202801 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202801_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202801_id_idx ON public.t_sys_script_log_p202801 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202802_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202802_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202802 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202802_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202802_id_idx ON public.t_sys_script_log_p202802 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202803_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202803_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202803 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202803_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202803_id_idx ON public.t_sys_script_log_p202803 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202804_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202804_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202804 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202804_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202804_id_idx ON public.t_sys_script_log_p202804 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202805_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202805_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202805 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202805_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202805_id_idx ON public.t_sys_script_log_p202805 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202806_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202806_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202806 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202806_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202806_id_idx ON public.t_sys_script_log_p202806 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202807_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202807_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202807 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202807_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202807_id_idx ON public.t_sys_script_log_p202807 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202808_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202808_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202808 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202808_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202808_id_idx ON public.t_sys_script_log_p202808 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202809_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202809_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202809 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202809_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202809_id_idx ON public.t_sys_script_log_p202809 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202810_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202810_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202810 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202810_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202810_id_idx ON public.t_sys_script_log_p202810 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202811_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202811_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202811 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202811_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202811_id_idx ON public.t_sys_script_log_p202811 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202812_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202812_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202812 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202812_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202812_id_idx ON public.t_sys_script_log_p202812 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202901_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202901_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202901 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202901_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202901_id_idx ON public.t_sys_script_log_p202901 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202902_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202902_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202902 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202902_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202902_id_idx ON public.t_sys_script_log_p202902 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202903_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202903_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202903 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202903_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202903_id_idx ON public.t_sys_script_log_p202903 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202904_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202904_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202904 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202904_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202904_id_idx ON public.t_sys_script_log_p202904 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202905_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202905_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202905 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202905_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202905_id_idx ON public.t_sys_script_log_p202905 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202906_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202906_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202906 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202906_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202906_id_idx ON public.t_sys_script_log_p202906 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202907_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202907_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202907 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202907_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202907_id_idx ON public.t_sys_script_log_p202907 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202908_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202908_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202908 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202908_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202908_id_idx ON public.t_sys_script_log_p202908 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202909_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202909_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202909 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202909_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202909_id_idx ON public.t_sys_script_log_p202909 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202910_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202910_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202910 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202910_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202910_id_idx ON public.t_sys_script_log_p202910 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202911_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202911_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202911 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202911_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202911_id_idx ON public.t_sys_script_log_p202911 USING btree (id DESC);


--
-- Name: t_sys_script_log_p202912_execute_status_transaction_mode_cr_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202912_execute_status_transaction_mode_cr_idx ON public.t_sys_script_log_p202912 USING btree (execute_status, transaction_mode, create_time DESC, id DESC);


--
-- Name: t_sys_script_log_p202912_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_script_log_p202912_id_idx ON public.t_sys_script_log_p202912 USING btree (id DESC);


--
-- Name: t_sys_sql_log_default_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_default_id_idx ON public.t_sys_sql_log_default USING btree (id DESC);


--
-- Name: t_sys_sql_log_default_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_default_result_type_create_time_id_idx ON public.t_sys_sql_log_default USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202601_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202601_id_idx ON public.t_sys_sql_log_p202601 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202601_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202601_result_type_create_time_id_idx ON public.t_sys_sql_log_p202601 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202602_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202602_id_idx ON public.t_sys_sql_log_p202602 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202602_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202602_result_type_create_time_id_idx ON public.t_sys_sql_log_p202602 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202603_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202603_id_idx ON public.t_sys_sql_log_p202603 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202603_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202603_result_type_create_time_id_idx ON public.t_sys_sql_log_p202603 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202604_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202604_id_idx ON public.t_sys_sql_log_p202604 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202604_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202604_result_type_create_time_id_idx ON public.t_sys_sql_log_p202604 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202605_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202605_id_idx ON public.t_sys_sql_log_p202605 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202605_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202605_result_type_create_time_id_idx ON public.t_sys_sql_log_p202605 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202606_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202606_id_idx ON public.t_sys_sql_log_p202606 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202606_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202606_result_type_create_time_id_idx ON public.t_sys_sql_log_p202606 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202607_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202607_id_idx ON public.t_sys_sql_log_p202607 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202607_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202607_result_type_create_time_id_idx ON public.t_sys_sql_log_p202607 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202608_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202608_id_idx ON public.t_sys_sql_log_p202608 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202608_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202608_result_type_create_time_id_idx ON public.t_sys_sql_log_p202608 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202609_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202609_id_idx ON public.t_sys_sql_log_p202609 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202609_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202609_result_type_create_time_id_idx ON public.t_sys_sql_log_p202609 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202610_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202610_id_idx ON public.t_sys_sql_log_p202610 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202610_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202610_result_type_create_time_id_idx ON public.t_sys_sql_log_p202610 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202611_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202611_id_idx ON public.t_sys_sql_log_p202611 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202611_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202611_result_type_create_time_id_idx ON public.t_sys_sql_log_p202611 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202612_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202612_id_idx ON public.t_sys_sql_log_p202612 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202612_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202612_result_type_create_time_id_idx ON public.t_sys_sql_log_p202612 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202701_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202701_id_idx ON public.t_sys_sql_log_p202701 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202701_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202701_result_type_create_time_id_idx ON public.t_sys_sql_log_p202701 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202702_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202702_id_idx ON public.t_sys_sql_log_p202702 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202702_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202702_result_type_create_time_id_idx ON public.t_sys_sql_log_p202702 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202703_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202703_id_idx ON public.t_sys_sql_log_p202703 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202703_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202703_result_type_create_time_id_idx ON public.t_sys_sql_log_p202703 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202704_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202704_id_idx ON public.t_sys_sql_log_p202704 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202704_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202704_result_type_create_time_id_idx ON public.t_sys_sql_log_p202704 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202705_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202705_id_idx ON public.t_sys_sql_log_p202705 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202705_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202705_result_type_create_time_id_idx ON public.t_sys_sql_log_p202705 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202706_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202706_id_idx ON public.t_sys_sql_log_p202706 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202706_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202706_result_type_create_time_id_idx ON public.t_sys_sql_log_p202706 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202707_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202707_id_idx ON public.t_sys_sql_log_p202707 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202707_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202707_result_type_create_time_id_idx ON public.t_sys_sql_log_p202707 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202708_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202708_id_idx ON public.t_sys_sql_log_p202708 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202708_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202708_result_type_create_time_id_idx ON public.t_sys_sql_log_p202708 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202709_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202709_id_idx ON public.t_sys_sql_log_p202709 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202709_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202709_result_type_create_time_id_idx ON public.t_sys_sql_log_p202709 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202710_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202710_id_idx ON public.t_sys_sql_log_p202710 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202710_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202710_result_type_create_time_id_idx ON public.t_sys_sql_log_p202710 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202711_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202711_id_idx ON public.t_sys_sql_log_p202711 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202711_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202711_result_type_create_time_id_idx ON public.t_sys_sql_log_p202711 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202712_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202712_id_idx ON public.t_sys_sql_log_p202712 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202712_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202712_result_type_create_time_id_idx ON public.t_sys_sql_log_p202712 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202801_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202801_id_idx ON public.t_sys_sql_log_p202801 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202801_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202801_result_type_create_time_id_idx ON public.t_sys_sql_log_p202801 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202802_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202802_id_idx ON public.t_sys_sql_log_p202802 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202802_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202802_result_type_create_time_id_idx ON public.t_sys_sql_log_p202802 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202803_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202803_id_idx ON public.t_sys_sql_log_p202803 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202803_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202803_result_type_create_time_id_idx ON public.t_sys_sql_log_p202803 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202804_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202804_id_idx ON public.t_sys_sql_log_p202804 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202804_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202804_result_type_create_time_id_idx ON public.t_sys_sql_log_p202804 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202805_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202805_id_idx ON public.t_sys_sql_log_p202805 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202805_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202805_result_type_create_time_id_idx ON public.t_sys_sql_log_p202805 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202806_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202806_id_idx ON public.t_sys_sql_log_p202806 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202806_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202806_result_type_create_time_id_idx ON public.t_sys_sql_log_p202806 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202807_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202807_id_idx ON public.t_sys_sql_log_p202807 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202807_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202807_result_type_create_time_id_idx ON public.t_sys_sql_log_p202807 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202808_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202808_id_idx ON public.t_sys_sql_log_p202808 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202808_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202808_result_type_create_time_id_idx ON public.t_sys_sql_log_p202808 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202809_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202809_id_idx ON public.t_sys_sql_log_p202809 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202809_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202809_result_type_create_time_id_idx ON public.t_sys_sql_log_p202809 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202810_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202810_id_idx ON public.t_sys_sql_log_p202810 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202810_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202810_result_type_create_time_id_idx ON public.t_sys_sql_log_p202810 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202811_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202811_id_idx ON public.t_sys_sql_log_p202811 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202811_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202811_result_type_create_time_id_idx ON public.t_sys_sql_log_p202811 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202812_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202812_id_idx ON public.t_sys_sql_log_p202812 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202812_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202812_result_type_create_time_id_idx ON public.t_sys_sql_log_p202812 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202901_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202901_id_idx ON public.t_sys_sql_log_p202901 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202901_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202901_result_type_create_time_id_idx ON public.t_sys_sql_log_p202901 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202902_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202902_id_idx ON public.t_sys_sql_log_p202902 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202902_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202902_result_type_create_time_id_idx ON public.t_sys_sql_log_p202902 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202903_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202903_id_idx ON public.t_sys_sql_log_p202903 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202903_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202903_result_type_create_time_id_idx ON public.t_sys_sql_log_p202903 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202904_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202904_id_idx ON public.t_sys_sql_log_p202904 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202904_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202904_result_type_create_time_id_idx ON public.t_sys_sql_log_p202904 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202905_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202905_id_idx ON public.t_sys_sql_log_p202905 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202905_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202905_result_type_create_time_id_idx ON public.t_sys_sql_log_p202905 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202906_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202906_id_idx ON public.t_sys_sql_log_p202906 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202906_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202906_result_type_create_time_id_idx ON public.t_sys_sql_log_p202906 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202907_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202907_id_idx ON public.t_sys_sql_log_p202907 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202907_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202907_result_type_create_time_id_idx ON public.t_sys_sql_log_p202907 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202908_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202908_id_idx ON public.t_sys_sql_log_p202908 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202908_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202908_result_type_create_time_id_idx ON public.t_sys_sql_log_p202908 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202909_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202909_id_idx ON public.t_sys_sql_log_p202909 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202909_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202909_result_type_create_time_id_idx ON public.t_sys_sql_log_p202909 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202910_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202910_id_idx ON public.t_sys_sql_log_p202910 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202910_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202910_result_type_create_time_id_idx ON public.t_sys_sql_log_p202910 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202911_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202911_id_idx ON public.t_sys_sql_log_p202911 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202911_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202911_result_type_create_time_id_idx ON public.t_sys_sql_log_p202911 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: t_sys_sql_log_p202912_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202912_id_idx ON public.t_sys_sql_log_p202912 USING btree (id DESC);


--
-- Name: t_sys_sql_log_p202912_result_type_create_time_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX t_sys_sql_log_p202912_result_type_create_time_id_idx ON public.t_sys_sql_log_p202912 USING btree (result_type, create_time DESC, id DESC);


--
-- Name: uk_basic_data_item_category_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_basic_data_item_category_number ON public.t_sys_basic_data_item USING btree (category_id, number);


--
-- Name: uk_sys_attachment_object_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_attachment_object_key ON public.t_sys_attachment USING btree (object_key);


--
-- Name: uk_sys_file_config_singleton; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_file_config_singleton ON public.t_sys_file_config USING btree ((true));


--
-- Name: uk_sys_param_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_param_number ON public.t_sys_param USING btree (number);


--
-- Name: uk_sys_ui_config_singleton; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_ui_config_singleton ON public.t_sys_ui_config USING btree ((true));


--
-- Name: t_sys_job_log_default_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_default_fire_instance_id_idx;


--
-- Name: t_sys_job_log_default_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_default_id_idx;


--
-- Name: t_sys_job_log_default_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_default_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_default_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_default_pkey;


--
-- Name: t_sys_job_log_default_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_default_status_start_time_id_idx;


--
-- Name: t_sys_job_log_default_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_default_trace_id_idx;


--
-- Name: t_sys_job_log_p202601_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202601_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202601_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202601_id_idx;


--
-- Name: t_sys_job_log_p202601_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202601_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202601_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202601_pkey;


--
-- Name: t_sys_job_log_p202601_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202601_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202601_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202601_trace_id_idx;


--
-- Name: t_sys_job_log_p202602_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202602_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202602_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202602_id_idx;


--
-- Name: t_sys_job_log_p202602_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202602_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202602_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202602_pkey;


--
-- Name: t_sys_job_log_p202602_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202602_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202602_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202602_trace_id_idx;


--
-- Name: t_sys_job_log_p202603_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202603_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202603_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202603_id_idx;


--
-- Name: t_sys_job_log_p202603_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202603_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202603_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202603_pkey;


--
-- Name: t_sys_job_log_p202603_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202603_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202603_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202603_trace_id_idx;


--
-- Name: t_sys_job_log_p202604_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202604_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202604_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202604_id_idx;


--
-- Name: t_sys_job_log_p202604_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202604_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202604_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202604_pkey;


--
-- Name: t_sys_job_log_p202604_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202604_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202604_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202604_trace_id_idx;


--
-- Name: t_sys_job_log_p202605_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202605_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202605_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202605_id_idx;


--
-- Name: t_sys_job_log_p202605_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202605_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202605_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202605_pkey;


--
-- Name: t_sys_job_log_p202605_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202605_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202605_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202605_trace_id_idx;


--
-- Name: t_sys_job_log_p202606_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202606_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202606_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202606_id_idx;


--
-- Name: t_sys_job_log_p202606_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202606_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202606_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202606_pkey;


--
-- Name: t_sys_job_log_p202606_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202606_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202606_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202606_trace_id_idx;


--
-- Name: t_sys_job_log_p202607_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202607_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202607_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202607_id_idx;


--
-- Name: t_sys_job_log_p202607_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202607_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202607_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202607_pkey;


--
-- Name: t_sys_job_log_p202607_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202607_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202607_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202607_trace_id_idx;


--
-- Name: t_sys_job_log_p202608_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202608_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202608_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202608_id_idx;


--
-- Name: t_sys_job_log_p202608_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202608_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202608_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202608_pkey;


--
-- Name: t_sys_job_log_p202608_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202608_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202608_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202608_trace_id_idx;


--
-- Name: t_sys_job_log_p202609_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202609_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202609_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202609_id_idx;


--
-- Name: t_sys_job_log_p202609_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202609_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202609_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202609_pkey;


--
-- Name: t_sys_job_log_p202609_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202609_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202609_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202609_trace_id_idx;


--
-- Name: t_sys_job_log_p202610_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202610_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202610_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202610_id_idx;


--
-- Name: t_sys_job_log_p202610_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202610_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202610_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202610_pkey;


--
-- Name: t_sys_job_log_p202610_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202610_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202610_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202610_trace_id_idx;


--
-- Name: t_sys_job_log_p202611_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202611_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202611_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202611_id_idx;


--
-- Name: t_sys_job_log_p202611_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202611_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202611_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202611_pkey;


--
-- Name: t_sys_job_log_p202611_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202611_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202611_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202611_trace_id_idx;


--
-- Name: t_sys_job_log_p202612_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202612_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202612_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202612_id_idx;


--
-- Name: t_sys_job_log_p202612_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202612_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202612_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202612_pkey;


--
-- Name: t_sys_job_log_p202612_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202612_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202612_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202612_trace_id_idx;


--
-- Name: t_sys_job_log_p202701_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202701_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202701_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202701_id_idx;


--
-- Name: t_sys_job_log_p202701_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202701_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202701_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202701_pkey;


--
-- Name: t_sys_job_log_p202701_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202701_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202701_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202701_trace_id_idx;


--
-- Name: t_sys_job_log_p202702_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202702_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202702_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202702_id_idx;


--
-- Name: t_sys_job_log_p202702_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202702_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202702_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202702_pkey;


--
-- Name: t_sys_job_log_p202702_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202702_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202702_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202702_trace_id_idx;


--
-- Name: t_sys_job_log_p202703_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202703_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202703_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202703_id_idx;


--
-- Name: t_sys_job_log_p202703_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202703_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202703_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202703_pkey;


--
-- Name: t_sys_job_log_p202703_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202703_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202703_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202703_trace_id_idx;


--
-- Name: t_sys_job_log_p202704_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202704_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202704_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202704_id_idx;


--
-- Name: t_sys_job_log_p202704_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202704_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202704_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202704_pkey;


--
-- Name: t_sys_job_log_p202704_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202704_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202704_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202704_trace_id_idx;


--
-- Name: t_sys_job_log_p202705_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202705_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202705_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202705_id_idx;


--
-- Name: t_sys_job_log_p202705_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202705_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202705_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202705_pkey;


--
-- Name: t_sys_job_log_p202705_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202705_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202705_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202705_trace_id_idx;


--
-- Name: t_sys_job_log_p202706_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202706_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202706_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202706_id_idx;


--
-- Name: t_sys_job_log_p202706_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202706_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202706_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202706_pkey;


--
-- Name: t_sys_job_log_p202706_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202706_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202706_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202706_trace_id_idx;


--
-- Name: t_sys_job_log_p202707_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202707_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202707_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202707_id_idx;


--
-- Name: t_sys_job_log_p202707_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202707_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202707_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202707_pkey;


--
-- Name: t_sys_job_log_p202707_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202707_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202707_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202707_trace_id_idx;


--
-- Name: t_sys_job_log_p202708_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202708_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202708_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202708_id_idx;


--
-- Name: t_sys_job_log_p202708_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202708_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202708_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202708_pkey;


--
-- Name: t_sys_job_log_p202708_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202708_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202708_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202708_trace_id_idx;


--
-- Name: t_sys_job_log_p202709_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202709_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202709_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202709_id_idx;


--
-- Name: t_sys_job_log_p202709_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202709_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202709_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202709_pkey;


--
-- Name: t_sys_job_log_p202709_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202709_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202709_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202709_trace_id_idx;


--
-- Name: t_sys_job_log_p202710_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202710_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202710_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202710_id_idx;


--
-- Name: t_sys_job_log_p202710_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202710_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202710_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202710_pkey;


--
-- Name: t_sys_job_log_p202710_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202710_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202710_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202710_trace_id_idx;


--
-- Name: t_sys_job_log_p202711_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202711_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202711_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202711_id_idx;


--
-- Name: t_sys_job_log_p202711_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202711_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202711_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202711_pkey;


--
-- Name: t_sys_job_log_p202711_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202711_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202711_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202711_trace_id_idx;


--
-- Name: t_sys_job_log_p202712_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202712_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202712_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202712_id_idx;


--
-- Name: t_sys_job_log_p202712_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202712_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202712_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202712_pkey;


--
-- Name: t_sys_job_log_p202712_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202712_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202712_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202712_trace_id_idx;


--
-- Name: t_sys_job_log_p202801_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202801_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202801_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202801_id_idx;


--
-- Name: t_sys_job_log_p202801_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202801_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202801_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202801_pkey;


--
-- Name: t_sys_job_log_p202801_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202801_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202801_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202801_trace_id_idx;


--
-- Name: t_sys_job_log_p202802_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202802_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202802_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202802_id_idx;


--
-- Name: t_sys_job_log_p202802_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202802_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202802_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202802_pkey;


--
-- Name: t_sys_job_log_p202802_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202802_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202802_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202802_trace_id_idx;


--
-- Name: t_sys_job_log_p202803_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202803_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202803_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202803_id_idx;


--
-- Name: t_sys_job_log_p202803_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202803_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202803_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202803_pkey;


--
-- Name: t_sys_job_log_p202803_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202803_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202803_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202803_trace_id_idx;


--
-- Name: t_sys_job_log_p202804_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202804_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202804_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202804_id_idx;


--
-- Name: t_sys_job_log_p202804_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202804_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202804_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202804_pkey;


--
-- Name: t_sys_job_log_p202804_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202804_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202804_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202804_trace_id_idx;


--
-- Name: t_sys_job_log_p202805_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202805_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202805_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202805_id_idx;


--
-- Name: t_sys_job_log_p202805_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202805_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202805_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202805_pkey;


--
-- Name: t_sys_job_log_p202805_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202805_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202805_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202805_trace_id_idx;


--
-- Name: t_sys_job_log_p202806_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202806_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202806_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202806_id_idx;


--
-- Name: t_sys_job_log_p202806_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202806_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202806_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202806_pkey;


--
-- Name: t_sys_job_log_p202806_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202806_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202806_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202806_trace_id_idx;


--
-- Name: t_sys_job_log_p202807_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202807_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202807_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202807_id_idx;


--
-- Name: t_sys_job_log_p202807_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202807_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202807_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202807_pkey;


--
-- Name: t_sys_job_log_p202807_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202807_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202807_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202807_trace_id_idx;


--
-- Name: t_sys_job_log_p202808_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202808_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202808_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202808_id_idx;


--
-- Name: t_sys_job_log_p202808_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202808_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202808_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202808_pkey;


--
-- Name: t_sys_job_log_p202808_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202808_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202808_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202808_trace_id_idx;


--
-- Name: t_sys_job_log_p202809_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202809_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202809_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202809_id_idx;


--
-- Name: t_sys_job_log_p202809_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202809_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202809_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202809_pkey;


--
-- Name: t_sys_job_log_p202809_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202809_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202809_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202809_trace_id_idx;


--
-- Name: t_sys_job_log_p202810_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202810_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202810_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202810_id_idx;


--
-- Name: t_sys_job_log_p202810_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202810_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202810_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202810_pkey;


--
-- Name: t_sys_job_log_p202810_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202810_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202810_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202810_trace_id_idx;


--
-- Name: t_sys_job_log_p202811_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202811_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202811_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202811_id_idx;


--
-- Name: t_sys_job_log_p202811_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202811_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202811_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202811_pkey;


--
-- Name: t_sys_job_log_p202811_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202811_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202811_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202811_trace_id_idx;


--
-- Name: t_sys_job_log_p202812_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202812_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202812_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202812_id_idx;


--
-- Name: t_sys_job_log_p202812_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202812_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202812_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202812_pkey;


--
-- Name: t_sys_job_log_p202812_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202812_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202812_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202812_trace_id_idx;


--
-- Name: t_sys_job_log_p202901_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202901_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202901_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202901_id_idx;


--
-- Name: t_sys_job_log_p202901_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202901_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202901_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202901_pkey;


--
-- Name: t_sys_job_log_p202901_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202901_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202901_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202901_trace_id_idx;


--
-- Name: t_sys_job_log_p202902_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202902_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202902_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202902_id_idx;


--
-- Name: t_sys_job_log_p202902_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202902_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202902_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202902_pkey;


--
-- Name: t_sys_job_log_p202902_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202902_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202902_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202902_trace_id_idx;


--
-- Name: t_sys_job_log_p202903_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202903_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202903_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202903_id_idx;


--
-- Name: t_sys_job_log_p202903_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202903_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202903_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202903_pkey;


--
-- Name: t_sys_job_log_p202903_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202903_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202903_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202903_trace_id_idx;


--
-- Name: t_sys_job_log_p202904_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202904_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202904_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202904_id_idx;


--
-- Name: t_sys_job_log_p202904_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202904_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202904_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202904_pkey;


--
-- Name: t_sys_job_log_p202904_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202904_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202904_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202904_trace_id_idx;


--
-- Name: t_sys_job_log_p202905_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202905_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202905_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202905_id_idx;


--
-- Name: t_sys_job_log_p202905_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202905_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202905_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202905_pkey;


--
-- Name: t_sys_job_log_p202905_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202905_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202905_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202905_trace_id_idx;


--
-- Name: t_sys_job_log_p202906_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202906_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202906_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202906_id_idx;


--
-- Name: t_sys_job_log_p202906_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202906_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202906_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202906_pkey;


--
-- Name: t_sys_job_log_p202906_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202906_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202906_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202906_trace_id_idx;


--
-- Name: t_sys_job_log_p202907_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202907_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202907_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202907_id_idx;


--
-- Name: t_sys_job_log_p202907_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202907_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202907_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202907_pkey;


--
-- Name: t_sys_job_log_p202907_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202907_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202907_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202907_trace_id_idx;


--
-- Name: t_sys_job_log_p202908_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202908_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202908_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202908_id_idx;


--
-- Name: t_sys_job_log_p202908_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202908_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202908_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202908_pkey;


--
-- Name: t_sys_job_log_p202908_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202908_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202908_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202908_trace_id_idx;


--
-- Name: t_sys_job_log_p202909_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202909_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202909_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202909_id_idx;


--
-- Name: t_sys_job_log_p202909_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202909_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202909_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202909_pkey;


--
-- Name: t_sys_job_log_p202909_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202909_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202909_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202909_trace_id_idx;


--
-- Name: t_sys_job_log_p202910_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202910_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202910_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202910_id_idx;


--
-- Name: t_sys_job_log_p202910_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202910_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202910_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202910_pkey;


--
-- Name: t_sys_job_log_p202910_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202910_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202910_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202910_trace_id_idx;


--
-- Name: t_sys_job_log_p202911_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202911_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202911_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202911_id_idx;


--
-- Name: t_sys_job_log_p202911_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202911_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202911_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202911_pkey;


--
-- Name: t_sys_job_log_p202911_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202911_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202911_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202911_trace_id_idx;


--
-- Name: t_sys_job_log_p202912_fire_instance_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_fire_instance_id ATTACH PARTITION public.t_sys_job_log_p202912_fire_instance_id_idx;


--
-- Name: t_sys_job_log_p202912_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_id ATTACH PARTITION public.t_sys_job_log_p202912_id_idx;


--
-- Name: t_sys_job_log_p202912_job_id_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_job_start ATTACH PARTITION public.t_sys_job_log_p202912_job_id_start_time_id_idx;


--
-- Name: t_sys_job_log_p202912_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_job_log ATTACH PARTITION public.t_sys_job_log_p202912_pkey;


--
-- Name: t_sys_job_log_p202912_status_start_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_status_start ATTACH PARTITION public.t_sys_job_log_p202912_status_start_time_id_idx;


--
-- Name: t_sys_job_log_p202912_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_job_log_trace_id ATTACH PARTITION public.t_sys_job_log_p202912_trace_id_idx;


--
-- Name: t_sys_login_log_default_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_default_id_idx;


--
-- Name: t_sys_login_log_default_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_default_pkey;


--
-- Name: t_sys_login_log_default_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_default_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_default_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_default_trace_id_idx;


--
-- Name: t_sys_login_log_default_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_default_user_id_create_time_idx;


--
-- Name: t_sys_login_log_default_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_default_username_create_time_idx;


--
-- Name: t_sys_login_log_p202601_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202601_id_idx;


--
-- Name: t_sys_login_log_p202601_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202601_pkey;


--
-- Name: t_sys_login_log_p202601_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202601_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202601_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202601_trace_id_idx;


--
-- Name: t_sys_login_log_p202601_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202601_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202601_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202601_username_create_time_idx;


--
-- Name: t_sys_login_log_p202602_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202602_id_idx;


--
-- Name: t_sys_login_log_p202602_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202602_pkey;


--
-- Name: t_sys_login_log_p202602_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202602_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202602_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202602_trace_id_idx;


--
-- Name: t_sys_login_log_p202602_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202602_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202602_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202602_username_create_time_idx;


--
-- Name: t_sys_login_log_p202603_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202603_id_idx;


--
-- Name: t_sys_login_log_p202603_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202603_pkey;


--
-- Name: t_sys_login_log_p202603_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202603_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202603_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202603_trace_id_idx;


--
-- Name: t_sys_login_log_p202603_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202603_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202603_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202603_username_create_time_idx;


--
-- Name: t_sys_login_log_p202604_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202604_id_idx;


--
-- Name: t_sys_login_log_p202604_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202604_pkey;


--
-- Name: t_sys_login_log_p202604_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202604_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202604_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202604_trace_id_idx;


--
-- Name: t_sys_login_log_p202604_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202604_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202604_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202604_username_create_time_idx;


--
-- Name: t_sys_login_log_p202605_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202605_id_idx;


--
-- Name: t_sys_login_log_p202605_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202605_pkey;


--
-- Name: t_sys_login_log_p202605_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202605_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202605_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202605_trace_id_idx;


--
-- Name: t_sys_login_log_p202605_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202605_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202605_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202605_username_create_time_idx;


--
-- Name: t_sys_login_log_p202606_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202606_id_idx;


--
-- Name: t_sys_login_log_p202606_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202606_pkey;


--
-- Name: t_sys_login_log_p202606_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202606_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202606_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202606_trace_id_idx;


--
-- Name: t_sys_login_log_p202606_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202606_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202606_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202606_username_create_time_idx;


--
-- Name: t_sys_login_log_p202607_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202607_id_idx;


--
-- Name: t_sys_login_log_p202607_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202607_pkey;


--
-- Name: t_sys_login_log_p202607_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202607_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202607_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202607_trace_id_idx;


--
-- Name: t_sys_login_log_p202607_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202607_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202607_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202607_username_create_time_idx;


--
-- Name: t_sys_login_log_p202608_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202608_id_idx;


--
-- Name: t_sys_login_log_p202608_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202608_pkey;


--
-- Name: t_sys_login_log_p202608_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202608_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202608_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202608_trace_id_idx;


--
-- Name: t_sys_login_log_p202608_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202608_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202608_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202608_username_create_time_idx;


--
-- Name: t_sys_login_log_p202609_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202609_id_idx;


--
-- Name: t_sys_login_log_p202609_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202609_pkey;


--
-- Name: t_sys_login_log_p202609_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202609_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202609_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202609_trace_id_idx;


--
-- Name: t_sys_login_log_p202609_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202609_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202609_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202609_username_create_time_idx;


--
-- Name: t_sys_login_log_p202610_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202610_id_idx;


--
-- Name: t_sys_login_log_p202610_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202610_pkey;


--
-- Name: t_sys_login_log_p202610_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202610_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202610_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202610_trace_id_idx;


--
-- Name: t_sys_login_log_p202610_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202610_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202610_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202610_username_create_time_idx;


--
-- Name: t_sys_login_log_p202611_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202611_id_idx;


--
-- Name: t_sys_login_log_p202611_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202611_pkey;


--
-- Name: t_sys_login_log_p202611_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202611_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202611_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202611_trace_id_idx;


--
-- Name: t_sys_login_log_p202611_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202611_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202611_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202611_username_create_time_idx;


--
-- Name: t_sys_login_log_p202612_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202612_id_idx;


--
-- Name: t_sys_login_log_p202612_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202612_pkey;


--
-- Name: t_sys_login_log_p202612_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202612_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202612_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202612_trace_id_idx;


--
-- Name: t_sys_login_log_p202612_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202612_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202612_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202612_username_create_time_idx;


--
-- Name: t_sys_login_log_p202701_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202701_id_idx;


--
-- Name: t_sys_login_log_p202701_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202701_pkey;


--
-- Name: t_sys_login_log_p202701_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202701_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202701_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202701_trace_id_idx;


--
-- Name: t_sys_login_log_p202701_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202701_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202701_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202701_username_create_time_idx;


--
-- Name: t_sys_login_log_p202702_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202702_id_idx;


--
-- Name: t_sys_login_log_p202702_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202702_pkey;


--
-- Name: t_sys_login_log_p202702_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202702_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202702_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202702_trace_id_idx;


--
-- Name: t_sys_login_log_p202702_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202702_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202702_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202702_username_create_time_idx;


--
-- Name: t_sys_login_log_p202703_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202703_id_idx;


--
-- Name: t_sys_login_log_p202703_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202703_pkey;


--
-- Name: t_sys_login_log_p202703_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202703_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202703_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202703_trace_id_idx;


--
-- Name: t_sys_login_log_p202703_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202703_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202703_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202703_username_create_time_idx;


--
-- Name: t_sys_login_log_p202704_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202704_id_idx;


--
-- Name: t_sys_login_log_p202704_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202704_pkey;


--
-- Name: t_sys_login_log_p202704_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202704_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202704_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202704_trace_id_idx;


--
-- Name: t_sys_login_log_p202704_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202704_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202704_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202704_username_create_time_idx;


--
-- Name: t_sys_login_log_p202705_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202705_id_idx;


--
-- Name: t_sys_login_log_p202705_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202705_pkey;


--
-- Name: t_sys_login_log_p202705_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202705_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202705_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202705_trace_id_idx;


--
-- Name: t_sys_login_log_p202705_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202705_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202705_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202705_username_create_time_idx;


--
-- Name: t_sys_login_log_p202706_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202706_id_idx;


--
-- Name: t_sys_login_log_p202706_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202706_pkey;


--
-- Name: t_sys_login_log_p202706_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202706_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202706_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202706_trace_id_idx;


--
-- Name: t_sys_login_log_p202706_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202706_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202706_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202706_username_create_time_idx;


--
-- Name: t_sys_login_log_p202707_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202707_id_idx;


--
-- Name: t_sys_login_log_p202707_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202707_pkey;


--
-- Name: t_sys_login_log_p202707_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202707_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202707_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202707_trace_id_idx;


--
-- Name: t_sys_login_log_p202707_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202707_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202707_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202707_username_create_time_idx;


--
-- Name: t_sys_login_log_p202708_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202708_id_idx;


--
-- Name: t_sys_login_log_p202708_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202708_pkey;


--
-- Name: t_sys_login_log_p202708_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202708_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202708_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202708_trace_id_idx;


--
-- Name: t_sys_login_log_p202708_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202708_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202708_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202708_username_create_time_idx;


--
-- Name: t_sys_login_log_p202709_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202709_id_idx;


--
-- Name: t_sys_login_log_p202709_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202709_pkey;


--
-- Name: t_sys_login_log_p202709_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202709_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202709_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202709_trace_id_idx;


--
-- Name: t_sys_login_log_p202709_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202709_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202709_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202709_username_create_time_idx;


--
-- Name: t_sys_login_log_p202710_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202710_id_idx;


--
-- Name: t_sys_login_log_p202710_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202710_pkey;


--
-- Name: t_sys_login_log_p202710_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202710_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202710_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202710_trace_id_idx;


--
-- Name: t_sys_login_log_p202710_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202710_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202710_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202710_username_create_time_idx;


--
-- Name: t_sys_login_log_p202711_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202711_id_idx;


--
-- Name: t_sys_login_log_p202711_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202711_pkey;


--
-- Name: t_sys_login_log_p202711_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202711_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202711_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202711_trace_id_idx;


--
-- Name: t_sys_login_log_p202711_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202711_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202711_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202711_username_create_time_idx;


--
-- Name: t_sys_login_log_p202712_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202712_id_idx;


--
-- Name: t_sys_login_log_p202712_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202712_pkey;


--
-- Name: t_sys_login_log_p202712_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202712_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202712_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202712_trace_id_idx;


--
-- Name: t_sys_login_log_p202712_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202712_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202712_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202712_username_create_time_idx;


--
-- Name: t_sys_login_log_p202801_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202801_id_idx;


--
-- Name: t_sys_login_log_p202801_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202801_pkey;


--
-- Name: t_sys_login_log_p202801_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202801_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202801_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202801_trace_id_idx;


--
-- Name: t_sys_login_log_p202801_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202801_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202801_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202801_username_create_time_idx;


--
-- Name: t_sys_login_log_p202802_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202802_id_idx;


--
-- Name: t_sys_login_log_p202802_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202802_pkey;


--
-- Name: t_sys_login_log_p202802_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202802_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202802_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202802_trace_id_idx;


--
-- Name: t_sys_login_log_p202802_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202802_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202802_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202802_username_create_time_idx;


--
-- Name: t_sys_login_log_p202803_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202803_id_idx;


--
-- Name: t_sys_login_log_p202803_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202803_pkey;


--
-- Name: t_sys_login_log_p202803_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202803_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202803_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202803_trace_id_idx;


--
-- Name: t_sys_login_log_p202803_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202803_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202803_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202803_username_create_time_idx;


--
-- Name: t_sys_login_log_p202804_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202804_id_idx;


--
-- Name: t_sys_login_log_p202804_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202804_pkey;


--
-- Name: t_sys_login_log_p202804_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202804_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202804_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202804_trace_id_idx;


--
-- Name: t_sys_login_log_p202804_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202804_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202804_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202804_username_create_time_idx;


--
-- Name: t_sys_login_log_p202805_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202805_id_idx;


--
-- Name: t_sys_login_log_p202805_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202805_pkey;


--
-- Name: t_sys_login_log_p202805_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202805_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202805_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202805_trace_id_idx;


--
-- Name: t_sys_login_log_p202805_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202805_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202805_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202805_username_create_time_idx;


--
-- Name: t_sys_login_log_p202806_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202806_id_idx;


--
-- Name: t_sys_login_log_p202806_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202806_pkey;


--
-- Name: t_sys_login_log_p202806_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202806_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202806_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202806_trace_id_idx;


--
-- Name: t_sys_login_log_p202806_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202806_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202806_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202806_username_create_time_idx;


--
-- Name: t_sys_login_log_p202807_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202807_id_idx;


--
-- Name: t_sys_login_log_p202807_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202807_pkey;


--
-- Name: t_sys_login_log_p202807_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202807_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202807_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202807_trace_id_idx;


--
-- Name: t_sys_login_log_p202807_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202807_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202807_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202807_username_create_time_idx;


--
-- Name: t_sys_login_log_p202808_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202808_id_idx;


--
-- Name: t_sys_login_log_p202808_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202808_pkey;


--
-- Name: t_sys_login_log_p202808_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202808_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202808_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202808_trace_id_idx;


--
-- Name: t_sys_login_log_p202808_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202808_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202808_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202808_username_create_time_idx;


--
-- Name: t_sys_login_log_p202809_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202809_id_idx;


--
-- Name: t_sys_login_log_p202809_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202809_pkey;


--
-- Name: t_sys_login_log_p202809_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202809_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202809_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202809_trace_id_idx;


--
-- Name: t_sys_login_log_p202809_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202809_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202809_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202809_username_create_time_idx;


--
-- Name: t_sys_login_log_p202810_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202810_id_idx;


--
-- Name: t_sys_login_log_p202810_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202810_pkey;


--
-- Name: t_sys_login_log_p202810_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202810_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202810_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202810_trace_id_idx;


--
-- Name: t_sys_login_log_p202810_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202810_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202810_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202810_username_create_time_idx;


--
-- Name: t_sys_login_log_p202811_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202811_id_idx;


--
-- Name: t_sys_login_log_p202811_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202811_pkey;


--
-- Name: t_sys_login_log_p202811_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202811_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202811_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202811_trace_id_idx;


--
-- Name: t_sys_login_log_p202811_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202811_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202811_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202811_username_create_time_idx;


--
-- Name: t_sys_login_log_p202812_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202812_id_idx;


--
-- Name: t_sys_login_log_p202812_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202812_pkey;


--
-- Name: t_sys_login_log_p202812_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202812_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202812_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202812_trace_id_idx;


--
-- Name: t_sys_login_log_p202812_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202812_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202812_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202812_username_create_time_idx;


--
-- Name: t_sys_login_log_p202901_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202901_id_idx;


--
-- Name: t_sys_login_log_p202901_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202901_pkey;


--
-- Name: t_sys_login_log_p202901_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202901_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202901_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202901_trace_id_idx;


--
-- Name: t_sys_login_log_p202901_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202901_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202901_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202901_username_create_time_idx;


--
-- Name: t_sys_login_log_p202902_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202902_id_idx;


--
-- Name: t_sys_login_log_p202902_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202902_pkey;


--
-- Name: t_sys_login_log_p202902_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202902_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202902_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202902_trace_id_idx;


--
-- Name: t_sys_login_log_p202902_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202902_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202902_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202902_username_create_time_idx;


--
-- Name: t_sys_login_log_p202903_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202903_id_idx;


--
-- Name: t_sys_login_log_p202903_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202903_pkey;


--
-- Name: t_sys_login_log_p202903_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202903_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202903_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202903_trace_id_idx;


--
-- Name: t_sys_login_log_p202903_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202903_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202903_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202903_username_create_time_idx;


--
-- Name: t_sys_login_log_p202904_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202904_id_idx;


--
-- Name: t_sys_login_log_p202904_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202904_pkey;


--
-- Name: t_sys_login_log_p202904_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202904_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202904_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202904_trace_id_idx;


--
-- Name: t_sys_login_log_p202904_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202904_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202904_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202904_username_create_time_idx;


--
-- Name: t_sys_login_log_p202905_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202905_id_idx;


--
-- Name: t_sys_login_log_p202905_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202905_pkey;


--
-- Name: t_sys_login_log_p202905_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202905_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202905_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202905_trace_id_idx;


--
-- Name: t_sys_login_log_p202905_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202905_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202905_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202905_username_create_time_idx;


--
-- Name: t_sys_login_log_p202906_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202906_id_idx;


--
-- Name: t_sys_login_log_p202906_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202906_pkey;


--
-- Name: t_sys_login_log_p202906_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202906_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202906_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202906_trace_id_idx;


--
-- Name: t_sys_login_log_p202906_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202906_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202906_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202906_username_create_time_idx;


--
-- Name: t_sys_login_log_p202907_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202907_id_idx;


--
-- Name: t_sys_login_log_p202907_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202907_pkey;


--
-- Name: t_sys_login_log_p202907_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202907_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202907_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202907_trace_id_idx;


--
-- Name: t_sys_login_log_p202907_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202907_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202907_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202907_username_create_time_idx;


--
-- Name: t_sys_login_log_p202908_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202908_id_idx;


--
-- Name: t_sys_login_log_p202908_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202908_pkey;


--
-- Name: t_sys_login_log_p202908_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202908_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202908_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202908_trace_id_idx;


--
-- Name: t_sys_login_log_p202908_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202908_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202908_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202908_username_create_time_idx;


--
-- Name: t_sys_login_log_p202909_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202909_id_idx;


--
-- Name: t_sys_login_log_p202909_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202909_pkey;


--
-- Name: t_sys_login_log_p202909_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202909_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202909_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202909_trace_id_idx;


--
-- Name: t_sys_login_log_p202909_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202909_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202909_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202909_username_create_time_idx;


--
-- Name: t_sys_login_log_p202910_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202910_id_idx;


--
-- Name: t_sys_login_log_p202910_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202910_pkey;


--
-- Name: t_sys_login_log_p202910_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202910_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202910_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202910_trace_id_idx;


--
-- Name: t_sys_login_log_p202910_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202910_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202910_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202910_username_create_time_idx;


--
-- Name: t_sys_login_log_p202911_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202911_id_idx;


--
-- Name: t_sys_login_log_p202911_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202911_pkey;


--
-- Name: t_sys_login_log_p202911_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202911_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202911_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202911_trace_id_idx;


--
-- Name: t_sys_login_log_p202911_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202911_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202911_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202911_username_create_time_idx;


--
-- Name: t_sys_login_log_p202912_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_id ATTACH PARTITION public.t_sys_login_log_p202912_id_idx;


--
-- Name: t_sys_login_log_p202912_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_login_log ATTACH PARTITION public.t_sys_login_log_p202912_pkey;


--
-- Name: t_sys_login_log_p202912_success_event_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_result_time ATTACH PARTITION public.t_sys_login_log_p202912_success_event_type_create_time_id_idx;


--
-- Name: t_sys_login_log_p202912_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_trace_id ATTACH PARTITION public.t_sys_login_log_p202912_trace_id_idx;


--
-- Name: t_sys_login_log_p202912_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_user_time ATTACH PARTITION public.t_sys_login_log_p202912_user_id_create_time_idx;


--
-- Name: t_sys_login_log_p202912_username_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_login_log_name_time ATTACH PARTITION public.t_sys_login_log_p202912_username_create_time_idx;


--
-- Name: t_sys_operate_log_default_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_default_id_idx;


--
-- Name: t_sys_operate_log_default_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_default_pkey;


--
-- Name: t_sys_operate_log_default_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_default_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_default_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_default_trace_id_idx;


--
-- Name: t_sys_operate_log_default_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_default_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202601_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202601_id_idx;


--
-- Name: t_sys_operate_log_p202601_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202601_pkey;


--
-- Name: t_sys_operate_log_p202601_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202601_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202601_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202601_trace_id_idx;


--
-- Name: t_sys_operate_log_p202601_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202601_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202602_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202602_id_idx;


--
-- Name: t_sys_operate_log_p202602_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202602_pkey;


--
-- Name: t_sys_operate_log_p202602_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202602_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202602_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202602_trace_id_idx;


--
-- Name: t_sys_operate_log_p202602_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202602_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202603_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202603_id_idx;


--
-- Name: t_sys_operate_log_p202603_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202603_pkey;


--
-- Name: t_sys_operate_log_p202603_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202603_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202603_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202603_trace_id_idx;


--
-- Name: t_sys_operate_log_p202603_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202603_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202604_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202604_id_idx;


--
-- Name: t_sys_operate_log_p202604_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202604_pkey;


--
-- Name: t_sys_operate_log_p202604_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202604_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202604_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202604_trace_id_idx;


--
-- Name: t_sys_operate_log_p202604_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202604_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202605_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202605_id_idx;


--
-- Name: t_sys_operate_log_p202605_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202605_pkey;


--
-- Name: t_sys_operate_log_p202605_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202605_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202605_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202605_trace_id_idx;


--
-- Name: t_sys_operate_log_p202605_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202605_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202606_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202606_id_idx;


--
-- Name: t_sys_operate_log_p202606_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202606_pkey;


--
-- Name: t_sys_operate_log_p202606_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202606_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202606_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202606_trace_id_idx;


--
-- Name: t_sys_operate_log_p202606_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202606_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202607_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202607_id_idx;


--
-- Name: t_sys_operate_log_p202607_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202607_pkey;


--
-- Name: t_sys_operate_log_p202607_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202607_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202607_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202607_trace_id_idx;


--
-- Name: t_sys_operate_log_p202607_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202607_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202608_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202608_id_idx;


--
-- Name: t_sys_operate_log_p202608_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202608_pkey;


--
-- Name: t_sys_operate_log_p202608_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202608_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202608_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202608_trace_id_idx;


--
-- Name: t_sys_operate_log_p202608_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202608_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202609_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202609_id_idx;


--
-- Name: t_sys_operate_log_p202609_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202609_pkey;


--
-- Name: t_sys_operate_log_p202609_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202609_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202609_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202609_trace_id_idx;


--
-- Name: t_sys_operate_log_p202609_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202609_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202610_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202610_id_idx;


--
-- Name: t_sys_operate_log_p202610_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202610_pkey;


--
-- Name: t_sys_operate_log_p202610_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202610_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202610_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202610_trace_id_idx;


--
-- Name: t_sys_operate_log_p202610_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202610_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202611_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202611_id_idx;


--
-- Name: t_sys_operate_log_p202611_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202611_pkey;


--
-- Name: t_sys_operate_log_p202611_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202611_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202611_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202611_trace_id_idx;


--
-- Name: t_sys_operate_log_p202611_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202611_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202612_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202612_id_idx;


--
-- Name: t_sys_operate_log_p202612_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202612_pkey;


--
-- Name: t_sys_operate_log_p202612_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202612_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202612_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202612_trace_id_idx;


--
-- Name: t_sys_operate_log_p202612_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202612_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202701_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202701_id_idx;


--
-- Name: t_sys_operate_log_p202701_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202701_pkey;


--
-- Name: t_sys_operate_log_p202701_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202701_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202701_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202701_trace_id_idx;


--
-- Name: t_sys_operate_log_p202701_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202701_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202702_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202702_id_idx;


--
-- Name: t_sys_operate_log_p202702_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202702_pkey;


--
-- Name: t_sys_operate_log_p202702_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202702_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202702_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202702_trace_id_idx;


--
-- Name: t_sys_operate_log_p202702_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202702_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202703_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202703_id_idx;


--
-- Name: t_sys_operate_log_p202703_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202703_pkey;


--
-- Name: t_sys_operate_log_p202703_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202703_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202703_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202703_trace_id_idx;


--
-- Name: t_sys_operate_log_p202703_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202703_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202704_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202704_id_idx;


--
-- Name: t_sys_operate_log_p202704_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202704_pkey;


--
-- Name: t_sys_operate_log_p202704_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202704_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202704_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202704_trace_id_idx;


--
-- Name: t_sys_operate_log_p202704_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202704_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202705_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202705_id_idx;


--
-- Name: t_sys_operate_log_p202705_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202705_pkey;


--
-- Name: t_sys_operate_log_p202705_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202705_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202705_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202705_trace_id_idx;


--
-- Name: t_sys_operate_log_p202705_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202705_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202706_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202706_id_idx;


--
-- Name: t_sys_operate_log_p202706_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202706_pkey;


--
-- Name: t_sys_operate_log_p202706_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202706_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202706_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202706_trace_id_idx;


--
-- Name: t_sys_operate_log_p202706_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202706_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202707_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202707_id_idx;


--
-- Name: t_sys_operate_log_p202707_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202707_pkey;


--
-- Name: t_sys_operate_log_p202707_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202707_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202707_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202707_trace_id_idx;


--
-- Name: t_sys_operate_log_p202707_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202707_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202708_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202708_id_idx;


--
-- Name: t_sys_operate_log_p202708_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202708_pkey;


--
-- Name: t_sys_operate_log_p202708_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202708_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202708_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202708_trace_id_idx;


--
-- Name: t_sys_operate_log_p202708_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202708_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202709_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202709_id_idx;


--
-- Name: t_sys_operate_log_p202709_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202709_pkey;


--
-- Name: t_sys_operate_log_p202709_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202709_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202709_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202709_trace_id_idx;


--
-- Name: t_sys_operate_log_p202709_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202709_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202710_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202710_id_idx;


--
-- Name: t_sys_operate_log_p202710_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202710_pkey;


--
-- Name: t_sys_operate_log_p202710_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202710_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202710_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202710_trace_id_idx;


--
-- Name: t_sys_operate_log_p202710_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202710_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202711_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202711_id_idx;


--
-- Name: t_sys_operate_log_p202711_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202711_pkey;


--
-- Name: t_sys_operate_log_p202711_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202711_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202711_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202711_trace_id_idx;


--
-- Name: t_sys_operate_log_p202711_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202711_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202712_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202712_id_idx;


--
-- Name: t_sys_operate_log_p202712_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202712_pkey;


--
-- Name: t_sys_operate_log_p202712_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202712_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202712_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202712_trace_id_idx;


--
-- Name: t_sys_operate_log_p202712_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202712_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202801_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202801_id_idx;


--
-- Name: t_sys_operate_log_p202801_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202801_pkey;


--
-- Name: t_sys_operate_log_p202801_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202801_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202801_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202801_trace_id_idx;


--
-- Name: t_sys_operate_log_p202801_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202801_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202802_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202802_id_idx;


--
-- Name: t_sys_operate_log_p202802_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202802_pkey;


--
-- Name: t_sys_operate_log_p202802_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202802_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202802_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202802_trace_id_idx;


--
-- Name: t_sys_operate_log_p202802_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202802_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202803_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202803_id_idx;


--
-- Name: t_sys_operate_log_p202803_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202803_pkey;


--
-- Name: t_sys_operate_log_p202803_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202803_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202803_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202803_trace_id_idx;


--
-- Name: t_sys_operate_log_p202803_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202803_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202804_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202804_id_idx;


--
-- Name: t_sys_operate_log_p202804_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202804_pkey;


--
-- Name: t_sys_operate_log_p202804_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202804_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202804_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202804_trace_id_idx;


--
-- Name: t_sys_operate_log_p202804_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202804_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202805_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202805_id_idx;


--
-- Name: t_sys_operate_log_p202805_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202805_pkey;


--
-- Name: t_sys_operate_log_p202805_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202805_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202805_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202805_trace_id_idx;


--
-- Name: t_sys_operate_log_p202805_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202805_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202806_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202806_id_idx;


--
-- Name: t_sys_operate_log_p202806_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202806_pkey;


--
-- Name: t_sys_operate_log_p202806_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202806_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202806_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202806_trace_id_idx;


--
-- Name: t_sys_operate_log_p202806_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202806_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202807_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202807_id_idx;


--
-- Name: t_sys_operate_log_p202807_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202807_pkey;


--
-- Name: t_sys_operate_log_p202807_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202807_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202807_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202807_trace_id_idx;


--
-- Name: t_sys_operate_log_p202807_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202807_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202808_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202808_id_idx;


--
-- Name: t_sys_operate_log_p202808_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202808_pkey;


--
-- Name: t_sys_operate_log_p202808_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202808_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202808_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202808_trace_id_idx;


--
-- Name: t_sys_operate_log_p202808_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202808_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202809_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202809_id_idx;


--
-- Name: t_sys_operate_log_p202809_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202809_pkey;


--
-- Name: t_sys_operate_log_p202809_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202809_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202809_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202809_trace_id_idx;


--
-- Name: t_sys_operate_log_p202809_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202809_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202810_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202810_id_idx;


--
-- Name: t_sys_operate_log_p202810_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202810_pkey;


--
-- Name: t_sys_operate_log_p202810_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202810_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202810_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202810_trace_id_idx;


--
-- Name: t_sys_operate_log_p202810_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202810_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202811_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202811_id_idx;


--
-- Name: t_sys_operate_log_p202811_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202811_pkey;


--
-- Name: t_sys_operate_log_p202811_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202811_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202811_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202811_trace_id_idx;


--
-- Name: t_sys_operate_log_p202811_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202811_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202812_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202812_id_idx;


--
-- Name: t_sys_operate_log_p202812_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202812_pkey;


--
-- Name: t_sys_operate_log_p202812_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202812_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202812_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202812_trace_id_idx;


--
-- Name: t_sys_operate_log_p202812_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202812_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202901_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202901_id_idx;


--
-- Name: t_sys_operate_log_p202901_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202901_pkey;


--
-- Name: t_sys_operate_log_p202901_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202901_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202901_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202901_trace_id_idx;


--
-- Name: t_sys_operate_log_p202901_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202901_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202902_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202902_id_idx;


--
-- Name: t_sys_operate_log_p202902_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202902_pkey;


--
-- Name: t_sys_operate_log_p202902_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202902_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202902_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202902_trace_id_idx;


--
-- Name: t_sys_operate_log_p202902_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202902_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202903_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202903_id_idx;


--
-- Name: t_sys_operate_log_p202903_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202903_pkey;


--
-- Name: t_sys_operate_log_p202903_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202903_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202903_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202903_trace_id_idx;


--
-- Name: t_sys_operate_log_p202903_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202903_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202904_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202904_id_idx;


--
-- Name: t_sys_operate_log_p202904_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202904_pkey;


--
-- Name: t_sys_operate_log_p202904_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202904_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202904_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202904_trace_id_idx;


--
-- Name: t_sys_operate_log_p202904_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202904_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202905_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202905_id_idx;


--
-- Name: t_sys_operate_log_p202905_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202905_pkey;


--
-- Name: t_sys_operate_log_p202905_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202905_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202905_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202905_trace_id_idx;


--
-- Name: t_sys_operate_log_p202905_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202905_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202906_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202906_id_idx;


--
-- Name: t_sys_operate_log_p202906_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202906_pkey;


--
-- Name: t_sys_operate_log_p202906_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202906_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202906_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202906_trace_id_idx;


--
-- Name: t_sys_operate_log_p202906_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202906_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202907_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202907_id_idx;


--
-- Name: t_sys_operate_log_p202907_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202907_pkey;


--
-- Name: t_sys_operate_log_p202907_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202907_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202907_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202907_trace_id_idx;


--
-- Name: t_sys_operate_log_p202907_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202907_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202908_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202908_id_idx;


--
-- Name: t_sys_operate_log_p202908_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202908_pkey;


--
-- Name: t_sys_operate_log_p202908_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202908_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202908_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202908_trace_id_idx;


--
-- Name: t_sys_operate_log_p202908_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202908_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202909_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202909_id_idx;


--
-- Name: t_sys_operate_log_p202909_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202909_pkey;


--
-- Name: t_sys_operate_log_p202909_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202909_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202909_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202909_trace_id_idx;


--
-- Name: t_sys_operate_log_p202909_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202909_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202910_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202910_id_idx;


--
-- Name: t_sys_operate_log_p202910_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202910_pkey;


--
-- Name: t_sys_operate_log_p202910_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202910_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202910_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202910_trace_id_idx;


--
-- Name: t_sys_operate_log_p202910_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202910_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202911_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202911_id_idx;


--
-- Name: t_sys_operate_log_p202911_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202911_pkey;


--
-- Name: t_sys_operate_log_p202911_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202911_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202911_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202911_trace_id_idx;


--
-- Name: t_sys_operate_log_p202911_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202911_user_id_create_time_idx;


--
-- Name: t_sys_operate_log_p202912_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_id ATTACH PARTITION public.t_sys_operate_log_p202912_id_idx;


--
-- Name: t_sys_operate_log_p202912_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_operate_log ATTACH PARTITION public.t_sys_operate_log_p202912_pkey;


--
-- Name: t_sys_operate_log_p202912_success_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_result_time ATTACH PARTITION public.t_sys_operate_log_p202912_success_create_time_id_idx;


--
-- Name: t_sys_operate_log_p202912_trace_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_trace_id ATTACH PARTITION public.t_sys_operate_log_p202912_trace_id_idx;


--
-- Name: t_sys_operate_log_p202912_user_id_create_time_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_operate_log_user_time ATTACH PARTITION public.t_sys_operate_log_p202912_user_id_create_time_idx;


--
-- Name: t_sys_script_log_default_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_default_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_default_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_default_id_idx;


--
-- Name: t_sys_script_log_default_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_default_pkey;


--
-- Name: t_sys_script_log_p202601_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202601_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202601_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202601_id_idx;


--
-- Name: t_sys_script_log_p202601_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202601_pkey;


--
-- Name: t_sys_script_log_p202602_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202602_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202602_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202602_id_idx;


--
-- Name: t_sys_script_log_p202602_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202602_pkey;


--
-- Name: t_sys_script_log_p202603_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202603_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202603_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202603_id_idx;


--
-- Name: t_sys_script_log_p202603_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202603_pkey;


--
-- Name: t_sys_script_log_p202604_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202604_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202604_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202604_id_idx;


--
-- Name: t_sys_script_log_p202604_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202604_pkey;


--
-- Name: t_sys_script_log_p202605_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202605_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202605_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202605_id_idx;


--
-- Name: t_sys_script_log_p202605_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202605_pkey;


--
-- Name: t_sys_script_log_p202606_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202606_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202606_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202606_id_idx;


--
-- Name: t_sys_script_log_p202606_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202606_pkey;


--
-- Name: t_sys_script_log_p202607_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202607_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202607_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202607_id_idx;


--
-- Name: t_sys_script_log_p202607_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202607_pkey;


--
-- Name: t_sys_script_log_p202608_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202608_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202608_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202608_id_idx;


--
-- Name: t_sys_script_log_p202608_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202608_pkey;


--
-- Name: t_sys_script_log_p202609_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202609_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202609_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202609_id_idx;


--
-- Name: t_sys_script_log_p202609_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202609_pkey;


--
-- Name: t_sys_script_log_p202610_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202610_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202610_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202610_id_idx;


--
-- Name: t_sys_script_log_p202610_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202610_pkey;


--
-- Name: t_sys_script_log_p202611_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202611_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202611_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202611_id_idx;


--
-- Name: t_sys_script_log_p202611_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202611_pkey;


--
-- Name: t_sys_script_log_p202612_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202612_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202612_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202612_id_idx;


--
-- Name: t_sys_script_log_p202612_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202612_pkey;


--
-- Name: t_sys_script_log_p202701_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202701_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202701_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202701_id_idx;


--
-- Name: t_sys_script_log_p202701_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202701_pkey;


--
-- Name: t_sys_script_log_p202702_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202702_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202702_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202702_id_idx;


--
-- Name: t_sys_script_log_p202702_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202702_pkey;


--
-- Name: t_sys_script_log_p202703_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202703_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202703_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202703_id_idx;


--
-- Name: t_sys_script_log_p202703_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202703_pkey;


--
-- Name: t_sys_script_log_p202704_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202704_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202704_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202704_id_idx;


--
-- Name: t_sys_script_log_p202704_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202704_pkey;


--
-- Name: t_sys_script_log_p202705_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202705_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202705_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202705_id_idx;


--
-- Name: t_sys_script_log_p202705_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202705_pkey;


--
-- Name: t_sys_script_log_p202706_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202706_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202706_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202706_id_idx;


--
-- Name: t_sys_script_log_p202706_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202706_pkey;


--
-- Name: t_sys_script_log_p202707_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202707_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202707_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202707_id_idx;


--
-- Name: t_sys_script_log_p202707_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202707_pkey;


--
-- Name: t_sys_script_log_p202708_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202708_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202708_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202708_id_idx;


--
-- Name: t_sys_script_log_p202708_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202708_pkey;


--
-- Name: t_sys_script_log_p202709_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202709_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202709_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202709_id_idx;


--
-- Name: t_sys_script_log_p202709_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202709_pkey;


--
-- Name: t_sys_script_log_p202710_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202710_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202710_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202710_id_idx;


--
-- Name: t_sys_script_log_p202710_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202710_pkey;


--
-- Name: t_sys_script_log_p202711_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202711_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202711_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202711_id_idx;


--
-- Name: t_sys_script_log_p202711_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202711_pkey;


--
-- Name: t_sys_script_log_p202712_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202712_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202712_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202712_id_idx;


--
-- Name: t_sys_script_log_p202712_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202712_pkey;


--
-- Name: t_sys_script_log_p202801_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202801_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202801_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202801_id_idx;


--
-- Name: t_sys_script_log_p202801_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202801_pkey;


--
-- Name: t_sys_script_log_p202802_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202802_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202802_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202802_id_idx;


--
-- Name: t_sys_script_log_p202802_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202802_pkey;


--
-- Name: t_sys_script_log_p202803_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202803_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202803_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202803_id_idx;


--
-- Name: t_sys_script_log_p202803_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202803_pkey;


--
-- Name: t_sys_script_log_p202804_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202804_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202804_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202804_id_idx;


--
-- Name: t_sys_script_log_p202804_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202804_pkey;


--
-- Name: t_sys_script_log_p202805_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202805_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202805_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202805_id_idx;


--
-- Name: t_sys_script_log_p202805_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202805_pkey;


--
-- Name: t_sys_script_log_p202806_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202806_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202806_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202806_id_idx;


--
-- Name: t_sys_script_log_p202806_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202806_pkey;


--
-- Name: t_sys_script_log_p202807_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202807_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202807_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202807_id_idx;


--
-- Name: t_sys_script_log_p202807_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202807_pkey;


--
-- Name: t_sys_script_log_p202808_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202808_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202808_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202808_id_idx;


--
-- Name: t_sys_script_log_p202808_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202808_pkey;


--
-- Name: t_sys_script_log_p202809_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202809_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202809_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202809_id_idx;


--
-- Name: t_sys_script_log_p202809_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202809_pkey;


--
-- Name: t_sys_script_log_p202810_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202810_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202810_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202810_id_idx;


--
-- Name: t_sys_script_log_p202810_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202810_pkey;


--
-- Name: t_sys_script_log_p202811_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202811_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202811_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202811_id_idx;


--
-- Name: t_sys_script_log_p202811_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202811_pkey;


--
-- Name: t_sys_script_log_p202812_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202812_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202812_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202812_id_idx;


--
-- Name: t_sys_script_log_p202812_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202812_pkey;


--
-- Name: t_sys_script_log_p202901_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202901_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202901_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202901_id_idx;


--
-- Name: t_sys_script_log_p202901_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202901_pkey;


--
-- Name: t_sys_script_log_p202902_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202902_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202902_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202902_id_idx;


--
-- Name: t_sys_script_log_p202902_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202902_pkey;


--
-- Name: t_sys_script_log_p202903_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202903_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202903_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202903_id_idx;


--
-- Name: t_sys_script_log_p202903_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202903_pkey;


--
-- Name: t_sys_script_log_p202904_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202904_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202904_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202904_id_idx;


--
-- Name: t_sys_script_log_p202904_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202904_pkey;


--
-- Name: t_sys_script_log_p202905_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202905_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202905_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202905_id_idx;


--
-- Name: t_sys_script_log_p202905_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202905_pkey;


--
-- Name: t_sys_script_log_p202906_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202906_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202906_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202906_id_idx;


--
-- Name: t_sys_script_log_p202906_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202906_pkey;


--
-- Name: t_sys_script_log_p202907_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202907_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202907_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202907_id_idx;


--
-- Name: t_sys_script_log_p202907_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202907_pkey;


--
-- Name: t_sys_script_log_p202908_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202908_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202908_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202908_id_idx;


--
-- Name: t_sys_script_log_p202908_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202908_pkey;


--
-- Name: t_sys_script_log_p202909_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202909_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202909_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202909_id_idx;


--
-- Name: t_sys_script_log_p202909_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202909_pkey;


--
-- Name: t_sys_script_log_p202910_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202910_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202910_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202910_id_idx;


--
-- Name: t_sys_script_log_p202910_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202910_pkey;


--
-- Name: t_sys_script_log_p202911_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202911_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202911_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202911_id_idx;


--
-- Name: t_sys_script_log_p202911_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202911_pkey;


--
-- Name: t_sys_script_log_p202912_execute_status_transaction_mode_cr_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_status_time ATTACH PARTITION public.t_sys_script_log_p202912_execute_status_transaction_mode_cr_idx;


--
-- Name: t_sys_script_log_p202912_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_script_log_id ATTACH PARTITION public.t_sys_script_log_p202912_id_idx;


--
-- Name: t_sys_script_log_p202912_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_script_log ATTACH PARTITION public.t_sys_script_log_p202912_pkey;


--
-- Name: t_sys_sql_log_default_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_default_id_idx;


--
-- Name: t_sys_sql_log_default_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_default_pkey;


--
-- Name: t_sys_sql_log_default_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_default_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202601_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202601_id_idx;


--
-- Name: t_sys_sql_log_p202601_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202601_pkey;


--
-- Name: t_sys_sql_log_p202601_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202601_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202602_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202602_id_idx;


--
-- Name: t_sys_sql_log_p202602_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202602_pkey;


--
-- Name: t_sys_sql_log_p202602_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202602_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202603_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202603_id_idx;


--
-- Name: t_sys_sql_log_p202603_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202603_pkey;


--
-- Name: t_sys_sql_log_p202603_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202603_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202604_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202604_id_idx;


--
-- Name: t_sys_sql_log_p202604_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202604_pkey;


--
-- Name: t_sys_sql_log_p202604_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202604_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202605_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202605_id_idx;


--
-- Name: t_sys_sql_log_p202605_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202605_pkey;


--
-- Name: t_sys_sql_log_p202605_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202605_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202606_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202606_id_idx;


--
-- Name: t_sys_sql_log_p202606_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202606_pkey;


--
-- Name: t_sys_sql_log_p202606_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202606_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202607_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202607_id_idx;


--
-- Name: t_sys_sql_log_p202607_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202607_pkey;


--
-- Name: t_sys_sql_log_p202607_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202607_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202608_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202608_id_idx;


--
-- Name: t_sys_sql_log_p202608_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202608_pkey;


--
-- Name: t_sys_sql_log_p202608_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202608_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202609_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202609_id_idx;


--
-- Name: t_sys_sql_log_p202609_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202609_pkey;


--
-- Name: t_sys_sql_log_p202609_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202609_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202610_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202610_id_idx;


--
-- Name: t_sys_sql_log_p202610_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202610_pkey;


--
-- Name: t_sys_sql_log_p202610_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202610_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202611_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202611_id_idx;


--
-- Name: t_sys_sql_log_p202611_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202611_pkey;


--
-- Name: t_sys_sql_log_p202611_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202611_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202612_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202612_id_idx;


--
-- Name: t_sys_sql_log_p202612_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202612_pkey;


--
-- Name: t_sys_sql_log_p202612_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202612_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202701_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202701_id_idx;


--
-- Name: t_sys_sql_log_p202701_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202701_pkey;


--
-- Name: t_sys_sql_log_p202701_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202701_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202702_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202702_id_idx;


--
-- Name: t_sys_sql_log_p202702_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202702_pkey;


--
-- Name: t_sys_sql_log_p202702_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202702_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202703_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202703_id_idx;


--
-- Name: t_sys_sql_log_p202703_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202703_pkey;


--
-- Name: t_sys_sql_log_p202703_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202703_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202704_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202704_id_idx;


--
-- Name: t_sys_sql_log_p202704_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202704_pkey;


--
-- Name: t_sys_sql_log_p202704_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202704_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202705_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202705_id_idx;


--
-- Name: t_sys_sql_log_p202705_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202705_pkey;


--
-- Name: t_sys_sql_log_p202705_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202705_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202706_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202706_id_idx;


--
-- Name: t_sys_sql_log_p202706_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202706_pkey;


--
-- Name: t_sys_sql_log_p202706_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202706_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202707_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202707_id_idx;


--
-- Name: t_sys_sql_log_p202707_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202707_pkey;


--
-- Name: t_sys_sql_log_p202707_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202707_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202708_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202708_id_idx;


--
-- Name: t_sys_sql_log_p202708_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202708_pkey;


--
-- Name: t_sys_sql_log_p202708_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202708_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202709_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202709_id_idx;


--
-- Name: t_sys_sql_log_p202709_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202709_pkey;


--
-- Name: t_sys_sql_log_p202709_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202709_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202710_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202710_id_idx;


--
-- Name: t_sys_sql_log_p202710_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202710_pkey;


--
-- Name: t_sys_sql_log_p202710_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202710_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202711_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202711_id_idx;


--
-- Name: t_sys_sql_log_p202711_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202711_pkey;


--
-- Name: t_sys_sql_log_p202711_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202711_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202712_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202712_id_idx;


--
-- Name: t_sys_sql_log_p202712_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202712_pkey;


--
-- Name: t_sys_sql_log_p202712_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202712_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202801_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202801_id_idx;


--
-- Name: t_sys_sql_log_p202801_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202801_pkey;


--
-- Name: t_sys_sql_log_p202801_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202801_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202802_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202802_id_idx;


--
-- Name: t_sys_sql_log_p202802_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202802_pkey;


--
-- Name: t_sys_sql_log_p202802_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202802_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202803_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202803_id_idx;


--
-- Name: t_sys_sql_log_p202803_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202803_pkey;


--
-- Name: t_sys_sql_log_p202803_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202803_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202804_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202804_id_idx;


--
-- Name: t_sys_sql_log_p202804_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202804_pkey;


--
-- Name: t_sys_sql_log_p202804_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202804_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202805_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202805_id_idx;


--
-- Name: t_sys_sql_log_p202805_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202805_pkey;


--
-- Name: t_sys_sql_log_p202805_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202805_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202806_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202806_id_idx;


--
-- Name: t_sys_sql_log_p202806_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202806_pkey;


--
-- Name: t_sys_sql_log_p202806_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202806_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202807_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202807_id_idx;


--
-- Name: t_sys_sql_log_p202807_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202807_pkey;


--
-- Name: t_sys_sql_log_p202807_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202807_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202808_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202808_id_idx;


--
-- Name: t_sys_sql_log_p202808_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202808_pkey;


--
-- Name: t_sys_sql_log_p202808_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202808_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202809_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202809_id_idx;


--
-- Name: t_sys_sql_log_p202809_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202809_pkey;


--
-- Name: t_sys_sql_log_p202809_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202809_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202810_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202810_id_idx;


--
-- Name: t_sys_sql_log_p202810_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202810_pkey;


--
-- Name: t_sys_sql_log_p202810_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202810_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202811_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202811_id_idx;


--
-- Name: t_sys_sql_log_p202811_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202811_pkey;


--
-- Name: t_sys_sql_log_p202811_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202811_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202812_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202812_id_idx;


--
-- Name: t_sys_sql_log_p202812_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202812_pkey;


--
-- Name: t_sys_sql_log_p202812_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202812_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202901_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202901_id_idx;


--
-- Name: t_sys_sql_log_p202901_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202901_pkey;


--
-- Name: t_sys_sql_log_p202901_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202901_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202902_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202902_id_idx;


--
-- Name: t_sys_sql_log_p202902_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202902_pkey;


--
-- Name: t_sys_sql_log_p202902_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202902_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202903_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202903_id_idx;


--
-- Name: t_sys_sql_log_p202903_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202903_pkey;


--
-- Name: t_sys_sql_log_p202903_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202903_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202904_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202904_id_idx;


--
-- Name: t_sys_sql_log_p202904_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202904_pkey;


--
-- Name: t_sys_sql_log_p202904_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202904_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202905_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202905_id_idx;


--
-- Name: t_sys_sql_log_p202905_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202905_pkey;


--
-- Name: t_sys_sql_log_p202905_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202905_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202906_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202906_id_idx;


--
-- Name: t_sys_sql_log_p202906_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202906_pkey;


--
-- Name: t_sys_sql_log_p202906_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202906_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202907_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202907_id_idx;


--
-- Name: t_sys_sql_log_p202907_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202907_pkey;


--
-- Name: t_sys_sql_log_p202907_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202907_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202908_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202908_id_idx;


--
-- Name: t_sys_sql_log_p202908_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202908_pkey;


--
-- Name: t_sys_sql_log_p202908_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202908_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202909_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202909_id_idx;


--
-- Name: t_sys_sql_log_p202909_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202909_pkey;


--
-- Name: t_sys_sql_log_p202909_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202909_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202910_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202910_id_idx;


--
-- Name: t_sys_sql_log_p202910_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202910_pkey;


--
-- Name: t_sys_sql_log_p202910_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202910_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202911_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202911_id_idx;


--
-- Name: t_sys_sql_log_p202911_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202911_pkey;


--
-- Name: t_sys_sql_log_p202911_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202911_result_type_create_time_id_idx;


--
-- Name: t_sys_sql_log_p202912_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_id ATTACH PARTITION public.t_sys_sql_log_p202912_id_idx;


--
-- Name: t_sys_sql_log_p202912_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.pk_sys_sql_log ATTACH PARTITION public.t_sys_sql_log_p202912_pkey;


--
-- Name: t_sys_sql_log_p202912_result_type_create_time_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_sys_sql_log_result_time ATTACH PARTITION public.t_sys_sql_log_p202912_result_type_create_time_id_idx;


--
-- Name: t_sys_basic_data_category fk_basic_data_category_cloud; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_category
    ADD CONSTRAINT fk_basic_data_category_cloud FOREIGN KEY (cloud_id) REFERENCES public.t_sys_cloud(id);


--
-- Name: t_sys_basic_data_item fk_basic_data_item_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_item
    ADD CONSTRAINT fk_basic_data_item_category FOREIGN KEY (category_id) REFERENCES public.t_sys_basic_data_category(id);


--
-- Name: t_sys_basic_data_item fk_basic_data_item_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_basic_data_item
    ADD CONSTRAINT fk_basic_data_item_parent FOREIGN KEY (parent_id) REFERENCES public.t_sys_basic_data_item(id);


--
-- Name: t_scm_purchase_requisition fk_scm_purchase_requisition_applicant; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition
    ADD CONSTRAINT fk_scm_purchase_requisition_applicant FOREIGN KEY (applicant_id) REFERENCES public.t_sys_user(id);


--
-- Name: t_scm_purchase_requisition_entry fk_scm_purchase_requisition_entry_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition_entry
    ADD CONSTRAINT fk_scm_purchase_requisition_entry_parent FOREIGN KEY (parent_id) REFERENCES public.t_scm_purchase_requisition(id);


--
-- Name: t_scm_purchase_requisition fk_scm_purchase_requisition_org_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scm_purchase_requisition
    ADD CONSTRAINT fk_scm_purchase_requisition_org_id FOREIGN KEY (org_id) REFERENCES public.t_sys_org(id);


--
-- Name: t_sys_app fk_sys_app_cloud; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_app
    ADD CONSTRAINT fk_sys_app_cloud FOREIGN KEY (cloud_id) REFERENCES public.t_sys_cloud(id);


--
-- Name: t_sys_menu fk_sys_menu_app; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_menu
    ADD CONSTRAINT fk_sys_menu_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id);


--
-- Name: t_sys_menu fk_sys_menu_perm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_menu
    ADD CONSTRAINT fk_sys_menu_perm FOREIGN KEY (permission_id) REFERENCES public.t_sys_permission(id);


--
-- Name: t_sys_param fk_sys_param_app; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_param
    ADD CONSTRAINT fk_sys_param_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id);


--
-- Name: t_sys_permission fk_sys_perm_app; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_permission
    ADD CONSTRAINT fk_sys_perm_app FOREIGN KEY (app_id) REFERENCES public.t_sys_app(id);


--
-- Name: t_sys_role_perms fk_sys_rp_perm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_role_perms
    ADD CONSTRAINT fk_sys_rp_perm FOREIGN KEY (permission_id) REFERENCES public.t_sys_permission(id);


--
-- Name: t_sys_role_perms fk_sys_rp_role; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_role_perms
    ADD CONSTRAINT fk_sys_rp_role FOREIGN KEY (role_id) REFERENCES public.t_sys_role(id);


--
-- Name: t_sys_user_role fk_sys_ur_org; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user_role
    ADD CONSTRAINT fk_sys_ur_org FOREIGN KEY (org_id) REFERENCES public.t_sys_org(id);


--
-- Name: t_sys_user_role fk_sys_ur_role; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user_role
    ADD CONSTRAINT fk_sys_ur_role FOREIGN KEY (role_id) REFERENCES public.t_sys_role(id);


--
-- Name: t_sys_user_role fk_sys_ur_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_user_role
    ADD CONSTRAINT fk_sys_ur_user FOREIGN KEY (user_id) REFERENCES public.t_sys_user(id);


--
-- Name: t_sys_ui_config fk_ui_config_header_logo_attachment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_ui_config
    ADD CONSTRAINT fk_ui_config_header_logo_attachment FOREIGN KEY (header_logo_attachment_id) REFERENCES public.t_sys_attachment(id);


--
-- Name: t_sys_ui_config fk_ui_config_login_banner_attachment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_ui_config
    ADD CONSTRAINT fk_ui_config_login_banner_attachment FOREIGN KEY (login_banner_attachment_id) REFERENCES public.t_sys_attachment(id);


--
-- Name: t_sys_ui_config fk_ui_config_login_logo_attachment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_sys_ui_config
    ADD CONSTRAINT fk_ui_config_login_logo_attachment FOREIGN KEY (login_logo_attachment_id) REFERENCES public.t_sys_attachment(id);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_sched_name_trigger_name_trigger_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_sched_name_trigger_name_trigger_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_sched_name_trigger_name_trigger_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_sched_name_trigger_name_trigger_grou_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_sched_name_trigger_name_trigger_grou_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_sched_name_job_name_job_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_sched_name_job_name_job_group_fkey FOREIGN KEY (sched_name, job_name, job_group) REFERENCES public.qrtz_job_details(sched_name, job_name, job_group);


--
-- PostgreSQL database dump complete
--

CREATE TABLE public.t_sys_number_reference (
    id bigint NOT NULL,
    reference_key varchar(200) NOT NULL,
    feature_id bigint NOT NULL,
    name varchar(100) NOT NULL,
    default_rule_key varchar(200),
    system_preset boolean NOT NULL DEFAULT false,
    description varchar(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_number_reference PRIMARY KEY (id),
    CONSTRAINT uk_sys_number_reference_key UNIQUE (reference_key),
    CONSTRAINT fk_sys_number_reference_feature FOREIGN KEY (feature_id)
        REFERENCES public.t_sys_feature(id),
    CONSTRAINT fk_sys_number_reference_default_rule FOREIGN KEY (default_rule_key)
        REFERENCES public.t_sys_number_rule(rule_key)
);

COMMENT ON TABLE public.t_sys_number_reference IS '编号引用';
COMMENT ON COLUMN public.t_sys_number_reference.id IS 'ID';
COMMENT ON COLUMN public.t_sys_number_reference.reference_key IS '稳定编号引用键';
COMMENT ON COLUMN public.t_sys_number_reference.feature_id IS '所属功能ID';
COMMENT ON COLUMN public.t_sys_number_reference.name IS '名称';
COMMENT ON COLUMN public.t_sys_number_reference.default_rule_key IS '默认编号规则键';
COMMENT ON COLUMN public.t_sys_number_reference.system_preset IS '是否系统预置';
COMMENT ON COLUMN public.t_sys_number_reference.description IS '描述';
COMMENT ON COLUMN public.t_sys_number_reference.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_number_reference.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_number_reference.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_number_reference.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_number_reference.version IS '乐观锁版本号';

INSERT INTO public.t_sys_number_reference
    (id, reference_key, feature_id, name, default_rule_key, system_preset, description, version)
SELECT 461000000000000001, 'scm/procurement/purchase-requisition.number', a.id,
       '采购申请编号', 'scm/procurement/purchase-requisition', true, '采购申请业务编号', 0
FROM public.t_sys_feature a
WHERE a.feature_key = 'scm/procurement/purchase-requisition';

INSERT INTO public.t_sys_number_reference
    (id, reference_key, feature_id, name, default_rule_key, system_preset, description, version)
SELECT 461000000000000002, 'sys/base/basic-data-item.number', a.id,
       '基础资料编码', 'sys/base/basic-data-item', true, '基础资料节点编码', 0
FROM public.t_sys_feature a
WHERE a.feature_key = 'sys/base/basic-data';

ALTER TABLE public.t_sys_number_rule ADD COLUMN reference_key varchar(200);

UPDATE public.t_sys_number_rule
SET reference_key = 'scm/procurement/purchase-requisition.number'
WHERE rule_key = 'scm/procurement/purchase-requisition';

UPDATE public.t_sys_number_rule
SET reference_key = 'sys/base/basic-data-item.number'
WHERE rule_key = 'sys/base/basic-data-item';

ALTER TABLE public.t_sys_number_rule
    ALTER COLUMN reference_key SET NOT NULL,
    ADD CONSTRAINT fk_sys_number_rule_reference FOREIGN KEY (reference_key)
        REFERENCES public.t_sys_number_reference(reference_key);

COMMENT ON COLUMN public.t_sys_number_rule.reference_key IS '所属编号引用键';

CREATE INDEX idx_sys_number_rule_reference ON public.t_sys_number_rule(reference_key);

CREATE TABLE public.t_sys_number_rule_segment (
    id bigint NOT NULL,
    rule_key varchar(200) NOT NULL,
    sort integer NOT NULL,
    segment_type varchar(20) NOT NULL,
    value varchar(200),
    format varchar(20),
    length integer,
    separator varchar(10) NOT NULL DEFAULT '',
    CONSTRAINT pk_sys_number_rule_segment PRIMARY KEY (id),
    CONSTRAINT uk_sys_number_rule_segment_sort UNIQUE (rule_key, sort),
    CONSTRAINT fk_sys_number_rule_segment_rule FOREIGN KEY (rule_key)
        REFERENCES public.t_sys_number_rule(rule_key),
    CONSTRAINT ck_sys_number_rule_segment_type
        CHECK (segment_type IN ('FIXED', 'VARIABLE', 'DATE', 'SEQUENCE')),
    CONSTRAINT ck_sys_number_rule_segment_sort CHECK (sort > 0),
    CONSTRAINT ck_sys_number_rule_segment_length CHECK (length IS NULL OR length BETWEEN 1 AND 18)
);

COMMENT ON TABLE public.t_sys_number_rule_segment IS '编号规则格式段';
COMMENT ON COLUMN public.t_sys_number_rule_segment.id IS 'ID';
COMMENT ON COLUMN public.t_sys_number_rule_segment.rule_key IS '编号规则键';
COMMENT ON COLUMN public.t_sys_number_rule_segment.sort IS '顺序';
COMMENT ON COLUMN public.t_sys_number_rule_segment.segment_type IS '段类型';
COMMENT ON COLUMN public.t_sys_number_rule_segment.value IS '固定值或受控变量键';
COMMENT ON COLUMN public.t_sys_number_rule_segment.format IS '日期格式';
COMMENT ON COLUMN public.t_sys_number_rule_segment.length IS '流水位数';
COMMENT ON COLUMN public.t_sys_number_rule_segment.separator IS '段后分隔符';

INSERT INTO public.t_sys_number_rule_segment
    (id, rule_key, sort, segment_type, value, format, length, separator)
VALUES
    (461000000000000011, 'scm/procurement/purchase-requisition', 1, 'FIXED', 'PR', NULL, NULL, '-'),
    (461000000000000012, 'scm/procurement/purchase-requisition', 2, 'DATE', 'bill.bizDate', 'yyyyMMdd', NULL, '-'),
    (461000000000000013, 'scm/procurement/purchase-requisition', 3, 'SEQUENCE', NULL, NULL, 5, ''),
    (461000000000000021, 'sys/base/basic-data-item', 1, 'FIXED', 'BD', NULL, NULL, '-'),
    (461000000000000022, 'sys/base/basic-data-item', 2, 'SEQUENCE', NULL, NULL, 4, '');

UPDATE public.t_sys_number_rule
SET pattern = 'PR-{bill.bizDate:yyyyMMdd}-{seq:5}',
    description = '采购申请按组织、业务日期独立流水；格式可按需加入受控变量 org.number'
WHERE rule_key = 'scm/procurement/purchase-requisition';

UPDATE public.t_sys_number_rule
SET pattern = 'BD-{seq:4}'
WHERE rule_key = 'sys/base/basic-data-item';

INSERT INTO public.t_sys_permission (id, name, number, feature_id, version)
VALUES
    (461000000000000031, '编号规则-启用', 'sys:base:number-rule:enable', 460000000000000010, 0),
    (461000000000000032, '编号规则-停用', 'sys:base:number-rule:disable', 460000000000000010, 0);

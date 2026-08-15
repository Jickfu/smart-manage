CREATE TABLE public.t_sys_number_rule (
    id bigint NOT NULL,
    rule_key varchar(200) NOT NULL,
    name varchar(100) NOT NULL,
    pattern varchar(200) NOT NULL,
    scope_type varchar(20) NOT NULL,
    reset_period varchar(20) NOT NULL,
    start_value bigint NOT NULL DEFAULT 1,
    enabled boolean NOT NULL DEFAULT true,
    system_preset boolean NOT NULL DEFAULT false,
    description varchar(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    version integer NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_number_rule PRIMARY KEY (id),
    CONSTRAINT uk_sys_number_rule_key UNIQUE (rule_key),
    CONSTRAINT ck_sys_number_rule_scope CHECK (scope_type IN ('GLOBAL', 'ORG', 'CATEGORY')),
    CONSTRAINT ck_sys_number_rule_reset CHECK (reset_period IN ('NEVER', 'YEAR', 'MONTH', 'DAY')),
    CONSTRAINT ck_sys_number_rule_start CHECK (start_value BETWEEN 1 AND 2147483647)
);

COMMENT ON TABLE public.t_sys_number_rule IS '编号规则';
COMMENT ON COLUMN public.t_sys_number_rule.id IS 'ID';
COMMENT ON COLUMN public.t_sys_number_rule.rule_key IS '稳定规则键';
COMMENT ON COLUMN public.t_sys_number_rule.name IS '名称';
COMMENT ON COLUMN public.t_sys_number_rule.pattern IS '编号模板';
COMMENT ON COLUMN public.t_sys_number_rule.scope_type IS '流水作用域：GLOBAL全局、ORG组织、CATEGORY分类';
COMMENT ON COLUMN public.t_sys_number_rule.reset_period IS '重置周期：NEVER不重置、YEAR按年、MONTH按月、DAY按日';
COMMENT ON COLUMN public.t_sys_number_rule.start_value IS '新分段起始流水值';
COMMENT ON COLUMN public.t_sys_number_rule.enabled IS '启用状态';
COMMENT ON COLUMN public.t_sys_number_rule.system_preset IS '是否系统预置';
COMMENT ON COLUMN public.t_sys_number_rule.description IS '描述';
COMMENT ON COLUMN public.t_sys_number_rule.create_time IS '创建时间';
COMMENT ON COLUMN public.t_sys_number_rule.update_time IS '更新时间';
COMMENT ON COLUMN public.t_sys_number_rule.create_user IS '创建人';
COMMENT ON COLUMN public.t_sys_number_rule.update_user IS '修改人';
COMMENT ON COLUMN public.t_sys_number_rule.version IS '乐观锁版本号';

CREATE TABLE public.t_sys_number_counter (
    rule_key varchar(200) NOT NULL,
    scope_key varchar(100) NOT NULL,
    period_key varchar(20) NOT NULL,
    current_value bigint NOT NULL,
    update_time timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT pk_sys_number_counter PRIMARY KEY (rule_key, scope_key, period_key),
    CONSTRAINT fk_sys_number_counter_rule FOREIGN KEY (rule_key)
        REFERENCES public.t_sys_number_rule(rule_key),
    CONSTRAINT ck_sys_number_counter_value CHECK (current_value >= 1)
);

COMMENT ON TABLE public.t_sys_number_counter IS '编号流水计数器';
COMMENT ON COLUMN public.t_sys_number_counter.rule_key IS '稳定规则键';
COMMENT ON COLUMN public.t_sys_number_counter.scope_key IS '流水作用域键';
COMMENT ON COLUMN public.t_sys_number_counter.period_key IS '重置周期键';
COMMENT ON COLUMN public.t_sys_number_counter.current_value IS '当前流水值';
COMMENT ON COLUMN public.t_sys_number_counter.update_time IS '更新时间';

INSERT INTO public.t_sys_number_rule
    (id, rule_key, name, pattern, scope_type, reset_period, start_value, enabled,
     system_preset, description, version)
VALUES
    (460000000000000001, 'scm/procurement/purchase-requisition', '采购申请编号',
     'PR-{yyyyMMdd}-{seq:5}', 'ORG', 'DAY', 1, true, true,
     '采购申请按组织、业务日期独立流水；模板可按需加入 {orgNumber}', 0),
    (460000000000000002, 'sys/base/basic-data-item', '基础资料编号',
     'BD-{seq:4}', 'CATEGORY', 'NEVER', 1, true, true,
     '基础资料按分类独立流水', 0);

-- 采购申请按组织独立流水，编号文本是否包含组织编码由模板决定，因此唯一性也按组织约束。
ALTER TABLE public.t_scm_purchase_requisition
    DROP CONSTRAINT uk_scm_purchase_requisition_number,
    ADD CONSTRAINT uk_scm_purchase_requisition_org_number UNIQUE (org_id, number);

ALTER TABLE public.t_sys_basic_data_category
    ADD COLUMN number_mode varchar(20) NOT NULL DEFAULT 'AUTO_DEFAULT',
    ADD COLUMN number_rule_key varchar(200) NOT NULL DEFAULT 'sys/base/basic-data-item',
    ADD CONSTRAINT ck_basic_data_category_number_mode
        CHECK (number_mode IN ('MANUAL', 'AUTO_LOCKED', 'AUTO_DEFAULT')),
    ADD CONSTRAINT fk_basic_data_category_number_rule
        FOREIGN KEY (number_rule_key) REFERENCES public.t_sys_number_rule(rule_key);

COMMENT ON COLUMN public.t_sys_basic_data_category.number_mode IS
    '节点编号模式：MANUAL人工、AUTO_LOCKED自动锁定、AUTO_DEFAULT留空自动';
COMMENT ON COLUMN public.t_sys_basic_data_category.number_rule_key IS '节点编号规则键';

CREATE INDEX idx_basic_data_category_number_rule
    ON public.t_sys_basic_data_category(number_rule_key);

INSERT INTO public.t_sys_feature
    (id, feature_key, app_id, default_name, default_seq, description, visible, source, version)
VALUES
    (460000000000000010, 'sys/base/number-rule', 31, '编号规则', 45,
     '维护业务单据和主数据的编号模板、作用域与重置周期', true, 'SYSTEM', 0);

INSERT INTO public.t_sys_permission (id, name, number, feature_id, version)
VALUES
    (460000000000000011, '编号规则-查询', 'sys:base:number-rule:listPage', 460000000000000010, 0),
    (460000000000000012, '编号规则-详情', 'sys:base:number-rule:detail', 460000000000000010, 0),
    (460000000000000013, '编号规则-保存', 'sys:base:number-rule:save', 460000000000000010, 0),
    (460000000000000014, '编号规则-删除', 'sys:base:number-rule:delete', 460000000000000010, 0),
    (460000000000000015, '编号规则-选择', 'sys:base:number-rule:select', 460000000000000010, 0),
    (460000000000000016, '编号规则-预览', 'sys:base:number-rule:preview', 460000000000000010, 0);

INSERT INTO public.t_sys_menu
    (id, number, name, level, parent_id, app_id, feature_id, permission_id, path, component,
     icon, description, sort, enabled, version)
VALUES
    (460000000000000020, 'number_rule', '编号规则', 1, 3101, 31, 460000000000000010,
     460000000000000011, '/sys/base/number-rule', 'sys/base/number-rule', 'FieldNumberOutlined',
     '维护系统编号规则', 45, true, 0);

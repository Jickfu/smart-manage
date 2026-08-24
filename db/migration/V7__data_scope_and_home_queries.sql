ALTER TABLE t_sys_role
    ADD COLUMN default_data_scope varchar(32) NOT NULL DEFAULT 'SELF';

UPDATE t_sys_role SET default_data_scope = 'ALL' WHERE number = 'admin';

CREATE TABLE t_sys_role_data_scope (
    id bigint PRIMARY KEY,
    role_id bigint NOT NULL REFERENCES t_sys_role(id) ON DELETE CASCADE,
    resource_type varchar(128) NOT NULL,
    action varchar(32),
    scope_type varchar(32) NOT NULL,
    create_time timestamp,
    update_time timestamp,
    create_user bigint,
    update_user bigint,
    CONSTRAINT uk_role_data_scope_rule UNIQUE NULLS NOT DISTINCT (role_id, resource_type, action),
    CONSTRAINT ck_role_data_scope_type CHECK (scope_type IN ('ALL', 'ORG_AND_CHILDREN', 'ORG', 'SELF', 'CUSTOM_ORGS')),
    CONSTRAINT ck_role_data_scope_action CHECK (action IS NULL OR action ~ '^[A-Z][A-Z0-9_]*$')
);

CREATE TABLE t_sys_role_data_scope_org (
    id bigint PRIMARY KEY,
    scope_rule_id bigint NOT NULL REFERENCES t_sys_role_data_scope(id) ON DELETE CASCADE,
    org_id bigint NOT NULL REFERENCES t_sys_org(id),
    create_time timestamp,
    update_time timestamp,
    create_user bigint,
    update_user bigint,
    CONSTRAINT uk_role_data_scope_org UNIQUE (scope_rule_id, org_id)
);

CREATE INDEX idx_role_data_scope_role ON t_sys_role_data_scope(role_id);
CREATE INDEX idx_role_data_scope_org_org ON t_sys_role_data_scope_org(org_id);
CREATE INDEX idx_purchase_requisition_scope ON t_scm_purchase_requisition(org_id, applicant_id, create_time DESC);

INSERT INTO t_sys_permission (id, name, number, version, feature_id)
VALUES (470000000000000101, '角色管理-分配数据范围', 'sys:base:role:assignDataScopes', 0, 450000000000000013);
INSERT INTO t_sys_role_perms (id, role_id, permission_id)
VALUES (470000000000000102, 1, 470000000000000101);

BEGIN;

INSERT INTO t_sys_role (id, name, number, version, default_data_scope) VALUES
    (900000000000000001, 'DataScope测试SELF', 'verify-datascope-self', 0, 'SELF'),
    (900000000000000002, 'DataScope测试ORG', 'verify-datascope-org', 0, 'ORG');
INSERT INTO t_sys_user_role (id, user_id, org_id, role_id) VALUES
    (900000000000000011, 1, 1, 900000000000000001),
    (900000000000000012, 1, 1, 900000000000000002);

CREATE FUNCTION pg_temp.verify_effective_scopes(test_action text, expected text[]) RETURNS void AS $$
DECLARE actual text[];
BEGIN
    SELECT array_agg(scope_type ORDER BY role_id) INTO actual
    FROM (
        -- 与 RoleDataScopeMapper.selectEffectiveRules 保持相同的三级继承形状。
        SELECT a.id AS role_id, COALESCE(c.scope_type, b.scope_type, a.default_data_scope) AS scope_type
        FROM t_sys_role a
        JOIN t_sys_user_role d ON d.role_id = a.id
        LEFT JOIN t_sys_role_data_scope b ON b.role_id = a.id
            AND b.resource_type = 'scm.procurement.purchase-requisition' AND b.action IS NULL
        LEFT JOIN t_sys_role_data_scope c ON c.role_id = a.id
            AND c.resource_type = 'scm.procurement.purchase-requisition' AND c.action = test_action
        WHERE d.user_id = 1 AND d.org_id = 1 AND a.id >= 900000000000000001
    ) effective;
    IF actual IS DISTINCT FROM expected THEN
        RAISE EXCEPTION 'DataScope继承错误，action=%, expected=%, actual=%', test_action, expected, actual;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT pg_temp.verify_effective_scopes('VIEW', ARRAY['SELF', 'ORG']);

INSERT INTO t_sys_role_data_scope (id, role_id, resource_type, action, scope_type) VALUES
    (900000000000000021, 900000000000000001, 'scm.procurement.purchase-requisition', NULL, 'ORG');
SELECT pg_temp.verify_effective_scopes('VIEW', ARRAY['ORG', 'ORG']);

INSERT INTO t_sys_role_data_scope (id, role_id, resource_type, action, scope_type) VALUES
    (900000000000000022, 900000000000000001, 'scm.procurement.purchase-requisition', 'VIEW', 'ALL'),
    (900000000000000023, 900000000000000001, 'scm.procurement.purchase-requisition', 'SUBMIT', 'CUSTOM_ORGS');
INSERT INTO t_sys_role_data_scope_org (id, scope_rule_id, org_id)
VALUES (900000000000000031, 900000000000000023, 2087035439688040449);

SELECT pg_temp.verify_effective_scopes('VIEW', ARRAY['ALL', 'ORG']);
SELECT pg_temp.verify_effective_scopes('SAVE', ARRAY['ORG', 'ORG']);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM t_sys_role_data_scope_org
        WHERE scope_rule_id = 900000000000000023 AND org_id = 2087035439688040449
    ) THEN
        RAISE EXCEPTION 'CUSTOM_ORGS关系解析错误';
    END IF;
END $$;

ROLLBACK;

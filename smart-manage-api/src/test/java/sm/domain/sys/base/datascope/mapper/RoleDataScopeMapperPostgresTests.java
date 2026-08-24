package sm.domain.sys.base.datascope.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** 在 Flyway 空库验证中加载真实 Mapper XML，并对迁移后的 PostgreSQL 执行三级继承查询。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class RoleDataScopeMapperPostgresTests {
    private static final String RESOURCE_TYPE = "scm.procurement.purchase-requisition";
    private static final String MAPPER_RESOURCE = "mapper/sys/base/datascope/RoleDataScopeMapper.xml";
    private static final long SELF_ROLE_ID = 900000000000000001L;
    private static final long ORG_ROLE_ID = 900000000000000002L;
    private static final long CUSTOM_RULE_ID = 900000000000000023L;
    private static final long CUSTOM_ORG_ID = 2087035439688040449L;

    @Test
    void actualMapperImplementsRoleResourceAndActionInheritance() throws Exception {
        try (SqlSession session = sqlSessionFactory().openSession(false)) {
            seedFixtures(session);
            RoleDataScopeMapper mapper = session.getMapper(RoleDataScopeMapper.class);

            assertScopes(mapper.selectEffectiveRules(1L, 1L, RESOURCE_TYPE, "VIEW"),
                    Map.of(SELF_ROLE_ID, "SELF", ORG_ROLE_ID, "ORG"));

            execute(session, "INSERT INTO t_sys_role_data_scope "
                    + "(id, role_id, resource_type, action, scope_type) VALUES "
                    + "(900000000000000021, " + SELF_ROLE_ID + ", '" + RESOURCE_TYPE + "', NULL, 'ORG')");
            assertScopes(mapper.selectEffectiveRules(1L, 1L, RESOURCE_TYPE, "VIEW"),
                    Map.of(SELF_ROLE_ID, "ORG", ORG_ROLE_ID, "ORG"));

            execute(session, "INSERT INTO t_sys_role_data_scope "
                    + "(id, role_id, resource_type, action, scope_type) VALUES "
                    + "(900000000000000022, " + SELF_ROLE_ID + ", '" + RESOURCE_TYPE + "', 'VIEW', 'ALL'), "
                    + "(" + CUSTOM_RULE_ID + ", " + SELF_ROLE_ID + ", '" + RESOURCE_TYPE
                    + "', 'SUBMIT', 'CUSTOM_ORGS')");
            execute(session, "INSERT INTO t_sys_role_data_scope_org (id, scope_rule_id, org_id) VALUES "
                    + "(900000000000000031, " + CUSTOM_RULE_ID + ", " + CUSTOM_ORG_ID + ")");

            assertScopes(mapper.selectEffectiveRules(1L, 1L, RESOURCE_TYPE, "VIEW"),
                    Map.of(SELF_ROLE_ID, "ALL", ORG_ROLE_ID, "ORG"));
            assertScopes(mapper.selectEffectiveRules(1L, 1L, RESOURCE_TYPE, "SAVE"),
                    Map.of(SELF_ROLE_ID, "ORG", ORG_ROLE_ID, "ORG"));
            List<RoleDataScopeEntity> submitRules = mapper.selectEffectiveRules(1L, 1L, RESOURCE_TYPE, "SUBMIT");
            assertScopes(submitRules, Map.of(SELF_ROLE_ID, "CUSTOM_ORGS", ORG_ROLE_ID, "ORG"));
            assertEquals(CUSTOM_RULE_ID, ruleByRole(submitRules).get(SELF_ROLE_ID).getId());
            assertCustomOrgRelation(session);

            session.rollback();
        }
    }

    private SqlSessionFactory sqlSessionFactory() {
        String url = System.getProperty("smartManage.testDbUrl");
        String user = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        UnpooledDataSource dataSource = new UnpooledDataSource("org.postgresql.Driver", url, user, password);
        Environment environment = new Environment("postgres-integration", new JdbcTransactionFactory(), dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
        assertNotNull(mapperInput, "DataScope Mapper XML 不存在");
        new XMLMapperBuilder(mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments()).parse();
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private void seedFixtures(SqlSession session) throws Exception {
        execute(session, "INSERT INTO t_sys_role (id, name, number, version, default_data_scope) VALUES "
                + "(" + SELF_ROLE_ID + ", 'DataScope测试SELF', 'verify-datascope-self', 0, 'SELF'), "
                + "(" + ORG_ROLE_ID + ", 'DataScope测试ORG', 'verify-datascope-org', 0, 'ORG')");
        execute(session, "INSERT INTO t_sys_user_role (id, user_id, org_id, role_id) VALUES "
                + "(900000000000000011, 1, 1, " + SELF_ROLE_ID + "), "
                + "(900000000000000012, 1, 1, " + ORG_ROLE_ID + ")");
    }

    private void execute(SqlSession session, String sql) throws Exception {
        try (Statement statement = session.getConnection().createStatement()) {
            statement.executeUpdate(sql);
        }
        // 夹具在同一事务中直接写 JDBC；清理一级缓存，确保下一次调用真实执行 Mapper SQL。
        session.clearCache();
    }

    private void assertScopes(List<RoleDataScopeEntity> rules, Map<Long, String> expectedScopes) {
        Map<Long, RoleDataScopeEntity> rulesByRole = ruleByRole(rules);
        assertEquals(expectedScopes.keySet(), rulesByRole.keySet());
        for (Map.Entry<Long, String> expected : expectedScopes.entrySet()) {
            assertEquals(expected.getValue(), rulesByRole.get(expected.getKey()).getScopeType());
        }
    }

    private Map<Long, RoleDataScopeEntity> ruleByRole(List<RoleDataScopeEntity> rules) {
        return rules.stream().collect(Collectors.toMap(RoleDataScopeEntity::getRoleId, rule -> rule));
    }

    private void assertCustomOrgRelation(SqlSession session) throws Exception {
        try (PreparedStatement statement = session.getConnection().prepareStatement(
                "SELECT org_id FROM t_sys_role_data_scope_org WHERE scope_rule_id = ?")) {
            statement.setLong(1, CUSTOM_RULE_ID);
            try (var resultSet = statement.executeQuery()) {
                assertEquals(true, resultSet.next());
                assertEquals(CUSTOM_ORG_ID, resultSet.getLong(1));
            }
        }
    }
}

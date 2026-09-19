package sm.domain.sys.base.user.quicklaunch.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;
import sm.test.MapperXmlTestSupport;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserHomeQuickLaunchMapperXmlTests {
    private static final String MAPPER_RESOURCE =
            "mapper/sys/base/user/UserHomeQuickLaunchMapper.xml";

    @Test
    void ordinaryUserItemsAreFilteredByCurrentOrganizationPermission() {
        BoundSql boundSql = mappedStatement("selectCurrentUserItems", parameters(false, 20L));
        String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(normalizedSql.contains("f.user_id = ?"));
        assertTrue(normalizedSql.contains("f.org_id = ?"));
        assertTrue(normalizedSql.contains("a.app_id = ?"));
        assertTrue(normalizedSql.contains("b.app_id = ?"));
    }

    @Test
    void administratorSystemItemsUseNullApplicationScopeWithoutRoleJoin() {
        BoundSql boundSql = mappedStatement("selectCurrentUserItems", parameters(true, null));
        String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(normalizedSql.contains("a.app_id IS NULL"));
        assertFalse(normalizedSql.contains("t_sys_user_role"));
    }

    private BoundSql mappedStatement(String statement, Map<String, Object> parameters) {
        MybatisConfiguration configuration = MapperXmlTestSupport.load(MAPPER_RESOURCE);
        return configuration
                .getMappedStatement(UserHomeQuickLaunchMapper.class.getName() + "." + statement)
                .getBoundSql(parameters);
    }

    private Map<String, Object> parameters(boolean administrator, Long appId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", 10L);
        parameters.put("orgId", 30L);
        parameters.put("administrator", administrator);
        parameters.put("scope", appId == null ? HomeScopeEnum.SYSTEM : HomeScopeEnum.APPLICATION);
        parameters.put("appId", appId);
        return parameters;
    }
}

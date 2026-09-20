package sm.domain.sys.base.user.apppin.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;
import sm.test.MapperXmlTestSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAppPinMapperXmlTests {
	private static final String MAPPER_RESOURCE = "mapper/sys/base/user/UserAppPinMapper.xml";

	@Test
	void ordinaryUserPinsAreFilteredByCurrentOrganizationAccess() {
		MybatisConfiguration configuration = MapperXmlTestSupport.load(MAPPER_RESOURCE);
		BoundSql boundSql = configuration
				.getMappedStatement(UserAppPinMapper.class.getName() + ".selectUserPins")
				.getBoundSql(Map.of("userId", 10L, "orgId", 20L, "administrator", false));
		String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

		assertTrue(normalizedSql.contains("f.user_id = ?"));
		assertTrue(normalizedSql.contains("f.org_id = ?"));
	}

	@Test
	void administratorPinsDoNotDependOnRoleMenus() {
		MybatisConfiguration configuration = MapperXmlTestSupport.load(MAPPER_RESOURCE);
		BoundSql boundSql = configuration
				.getMappedStatement(UserAppPinMapper.class.getName() + ".selectUserPins")
				.getBoundSql(Map.of("userId", 10L, "orgId", 20L, "administrator", true));
		String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

		assertFalse(normalizedSql.contains("t_sys_user_role"));
		assertTrue(normalizedSql.contains("ORDER BY b.seq, b.id"));
	}

}

package sm.domain.sys.base.user.apppin.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAppPinMapperXmlTests {
	private static final String MAPPER_RESOURCE = "mapper/sys/base/user/UserAppPinMapper.xml";

	@Test
	void ordinaryUserPinsAreFilteredByCurrentOrganizationAccess() {
		MybatisConfiguration configuration = parseConfiguration();
		BoundSql boundSql = configuration
				.getMappedStatement(UserAppPinMapper.class.getName() + ".selectUserPins")
				.getBoundSql(Map.of("userId", 10L, "orgId", 20L, "administrator", false));
		String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

		assertTrue(normalizedSql.contains("f.user_id = ?"));
		assertTrue(normalizedSql.contains("f.org_id = ?"));
	}

	@Test
	void administratorPinsDoNotDependOnRoleMenus() {
		MybatisConfiguration configuration = parseConfiguration();
		BoundSql boundSql = configuration
				.getMappedStatement(UserAppPinMapper.class.getName() + ".selectUserPins")
				.getBoundSql(Map.of("userId", 10L, "orgId", 20L, "administrator", true));
		String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

		assertFalse(normalizedSql.contains("t_sys_user_role"));
		assertTrue(normalizedSql.contains("ORDER BY b.seq, b.id"));
	}

	private MybatisConfiguration parseConfiguration() {
		MybatisConfiguration configuration = new MybatisConfiguration();
		InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
		assertNotNull(mapperInput, "用户固定应用 Mapper XML 不存在");
		new XMLMapperBuilder(mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments())
				.parse();
		return configuration;
	}
}

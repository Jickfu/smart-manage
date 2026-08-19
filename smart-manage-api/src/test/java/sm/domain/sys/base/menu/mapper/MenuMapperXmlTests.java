package sm.domain.sys.base.menu.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuMapperXmlTests {
    private static final String MAPPER_RESOURCE = "mapper/sys/base/menu/MenuMapper.xml";

    @Test
    void userMenuQueryUsesCurrentMenuLevelCodes() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
        assertNotNull(mapperInput, "菜单 Mapper XML 不存在");
        new XMLMapperBuilder(
                mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments())
                .parse();

        BoundSql boundSql = configuration
                .getMappedStatement(MenuMapper.class.getName() + ".selectUserMenus")
                .getBoundSql(Map.of("userId", 1L, "orgId", 1L, "appId", 31L, "admin", true));
        String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(normalizedSql.contains("a.level IN (0, 1)"));
        assertFalse(normalizedSql.contains("a.level IN (2, 3)"));
    }

    @Test
    void fullColumnUpdateIncludesFeatureAndExternalNavigationFields() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
        assertNotNull(mapperInput, "菜单 Mapper XML 不存在");
        new XMLMapperBuilder(
                mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments())
                .parse();

        BoundSql boundSql = configuration
                .getMappedStatement(MenuMapper.class.getName() + ".updateAllColumns")
                .getBoundSql(new sm.domain.sys.base.menu.model.entity.MenuEntity());
        String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(normalizedSql.contains("feature_id = ?"));
        assertTrue(normalizedSql.contains("target_type = ?"));
        assertTrue(normalizedSql.contains("external_url = ?"));
        assertTrue(normalizedSql.contains("external_open_mode = ?"));
    }
}

package sm.domain.sys.base.user.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;
import sm.system.query.ListSqlQuery;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperXmlTests {
    private static final String MAPPER_RESOURCE = "mapper/sys/base/user/UserMapper.xml";
    private static final ListSqlQuery EMPTY_LIST_QUERY = new ListSqlQuery(List.of(), null, null);

    @Test
    void organizationScopeUsesExistsWithoutDuplicatingUsers() {
        MybatisConfiguration configuration = configuration();
        BoundSql boundSql = configuration.getMappedStatement(UserMapper.class.getName() + ".selectScopedPage")
                .getBoundSql(Map.of("keyword", "", "orgIds", List.of(10L, 11L), "unassigned", false,
                        "listQuery", EMPTY_LIST_QUERY));
        String sql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(sql.contains("EXISTS ( SELECT 1 FROM t_sys_user_assignment b"));
        assertTrue(sql.contains("b.org_id IN"));
    }

    @Test
    void unassignedScopeUsesNotExists() {
        MybatisConfiguration configuration = configuration();
        BoundSql boundSql = configuration.getMappedStatement(UserMapper.class.getName() + ".selectScopedPage")
                .getBoundSql(Map.of("keyword", "", "orgIds", List.of(), "unassigned", true,
                        "listQuery", EMPTY_LIST_QUERY));

        assertTrue(boundSql.getSql().replaceAll("\\s+", " ").contains("NOT EXISTS"));
    }

    @Test
    void cacheSnapshotQueryDoesNotSelectAuthenticationOrContactFields() {
        MybatisConfiguration configuration = configuration();
        BoundSql boundSql = configuration.getMappedStatement(UserMapper.class.getName() + ".selectCacheSnapshotById")
                .getBoundSql(Map.of("id", 1L));
        String sql = boundSql.getSql().replaceAll("\\s+", " ").toLowerCase();

        assertTrue(sql.contains("a.id"));
        assertTrue(sql.contains("a.username"));
        assertTrue(sql.contains("a.name"));
        assertTrue(sql.contains("a.avatar_attachment_id"));
        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("email"));
        assertFalse(sql.contains("phone"));
    }

    private MybatisConfiguration configuration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        String commonResource = "mapper/common/ListSqlQueryMapper.xml";
        InputStream commonInput = getClass().getClassLoader().getResourceAsStream(commonResource);
        assertNotNull(commonInput, "公共列表查询 Mapper XML 不存在");
        new XMLMapperBuilder(commonInput, configuration, commonResource, configuration.getSqlFragments()).parse();
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
        assertNotNull(mapperInput, "用户 Mapper XML 不存在");
        new XMLMapperBuilder(mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments()).parse();
        return configuration;
    }
}

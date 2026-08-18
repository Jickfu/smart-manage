package sm.system.query;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;
import sm.system.form.PageForm;
import sm.domain.sys.base.app.model.form.AppListForm;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ListQueryUtilTests {
    @Test
    void sqlQueryUsesServerWhitelistAndNormalizesDateRange() {
        AppListForm form = new AppListForm();
        form.setFilters("[{\"field\":\"createdAt\",\"type\":\"date\","
                + "\"operator\":\"BETWEEN\",\"values\":[\"2026-08-01\",\"2026-08-18\"]}]");
        form.setSortField("name");
        form.setSortOrder("DESC");
        ListSqlQuery query = ListSqlQuery.of(form, Map.of(
                "createdAt", ListSqlQuery.dateTime("a.create_time", false),
                "name", ListSqlQuery.string("a.name", true)));

        assertEquals("a.name", query.sortColumn());
        assertEquals("DESC", query.sortOrder());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), query.conditions().getFirst().begin());
        assertEquals(LocalDateTime.of(2026, 8, 19, 0, 0), query.conditions().getFirst().endExclusive());
    }

    @Test
    void rejectsUnknownClientFieldAndInvalidSortDirection() {
        PageForm unknownField = new PageForm();
        unknownField.setFilters("[{\"field\":\"name desc; drop table x\",\"type\":\"string\","
                + "\"operator\":\"EQ\",\"value\":\"x\"}]");
        assertThrows(BizException.class, () -> ListSqlQuery.of(unknownField,
                Map.of("name", ListSqlQuery.string("a.name", true))));

        PageForm invalidOrder = new PageForm();
        invalidOrder.setSortField("name");
        invalidOrder.setSortOrder("DESC NULLS FIRST");
        assertThrows(BizException.class, () -> ListSqlQuery.of(invalidOrder,
                Map.of("name", ListSqlQuery.string("a.name", true))));
    }

    @Test
    void sharedSqlFragmentResolvesForAllJoinMappers() throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        List<String> resources = List.of(
                "mapper/common/ListSqlQueryMapper.xml",
                "mapper/sys/base/app/AppMapper.xml",
                "mapper/sys/base/feature/FeatureMapper.xml",
                "mapper/sys/base/numberrule/NumberRuleMapper.xml",
                "mapper/sys/base/permission/PermissionMapper.xml",
                "mapper/sys/base/sysparam/SysParamMapper.xml",
                "mapper/sys/base/user/UserMapper.xml");
        for (String resource : resources) {
            try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
                assertNotNull(stream, resource);
                new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        assertTrue(configuration.hasStatement("sm.domain.sys.base.app.mapper.AppMapper.selectListPage"));
        AppListForm form = new AppListForm();
        form.setFilters("[{\"field\":\"name\",\"type\":\"string\",\"operator\":\"CONTAINS\",\"value\":\"系统\"}]");
        ListSqlQuery listQuery = ListSqlQuery.of(form,
                Map.of("name", ListSqlQuery.string("a.name", true)));
        String sql = configuration.getMappedStatement("sm.domain.sys.base.app.mapper.AppMapper.selectListPage")
                .getBoundSql(Map.of("form", form, "listQuery", listQuery)).getSql();
        assertTrue(sql.contains("a.name LIKE CONCAT"));
        assertFalse(sql.contains("${"));
    }
}

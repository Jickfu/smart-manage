package sm.domain.sys.monitor.sql.mapper;

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
import sm.domain.sys.monitor.sql.model.vo.SqlCatalogColumnVO;
import sm.domain.sys.monitor.sql.model.vo.SqlCatalogObjectVO;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 在 Flyway 空库验证中用真实 PostgreSQL 系统目录校验对象浏览查询。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SqlCatalogMapperPostgresTests {
    private static final String MAPPER_RESOURCE = "mapper/sys/monitor/sql/SqlCatalogMapper.xml";

    @Test
    void searchesCommentsAndReturnsRealColumnMetadataWithoutSystemSchemas() {
        try (SqlSession session = sqlSessionFactory().openSession()) {
            SqlCatalogMapper mapper = session.getMapper(SqlCatalogMapper.class);

            List<SqlCatalogObjectVO> tableCommentMatches = mapper.selectObjects("功能关联数据对象", 20);
            assertTrue(tableCommentMatches.stream()
                    .anyMatch(object -> "public".equals(object.getSchemaName())
                            && "t_sys_feature_data_object".equals(object.getObjectName())));

            List<SqlCatalogObjectVO> columnCommentMatches = mapper.selectObjects("所属功能ID", 20);
            assertTrue(columnCommentMatches.stream()
                    .anyMatch(object -> "t_sys_feature_data_object".equals(object.getObjectName())));

            List<SqlCatalogObjectVO> objects = mapper.selectObjects(null, 5000);
            assertFalse(objects.isEmpty());
            assertTrue(objects.stream().noneMatch(object -> object.getSchemaName().startsWith("pg_")));
            assertTrue(objects.stream().noneMatch(object -> "information_schema".equals(object.getSchemaName())));

            SqlCatalogObjectVO featureTable = mapper.selectObject("public", "t_sys_feature_data_object");
            assertNotNull(featureTable);
            assertEquals("TABLE", featureTable.getObjectType());

            SqlCatalogObjectVO loginLogTable = mapper.selectObject("public", "t_sys_login_log");
            assertNotNull(loginLogTable);
            assertEquals("PARTITIONED_TABLE", loginLogTable.getObjectType());

            List<SqlCatalogColumnVO> columns = mapper.selectColumns("public", "t_sys_feature_data_object");
            SqlCatalogColumnVO idColumn = columns.stream()
                    .filter(column -> "id".equals(column.getColumnName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Boolean.TRUE, idColumn.getPrimaryKey());
            assertEquals(Boolean.FALSE, idColumn.getNullable());
            assertEquals("ID", idColumn.getComment());
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
        assertNotNull(mapperInput, "SQL 目录 Mapper XML 不存在");
        new XMLMapperBuilder(mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments()).parse();
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}

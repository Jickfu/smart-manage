package sm.domain.sys.monitor.sql.service;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SqlConsoleTimeZonePostgresTests {
    @Test
    void restoresBeijingTimeBeforeAuditingAndReusingTheConnection() {
        try (var dataSource = new DruidDataSource()) {
            dataSource.setUrl(System.getProperty("smartManage.testDbUrl"));
            dataSource.setUsername(System.getProperty("smartManage.testDbUser"));
            dataSource.setPassword(System.getProperty("smartManage.testDbPassword"));
            dataSource.setMaxActive(1);
            var jdbc = new JdbcTemplate(dataSource);
            var transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
            var audit = mock(SqlLogMapper.class);
            when(audit.insert(any(SqlLogEntity.class))).thenAnswer(invocation -> {
                assertEquals("Asia/Shanghai", jdbc.queryForObject("SHOW TimeZone", String.class));
                return 1;
            });
            int backendPid = jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class);
            var service = new SqlExecutionTxService(dataSource, audit);
            var plan = SqlExecutionPlan.parse("SELECT set_config('TimeZone', 'UTC', false)");
            var logEntity = new SqlLogEntity();

            transaction.executeWithoutResult(status -> {
                var result = service.execute(plan, 10, logEntity);
                assertEquals("QUERY", result.getType());
                assertEquals("UTC", result.getRows().getFirst().getFirst());
            });

            verify(audit).insert(logEntity);
            assertEquals(backendPid, jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
            assertEquals("Asia/Shanghai", jdbc.queryForObject("SHOW TimeZone", String.class));
        }
    }
}

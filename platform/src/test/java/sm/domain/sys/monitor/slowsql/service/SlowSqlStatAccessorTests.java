package sm.domain.sys.monitor.slowsql.service;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.JdbcSqlStat;
import org.junit.jupiter.api.Test;
import sm.domain.sys.monitor.slowsql.model.vo.SlowSqlSnapshotVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlowSqlStatAccessorTests {
    @Test
    void snapshotOnlyReturnsSqlWhoseMaximumDurationReachesThreshold() {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        StatFilter filter = new StatFilter();
        filter.setSlowSqlMillis(1000);
        when(dataSource.getProxyFilters()).thenReturn(List.of(filter));
        JdbcSqlStat slow = new JdbcSqlStat("select * from slow_table where id = ?");
        slow.addExecuteTime(1_500_000_000L);
        JdbcSqlStat fast = new JdbcSqlStat("select 1");
        fast.addExecuteTime(10_000_000L);
        when(dataSource.getSqlStatMap()).thenReturn(new ConcurrentHashMap<>(Map.of(
                slow.getSql(), slow,
                fast.getSql(), fast)));

        SlowSqlSnapshotVO result = new SlowSqlStatAccessor(dataSource).snapshot("instance1");

        assertEquals(1, result.getRecords().size());
        assertEquals("select * from slow_table where id = ?", result.getRecords().getFirst().getSql());
        assertEquals(1000, result.getThresholdMs());
    }

    @Test
    void clearDoesNotResetPoolStatistics() {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        StatFilter filter = new StatFilter();
        when(dataSource.getProxyFilters()).thenReturn(List.of(filter));
        Map<String, JdbcSqlStat> statistics = new ConcurrentHashMap<>();
        statistics.put("select 1", new JdbcSqlStat("select 1"));
        when(dataSource.getSqlStatMap()).thenReturn(statistics);
        SlowSqlStatAccessor accessor = new SlowSqlStatAccessor(dataSource);

        SlowSqlSnapshotVO result = accessor.clear("instance1");

        assertTrue(result.getRecords().isEmpty());
        assertTrue(dataSource.getSqlStatMap().isEmpty());
    }
}

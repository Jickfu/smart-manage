package sm.domain.sys.monitor.slowsql.service;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.JdbcSqlStat;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.slowsql.model.vo.SlowSqlSnapshotVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** 读取和控制当前主数据源的 Druid SQL 内存统计。 */
@Component
class SlowSqlStatAccessor {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DruidDataSource dataSource;

    SlowSqlStatAccessor(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    SlowSqlSnapshotVO snapshot(String instanceId) {
        StatFilter statFilter = requireStatFilter();
        long thresholdMs = statFilter.getSlowSqlMillis();
        List<SlowSqlSnapshotVO.SqlStatVO> records = dataSource.getSqlStatMap().values().stream()
                .filter(stat -> stat.getExecuteMillisMax() >= thresholdMs)
                .sorted(Comparator.comparingLong(JdbcSqlStat::getExecuteMillisMax).reversed())
                .map(this::toVO)
                .toList();
        SlowSqlSnapshotVO result = new SlowSqlSnapshotVO();
        result.setInstanceId(instanceId);
        result.setSampleTime(TIME_FORMATTER.format(LocalDateTime.now()));
        result.setThresholdMs(thresholdMs);
        result.setRecords(records);
        return result;
    }

    SlowSqlSnapshotVO updateThreshold(String instanceId, long thresholdMs) {
        requireStatFilter().setSlowSqlMillis(thresholdMs);
        return snapshot(instanceId);
    }

    SlowSqlSnapshotVO clear(String instanceId) {
        // 只清理 SQL 维度统计，不重置连接池累计指标，避免影响运行监控的连接池语义。
        dataSource.getSqlStatMap().clear();
        return snapshot(instanceId);
    }

    private StatFilter requireStatFilter() {
        for (Filter filter : dataSource.getProxyFilters()) {
            if (filter instanceof StatFilter statFilter) {
                return statFilter;
            }
        }
        throw new BizException(ResultEnum.CONFIG_ERROR, "主数据源未启用 Druid StatFilter");
    }

    private SlowSqlSnapshotVO.SqlStatVO toVO(JdbcSqlStat stat) {
        SlowSqlSnapshotVO.SqlStatVO result = new SlowSqlSnapshotVO.SqlStatVO();
        result.setId(stat.getId());
        result.setSql(stat.getSql());
        result.setExecuteCount(stat.getExecuteCount());
        result.setExecuteSuccessCount(stat.getExecuteSuccessCount());
        result.setErrorCount(stat.getErrorCount());
        result.setExecuteMillisTotal(stat.getExecuteMillisTotal());
        result.setExecuteMillisMax(stat.getExecuteMillisMax());
        result.setExecuteMillisAverage(stat.getExecuteCount() == 0 ? 0D
                : (double) stat.getExecuteMillisTotal() / stat.getExecuteCount());
        result.setConcurrentMax(stat.getConcurrentMax());
        result.setInTransactionCount(stat.getInTransactionCount());
        result.setUpdateCount(stat.getUpdateCount());
        result.setFetchRowCount(stat.getFetchRowCount());
        result.setLastExecuteTime(formatTime(stat.getExecuteLastStartTime()));
        return result;
    }

    private String formatTime(Date time) {
        return time == null ? null : TIME_FORMATTER.format(time.toInstant().atZone(ZoneId.systemDefault()));
    }
}

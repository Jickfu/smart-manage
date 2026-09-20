package sm.domain.sys.scheduler.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统日志月分区生命周期执行器。
 * 表名只能来自固定白名单和受校验的 YYYYMM 后缀，禁止接受外部任意标识符。
 */
@Component
public class SystemLogPartitionLifecycleManager {
    private static final int PARTITION_WARNING_MONTHS = 6;
    private static final Pattern PARTITION_SUFFIX = Pattern.compile("^(?<parent>[a-z0-9_]+)_p(?<month>\\d{6})$");
    private static final String PARTITION_QUERY = """
            SELECT child.relname
            FROM pg_catalog.pg_inherits inheritance
            JOIN pg_catalog.pg_class parent ON parent.oid = inheritance.inhparent
            JOIN pg_catalog.pg_class child ON child.oid = inheritance.inhrelid
            WHERE parent.oid = pg_catalog.to_regclass(?)
            ORDER BY child.relname
            """;

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Autowired
    public SystemLogPartitionLifecycleManager(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.systemDefaultZone());
    }

    SystemLogPartitionLifecycleManager(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional(rollbackFor = Exception.class)
    public int archive(SystemLogLifecycleSettings settings) {
        int archived = 0;
        for (SystemLogPartitionDefinition definition : SystemLogPartitionDefinition.VALUES) {
            LocalDate cutoff = LocalDate.now(clock).minusDays(settings.retentionDays(definition));
            for (MonthlyPartition partition : monthlyPartitions(
                    definition.activeTable(), definition.activeTable())) {
                if (archived >= settings.maxPartitionsPerRun() || partition.end().atStartOfDay().isAfter(cutoff.atStartOfDay())) {
                    continue;
                }
                if (definition.protectUnsettledExecutions() && containsUnsettledExecution(partition.tableName())) {
                    continue;
                }
                jdbcTemplate.execute("ALTER TABLE public." + definition.activeTable()
                        + " DETACH PARTITION public." + partition.tableName());
                jdbcTemplate.execute("ALTER TABLE public." + definition.historyTable()
                        + " ATTACH PARTITION public." + partition.tableName()
                        + " FOR VALUES FROM ('" + partition.start() + "') TO ('" + partition.end() + "')");
                archived++;
            }
        }
        return archived;
    }

    @Transactional(rollbackFor = Exception.class)
    public int purge(SystemLogLifecycleSettings settings) {
        int purged = 0;
        for (SystemLogPartitionDefinition definition : SystemLogPartitionDefinition.VALUES) {
            LocalDate cutoff = LocalDate.now(clock).minusDays(settings.retentionDays(definition));
            // 物理分区转入历史父表后保留在线表前缀，避免重命名引入额外 DDL 锁。
            for (MonthlyPartition partition : monthlyPartitions(
                    definition.historyTable(), definition.activeTable())) {
                if (purged >= settings.maxPartitionsPerRun() || partition.end().atStartOfDay().isAfter(cutoff.atStartOfDay())) {
                    continue;
                }
                jdbcTemplate.execute("DROP TABLE public." + partition.tableName());
                purged++;
            }
        }
        return purged;
    }

    /** 返回未来分区不足六个月的在线父表编码。 */
    public List<String> partitionsRequiringExtension() {
        YearMonth requiredThrough = YearMonth.now(clock).plusMonths(PARTITION_WARNING_MONTHS);
        List<String> insufficientDefinitions = new ArrayList<>();
        for (SystemLogPartitionDefinition definition : SystemLogPartitionDefinition.VALUES) {
            YearMonth lastCoveredMonth = monthlyPartitions(
                    definition.activeTable(), definition.activeTable()).stream()
                    .map(MonthlyPartition::month)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            if (lastCoveredMonth == null || lastCoveredMonth.isBefore(requiredThrough)) {
                insufficientDefinitions.add(definition.parameterPrefix());
            }
        }
        return List.copyOf(insufficientDefinitions);
    }

    private boolean containsUnsettledExecution(String partitionTable) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM public." + partitionTable
                        + " WHERE status IN ('RUNNING', 'UNKNOWN'))",
                Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private List<MonthlyPartition> monthlyPartitions(String parentTable, String partitionNamePrefix) {
        return jdbcTemplate.queryForList(PARTITION_QUERY, String.class, "public." + parentTable).stream()
                .map(tableName -> parseMonthlyPartition(partitionNamePrefix, tableName))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(MonthlyPartition::month))
                .toList();
    }

    private MonthlyPartition parseMonthlyPartition(String expectedParent, String tableName) {
        Matcher matcher = PARTITION_SUFFIX.matcher(tableName);
        if (!matcher.matches() || !expectedParent.equals(matcher.group("parent"))) {
            return null;
        }
        String monthValue = matcher.group("month");
        YearMonth month = YearMonth.of(
                Integer.parseInt(monthValue.substring(0, 4)), Integer.parseInt(monthValue.substring(4, 6)));
        return new MonthlyPartition(tableName, month);
    }

    record MonthlyPartition(String tableName, YearMonth month) {
        LocalDate start() {
            return month.atDay(1);
        }

        LocalDate end() {
            return month.plusMonths(1).atDay(1);
        }
    }
}

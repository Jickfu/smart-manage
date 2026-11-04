package sm.domain.sys.scheduler.home.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.catalina.startup.Tomcat;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sm.domain.sys.scheduler.home.controller.SchedulerHomeController;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.model.vo.SchedulerDailyCountVO;
import sm.domain.sys.scheduler.model.vo.SchedulerSummaryVO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** 使用迁移后的临时库验证大数据量统计；耗时仅作观察，不设置易受机器负载影响的门限。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SchedulerHomePerformancePostgresTests {
    private static final LocalDateTime START = LocalDate.of(2026, 10, 29).atStartOfDay();
    private static final LocalDateTime END = START.plusDays(7);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-04T04:00:00Z"), ZoneOffset.UTC);
    private static final long FIRST_ID = 9500000000L;
    private static final int LOG_COUNT = 131250;
    private static final String AGGREGATE_ID = JobLogMapper.class.getName() + ".selectDailyCounts";
    private final QueryProbe probe = new QueryProbe();
    private final JsonMapper json = JsonMapper.builder().build();

    @TempDir
    Path tomcatDirectory;

    @Test
    void largeHistoryUsesPrunedAggregateAndKeepsSummarySemantics() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var jdbc = new JdbcTemplate(source);
        // 本测试包含提交后的 HTTP 跨线程读取，仅允许空库验证脚本创建的临时库。
        assertTrue(jdbc.queryForObject("SELECT current_database()", String.class).startsWith("smart_manage_verify_"));
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM t_sys_job_log", Long.class));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
        configuration.setCacheEnabled(false);
        configuration.addMapper(JobMapper.class);
        configuration.addMapper(JobLogMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(probe);
        factory.setMapperLocations(new ClassPathResource("mapper/sys/scheduler/JobLogMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        var jobs = session.getMapper(JobMapper.class);
        var logs = session.getMapper(JobLogMapper.class);
        var service = new SchedulerHomeService(jobs, logs) {
            @Override
            public SchedulerSummaryVO summary() {
                return summary(CLOCK);
            }
        };
        var statement = configuration.getMappedStatement(AGGREGATE_ID);
        assertEquals(SchedulerDailyCountVO.class, statement.getResultMaps().getFirst().getType());
        String aggregateSql = statement.getBoundSql(Map.of("start", START, "end", END)).getSql();
        try {
            seed(jdbc);
            System.out.printf("SCHEDULER_ENV postgres=%s java=%s rows=%d window=%s/%s%n",
                    jdbc.queryForObject("SHOW server_version", String.class), System.getProperty("java.version"),
                    LOG_COUNT, START, END);
            // 首次记录发生在夹具写入和 ANALYZE 后，并非磁盘冷缓存。
            benchmarkHttp(service);
            benchmarkMapper(logs, service);
            try (Connection connection = source.getConnection()) {
                System.out.printf("SCHEDULER_DRIVER %s%n", connection.getMetaData().getDriverVersion());
                for (int attempt = 1; attempt <= 3; attempt++) {
                    fetchJdbc(connection, "aggregate", aggregateSql, attempt, true);
                    fetchJdbc(connection, "legacy-detail", "SELECT * FROM t_sys_job_log WHERE start_time >= ?", attempt, false);
                }
            }
            for (int attempt = 1; attempt <= 3; attempt++) {
                var plan = json.readTree(jdbc.queryForObject(
                        "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + aggregateSql, String.class, START, END)).get(0);
                Set<String> relations = new HashSet<>();
                collectRelations(plan.get("Plan"), relations);
                Set<String> expected = new HashSet<>(jdbc.queryForList(
                        "SELECT DISTINCT tableoid::regclass::text FROM t_sys_job_log WHERE start_time >= ? AND start_time < ?",
                        String.class, START, END));
                assertEquals(2, expected.size());
                assertEquals(expected, relations, "执行计划只能访问窗口覆盖的两个月分区");
                System.out.printf("SCHEDULER_SQL attempt=%d planning_ms=%s execution_ms=%s partitions=%s%n",
                        attempt, plan.get("Planning Time"), plan.get("Execution Time"), relations);
            }
            var oldPlan = json.readTree(jdbc.queryForObject(
                    "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) SELECT * FROM t_sys_job_log WHERE start_time >= ?",
                    String.class, START)).get(0);
            Set<String> oldRelations = new HashSet<>();
            collectRelations(oldPlan.get("Plan"), oldRelations);
            assertTrue(oldRelations.size() > 2, "缺少上限的旧查询会访问更多分区");
            System.out.printf("SCHEDULER_LEGACY_SQL execution_ms=%s partitions=%d%n",
                    oldPlan.get("Execution Time"), oldRelations.size());
        } finally {
            // 只移除本次夹具；临时数据库整体生命周期仍由 verify-baseline.ps1 管理。
            jdbc.update("DELETE FROM t_sys_job_log WHERE id BETWEEN ? AND ?", FIRST_ID, FIRST_ID + LOG_COUNT + 2);
        }
    }

    private void seed(JdbcTemplate jdbc) {
        // 五个有数据日期、两个空日期、全部五种状态；失败正文为 2 KiB，避免只有窄行的失真测试。
        jdbc.update("""
                INSERT INTO t_sys_job_log
                    (id, job_id, job_name, start_time, status, error_message, trace_id,
                     domain_id, domain_name, app_id, app_name)
                SELECT ? + sequence_number, 9500000000, '性能验证任务',
                       timestamp '2026-10-29' + (ARRAY[0,1,3,5,6])[(sequence_number % 5)::int + 1] * interval '1 day',
                       (ARRAY['SUCCESS','FAILED','SKIPPED','RUNNING','UNKNOWN'])[(sequence_number / 5 % 5)::int + 1],
                       CASE WHEN sequence_number / 5 % 5 = 1 THEN repeat(md5(sequence_number::text), 64) ELSE NULL END,
                       md5(sequence_number::text), 9500000000, '测试领域', 9500000000, '测试应用'
                FROM generate_series(0, 131249) AS sequence_number
                """, FIRST_ID);
        // 窗口外 RUNNING 必须计入；排他上限处的失败记录不得计入今日和趋势。
        jdbc.update("""
                INSERT INTO t_sys_job_log
                    (id, job_name, start_time, status, domain_id, domain_name, app_id, app_name)
                VALUES (?, '下界外', ?, 'RUNNING', 9500000000, '测试领域', 9500000000, '测试应用'),
                       (?, '上界外', ?, 'FAILED', 9500000000, '测试领域', 9500000000, '测试应用'),
                       (?, '长期运行', timestamp '2025-01-01', 'RUNNING', 9500000000, '测试领域', 9500000000, '测试应用')
                """, FIRST_ID + LOG_COUNT, START.minusNanos(1000), FIRST_ID + LOG_COUNT + 1, END,
                FIRST_ID + LOG_COUNT + 2);
        jdbc.execute("ANALYZE t_sys_job_log");
    }

    private void benchmarkMapper(JobLogMapper logs, SchedulerHomeService service) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            long started = System.nanoTime();
            var rows = logs.selectDailyCounts(START, END);
            double elapsed = millisecondsSince(started);
            assertEquals(25, rows.size());
            assertEquals(LOG_COUNT, rows.stream().mapToLong(SchedulerDailyCountVO::getCount).sum());
            assertTrue(rows.stream().allMatch(row -> row.getCount() == 5250));
            System.out.printf("SCHEDULER_MAPPER aggregate attempt=%d elapsed_ms=%.3f rows=%d%n", attempt, elapsed, rows.size());
            probe.queries.clear();
            started = System.nanoTime();
            var summary = service.summary();
            elapsed = millisecondsSince(started);
            assertSummary(json.valueToTree(summary));
            assertSummaryQueries();
            System.out.printf("SCHEDULER_SERVICE attempt=%d elapsed_ms=%.3f%n", attempt, elapsed);
            started = System.nanoTime();
            var details = logs.selectList(new LambdaQueryWrapper<JobLogEntity>().ge(JobLogEntity::getStartTime, START));
            elapsed = millisecondsSince(started);
            assertEquals(LOG_COUNT + 1, details.size());
            assertEquals(2048, details.stream().filter(row -> row.getErrorMessage() != null).findFirst().orElseThrow().getErrorMessage().length());
            System.out.printf("SCHEDULER_MAPPER legacy-detail attempt=%d elapsed_ms=%.3f rows=%d%n", attempt, elapsed, details.size());
        }
    }

    private void benchmarkHttp(SchedulerHomeService service) throws Exception {
        var tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatDirectory.toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addContext("", tomcatDirectory.toString());
        context.setParentClassLoader(getClass().getClassLoader());
        var spring = new AnnotationConfigWebApplicationContext();
        spring.register(WebConfiguration.class);
        spring.addBeanFactoryPostProcessor(beanFactory -> beanFactory.registerSingleton(
                "schedulerHomeController", new SchedulerHomeController(service)));
        var servlet = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet(spring));
        servlet.setLoadOnStartup(1);
        context.addServletMappingDecoded("/", "dispatcher");
        try {
            tomcat.start();
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:"
                        + tomcat.getConnector().getLocalPort() + "/sys/scheduler/home/summary")).GET().build();
                for (int attempt = 1; attempt <= 3; attempt++) {
                    probe.queries.clear();
                    long started = System.nanoTime();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    double elapsed = millisecondsSince(started);
                    assertEquals(200, response.statusCode(), response.body());
                    var body = json.readTree(response.body());
                    assertEquals(0, body.get("code").asInt());
                    assertSummary(body.get("data"));
                    assertSummaryQueries();
                    System.out.printf("SCHEDULER_HTTP attempt=%d elapsed_ms=%.3f bytes=%d%n", attempt, elapsed,
                            response.body().getBytes(StandardCharsets.UTF_8).length);
                }
            }
        } finally {
            tomcat.stop();
            tomcat.destroy();
            spring.close();
        }
    }

    private void assertSummary(JsonNode summary) {
        assertEquals(26250, summary.get("todayExecutions").asLong());
        assertEquals(5250, summary.get("todayFailedExecutions").asLong());
        assertEquals(26252, summary.get("runningExecutions").asLong());
        assertEquals(summary.get("totalJobs").asLong() - summary.get("enabledJobs").asLong(),
                summary.get("pausedJobs").asLong());
        var trends = summary.get("trends");
        assertEquals(7, trends.size());
        for (int offset = 0; offset < 7; offset++) {
            var trend = trends.get(offset);
            assertEquals(START.toLocalDate().plusDays(offset).toString(), trend.get("date").asString());
            long expected = offset == 2 || offset == 4 ? 0 : 5250;
            for (String state : List.of("success", "failed", "skipped")) {
                assertEquals(expected, trend.get(state).asLong());
            }
        }
    }

    private void assertSummaryQueries() {
        assertEquals(4, probe.queries.size(), "每次 summary 应只有一次聚合与三次计数");
        assertEquals(1, probe.queries.stream().filter(query -> query.identifier().equals(AGGREGATE_ID)).count());
        assertEquals(2, probe.queries.stream().filter(query -> query.identifier().equals(JobMapper.class.getName() + ".selectCount")).count());
        var running = probe.queries.stream().filter(query -> query.identifier().equals(JobLogMapper.class.getName() + ".selectCount")).findFirst().orElseThrow();
        assertFalse(running.sql().contains("start_time"), "正在执行统计不受七天时间窗口约束");
        assertTrue(probe.queries.stream().allMatch(query -> query.resultTypes().stream()
                .allMatch(type -> type == Long.class || type == SchedulerDailyCountVO.class)), "应用只能接收计数与聚合投影");
    }

    private void fetchJdbc(Connection connection, String label, String sql, int attempt, boolean aggregate) throws Exception {
        long started = System.nanoTime();
        int rowCount = 0;
        long fieldBytes = 0;
        try (var query = connection.prepareStatement(sql)) {
            query.setObject(1, START);
            if (aggregate) {
                query.setObject(2, END);
            }
            try (var rows = query.executeQuery()) {
                int columns = rows.getMetaData().getColumnCount();
                while (rows.next()) {
                    rowCount++;
                    for (int column = 1; column <= columns; column++) {
                        String value = rows.getString(column);
                        if (value != null) {
                            fieldBytes += value.getBytes(StandardCharsets.UTF_8).length;
                        }
                    }
                }
            }
        }
        double elapsed = millisecondsSince(started);
        assertEquals(aggregate ? 25 : LOG_COUNT + 1, rowCount);
        System.out.printf("SCHEDULER_JDBC %s attempt=%d elapsed_ms=%.3f rows=%d field_utf8_bytes=%d%n",
                label, attempt, elapsed, rowCount, fieldBytes);
    }

    private void collectRelations(JsonNode plan, Set<String> relations) {
        if (plan.has("Relation Name")) {
            relations.add(plan.get("Relation Name").asString());
        }
        if (plan.has("Plans")) {
            for (JsonNode child : plan.get("Plans")) {
                collectRelations(child, relations);
            }
        }
    }

    private static double millisecondsSince(long started) {
        return (System.nanoTime() - started) / 1_000_000.0;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebConfiguration {
    }

    private record QueryObservation(String identifier, String sql, List<Class<?>> resultTypes) {
    }

    /** 在真实 MyBatis Executor 边界观察返回类型，避免只靠 Mock 声明没有读取实体。 */
    @Intercepts(@Signature(type = Executor.class, method = "query", args = {
            MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
    private static class QueryProbe implements Interceptor {
        private final List<QueryObservation> queries = new CopyOnWriteArrayList<>();

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            var statement = (MappedStatement) invocation.getArgs()[0];
            var result = (List<?>) invocation.proceed();
            var resultTypes = new ArrayList<Class<?>>();
            result.stream().map(Object::getClass).distinct().forEach(resultTypes::add);
            queries.add(new QueryObservation(statement.getId(), statement.getBoundSql(invocation.getArgs()[1]).getSql(), resultTypes));
            return result;
        }
    }
}

package sm.domain.sys.scheduler.singleton.service;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

/** 真实 PostgreSQL 和独立 JVM 验证：唯一执行、强杀接管、正常下线、共享停止与退避。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SingletonFailoverPostgresTests {
    private JdbcTemplate jdbc;
    private String number;
    private final List<Process> processes = new ArrayList<>();
    @BeforeEach void prepare() {
        String url = System.getProperty("smartManage.testDbUrl");
        assertTrue(url.contains("/smart_manage_verify_"), "只允许临时验证数据库");
        jdbc = new JdbcTemplate(new DriverManagerDataSource(url, System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword")));
        jdbc.execute("CREATE TABLE IF NOT EXISTS t_singleton_probe_test(id bigserial PRIMARY KEY, task_number text, instance_id text, run_id text, generation bigint)");
        number = "test:" + UUID.randomUUID();
    }
    @AfterEach void cleanup() throws Exception {
        for (Process process : processes) {
            if (process.isAlive()) process.destroyForcibly();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        }
        if (jdbc != null) {
            jdbc.update("DELETE FROM t_singleton_probe_test WHERE task_number=?", number);
            jdbc.update("DELETE FROM t_sys_singleton_event WHERE task_number=?", number);
            jdbc.update("DELETE FROM t_sys_singleton_task WHERE number=?", number);
        }
    }
    @Test void crashTransfersLoopOwnershipWithoutOldGenerationWrites() throws Exception { verifyFailover("loop", true); }
    @Test void gracefulShutdownTransfersListenerOwnership() throws Exception { verifyFailover("listener", false); }
    @Test void coordinationSocketTimeoutCannotBeDisabledByApplicationUrl() throws Exception {
        String url = System.getProperty("smartManage.testDbUrl");
        var properties = new org.springframework.boot.jdbc.autoconfigure.DataSourceProperties();
        properties.setUrl(url + (url.contains("?") ? "&" : "?")
                + "connectTimeout=0&socketTimeout=0&tcpKeepAlive=false");
        properties.setUsername(System.getProperty("smartManage.testDbUser"));
        properties.setPassword(System.getProperty("smartManage.testDbPassword"));
        var factory = new sm.system.concurrent.PostgresSessionFactory(properties);
        try (var connection = factory.open()) {
            assertEquals(5000, connection.getNetworkTimeout(), "业务 URL 不能禁用协调连接的读超时");
        }
    }
    @Test void stoppingAFailedRunRecordsItsCommittedStateWithoutRetry() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var accessor = new SingletonTaskAccessor(source);
        accessor.register(new sm.domain.sys.scheduler.singleton.contract.SingletonTaskDefinition(
                number, "停止与失败并发", "scheduler", "测试", true, true, 8));
        var transaction = new org.springframework.transaction.support.TransactionTemplate(
                new org.springframework.jdbc.datasource.DataSourceTransactionManager(source));
        try (var lease = SingletonLease.acquire(source.getConnection(), number, "probe-a")) {
            assertNotNull(lease);
            assertTrue(lease.claim(UUID.randomUUID().toString()));
            transaction.executeWithoutResult(status -> assertEquals(1, accessor.control(number, 0, false)));
            lease.finish(true, 8, "TASK_FAILED");
            assertEquals("STOPPED", accessor.find(number).getState());
            assertNull(accessor.find(number).getRetryAt());
            assertEquals("STOPPED", accessor.events(number).getFirst().eventType());
        }
    }
    private void verifyFailover(String mode, boolean crash) throws Exception {
        Process first = start("probe-a", mode);
        await(() -> count() >= 3);
        Process second = start("probe-b", mode);
        await(() -> processes.stream().allMatch(Process::isAlive));
        Thread.sleep(2500);
        assertEquals(1, jdbc.queryForObject("SELECT count(DISTINCT instance_id) FROM t_singleton_probe_test WHERE task_number=?", Integer.class, number));
        assertEquals("probe-a", owner());
        long before = System.nanoTime();
        if (crash) first.destroyForcibly();
        else { first.getOutputStream().write("stop\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)); first.getOutputStream().flush(); }
        assertTrue(first.waitFor(15, TimeUnit.SECONDS));
        await(() -> "probe-b".equals(owner()) && jdbc.queryForObject("SELECT count(*) FROM t_singleton_probe_test WHERE task_number=? AND instance_id='probe-b'", Integer.class, number) >= 3);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - before);
        assertEquals(0, jdbc.queryForObject("""
            SELECT count(*) FROM t_singleton_probe_test old_run WHERE task_number=? AND generation=1
            AND id > (SELECT min(id) FROM t_singleton_probe_test WHERE task_number=? AND generation=2)
            """, Integer.class, number, number));
        // 在父进程模拟命中任意节点的事务命令；执行节点从共享状态收敛停止。
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"), System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var transaction = new org.springframework.transaction.support.TransactionTemplate(new org.springframework.jdbc.datasource.DataSourceTransactionManager(source));
        var accessor = new SingletonTaskAccessor(source);
        transaction.executeWithoutResult(status -> assertEquals(1, accessor.control(number, 0, false)));
        await(() -> "STOPPED".equals(jdbc.queryForObject("SELECT state FROM t_sys_singleton_task WHERE number=?", String.class, number)) && owner() == null);
        int stoppedCount = count();
        Thread.sleep(2500);
        assertEquals(stoppedCount, count());
        assertEquals(0, accessor.control(number, 0, true), "旧版本不能重新启动");
        System.out.println("SINGLETON_FAILOVER mode=" + mode + " crash=" + crash + " takeoverMs=" + elapsed + " generations=2 stoppedWrites=" + stoppedCount);
        assertTrue(second.isAlive());
    }
    @Test void failureBackoffIsSharedAcrossProcesses() throws Exception {
        start("probe-a", "failure");
        await(() -> "FAILED".equals(jdbc.queryForObject("SELECT state FROM t_sys_singleton_task WHERE number=?", String.class, number)));
        start("probe-b", "failure");
        Thread.sleep(2500);
        assertEquals(1L, jdbc.queryForObject("SELECT generation FROM t_sys_singleton_task WHERE number=?", Long.class, number));
        assertNotNull(jdbc.queryForObject("SELECT retry_at FROM t_sys_singleton_task WHERE number=?", java.sql.Timestamp.class, number));
    }
    @Test void lostDatabaseSessionRejectsOldRunAndControlRollbackIsAtomic() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"), System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var accessor = new SingletonTaskAccessor(source);
        accessor.register(new sm.domain.sys.scheduler.singleton.contract.SingletonTaskDefinition(number, "测试", "scheduler", "测试", true, true, 8));
        var transaction = new org.springframework.transaction.support.TransactionTemplate(new org.springframework.jdbc.datasource.DataSourceTransactionManager(source));
        try (var connection = source.getConnection(); var first = SingletonLease.acquire(connection, number, "probe-a")) {
            assertNotNull(first);
            String firstRun = UUID.randomUUID().toString();
            assertTrue(first.claim(firstRun));
            var oldContext = new SingletonRunContext(number, firstRun, first.generation(), accessor);
            assertThrows(IllegalStateException.class, oldContext::guardTransaction);
            transaction.executeWithoutResult(status -> oldContext.guardTransaction());
            int backend = SingletonTaskAccessor.session(connection).queryForObject("SELECT pg_backend_pid()", Integer.class);
            assertEquals(true, jdbc.queryForObject("SELECT pg_terminate_backend(?)", Boolean.class, backend));
            try (var second = SingletonLease.acquire(source.getConnection(), number, "probe-b")) {
                assertNotNull(second);
                assertTrue(second.claim(UUID.randomUUID().toString()));
                assertEquals(2, second.generation());
                assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
                    new JdbcTemplate(source).update("INSERT INTO t_singleton_probe_test(task_number) VALUES (?)", number);
                    oldContext.guardTransaction();
                }));
                assertEquals(0, count(), "失效代际应回滚同事务的写入");
                assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(status -> {
                    assertEquals(1, accessor.control(number, 0, false));
                    throw new IllegalArgumentException("模拟后续业务失败");
                }));
                assertTrue(accessor.find(number).getRequested());
                assertEquals(0, accessor.find(number).getVersion());
                assertTrue(accessor.events(number).stream().noneMatch(event -> event.eventType().equals("STOP_REQUESTED")));
                jdbc.update("UPDATE t_sys_singleton_task SET heartbeat_at=localtimestamp-interval '1 minute' WHERE number=?", number);
                assertEquals("UNKNOWN", accessor.find(number).getState());
                second.finish(false, 8, null);
            }
        }
    }

    private Process start(String instance, String mode) throws Exception {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        if (!java.nio.file.Files.exists(Path.of(javaExecutable))) javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var builder = new ProcessBuilder(javaExecutable, "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")), SingletonProbeProcess.class.getName());
        builder.environment().put("SM_PROBE_URL", System.getProperty("smartManage.testDbUrl"));
        builder.environment().put("SM_PROBE_USER", System.getProperty("smartManage.testDbUser"));
        builder.environment().put("SM_PROBE_PASSWORD", System.getProperty("smartManage.testDbPassword"));
        builder.environment().put("SM_PROBE_NUMBER", number);
        builder.environment().put("SM_PROBE_INSTANCE", instance);
        builder.environment().put("SM_PROBE_MODE", mode);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        processes.add(process);
        return process;
    }
    private int count() { return jdbc.queryForObject("SELECT count(*) FROM t_singleton_probe_test WHERE task_number=?", Integer.class, number); }
    private String owner() {
        var values = jdbc.queryForList("SELECT owner_instance_id FROM t_sys_singleton_task WHERE number=?", String.class, number);
        return values.isEmpty() ? null : values.getFirst();
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(25).toNanos();
        while (System.nanoTime() < deadline) {
            try { if (condition.getAsBoolean()) return; }
            catch (org.springframework.dao.EmptyResultDataAccessException notReady) { /* 等待子进程注册。 */ }
            Thread.sleep(100);
        }
        fail("双实例状态未在期限内收敛");
    }
}

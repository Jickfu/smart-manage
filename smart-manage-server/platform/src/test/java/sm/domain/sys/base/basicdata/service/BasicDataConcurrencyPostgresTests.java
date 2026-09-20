package sm.domain.sys.base.basicdata.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 使用真实 PostgreSQL 行锁验证同一分类下的树结构写入会串行执行。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class BasicDataConcurrencyPostgresTests {
    private static final long CATEGORY_ID = -9300001L;
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private BasicDataCategoryMapper categoryMapper;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var manager = new DataSourceTransactionManager(source);
        transaction = new TransactionTemplate(manager);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(BasicDataCategoryMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var session = new SqlSessionTemplate(factory.getObject());
        categoryMapper = session.getMapper(BasicDataCategoryMapper.class);
        Long domainId = jdbc.queryForObject("SELECT id FROM t_sys_domain ORDER BY id LIMIT 1", Long.class);
        jdbc.update("INSERT INTO t_sys_basic_data_category(id, number, name, domain_id) VALUES (?, ?, ?, ?)",
                CATEGORY_ID, "verify-basic-data-lock", "基础数据并发测试", domainId);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM t_sys_basic_data_category WHERE id=?", CATEGORY_ID);
    }

    @Test
    void categoryRowSerializesTreeWriters() throws Exception {
        var firstLocked = new CountDownLatch(1);
        var allowCommit = new CountDownLatch(1);
        var secondBackend = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                categoryMapper.selectForUpdate(CATEGORY_ID);
                firstLocked.countDown();
                await(allowCommit);
            }));
            assertTrue(firstLocked.await(10, TimeUnit.SECONDS));
            var second = executor.submit(() -> transaction.executeWithoutResult(status -> {
                secondBackend.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                categoryMapper.selectForUpdate(CATEGORY_ID);
            }));

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            boolean blocked = false;
            while (System.nanoTime() < deadline && !second.isDone()) {
                blocked = Boolean.TRUE.equals(jdbc.queryForObject(
                        "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, secondBackend.get()));
                if (blocked) break;
                Thread.sleep(10);
            }
            assertTrue(blocked, "同一分类的第二个树写事务必须等待分类行锁");
            allowCommit.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            allowCommit.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("并发测试等待超时");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发测试被中断", exception);
        }
    }
}

package sm.domain.sys.base.attachment.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.exception.BizException;
import sm.system.resource.BusinessResourceRegistry;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 真实附件表、映射表和事务竞争，覆盖读取临时归属后并发提交的窗口。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class AttachmentConcurrencyPostgresTests {
    private static final long ATTACHMENT_ID = -9400001L;
    private static final long MAPPING_ID = -9400002L;
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private AttachmentMapper mapper;
    private BizAttachmentMapper bizMapper;
    private BusinessResourceRegistry resources;
    private AttachmentTxService service;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(AttachmentMapper.class);
        configuration.addMapper(BizAttachmentMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var session = new SqlSessionTemplate(factory.getObject());
        mapper = session.getMapper(AttachmentMapper.class);
        bizMapper = session.getMapper(BizAttachmentMapper.class);
        // 通用平台测试用独立业务行模拟聚合冻结策略，不依赖任何可选领域。
        jdbc.execute("CREATE TABLE verify_attachment_document (id text PRIMARY KEY, frozen boolean NOT NULL)");
        jdbc.update("INSERT INTO verify_attachment_document VALUES ('100', false)");
        jdbc.update("INSERT INTO t_sys_attachment(id, original_name, storage_type, object_key, status) VALUES (?, 'verify.txt', 'LOCAL', 'verify/attachment.txt', 'TEMP')", ATTACHMENT_ID);
        jdbc.update("INSERT INTO t_sys_biz_attachment(id, biz_type, attachment_id, remark) VALUES (?, 'verify-document', ?, '原备注')", MAPPING_ID, ATTACHMENT_ID);
        resources = mock(BusinessResourceRegistry.class);
        doAnswer(invocation -> {
            String businessId = invocation.getArgument(1);
            if (businessId != null && Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT frozen FROM verify_attachment_document WHERE id=? FOR UPDATE", Boolean.class, businessId))) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "单据已冻结");
            }
            return null;
        }).when(resources).beforeAttachmentMutation(anyString(), nullable(String.class), anyLong(), any());
        service = new AttachmentTxService(mapper, bizMapper, resources);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM t_sys_biz_attachment WHERE id=?", MAPPING_ID);
        jdbc.update("DELETE FROM t_sys_attachment WHERE id=?", ATTACHMENT_ID);
        jdbc.execute("DROP TABLE IF EXISTS verify_attachment_document");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void promotionAfterTemporaryOwnershipReadRejectsDeleteAndRemark(boolean deleting) throws Exception {
        var reachedLock = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        var delayed = new AttachmentTxService(delayedMapper(reachedLock, resume), bizMapper, resources);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var mutation = executor.submit(() -> assertThrows(BizException.class, () -> transaction.executeWithoutResult(status -> {
                if (deleting) delayed.markPendingDelete(ATTACHMENT_ID);
                else delayed.updateRemark(MAPPING_ID, ATTACHMENT_ID, "不应写入");
            })));
            assertTrue(reachedLock.await(10, TimeUnit.SECONDS));
            transaction.executeWithoutResult(status -> {
                promote(service);
                jdbc.update("UPDATE verify_attachment_document SET frozen=true WHERE id='100'");
            });
            resume.countDown();
            assertTrue(mutation.get(10, TimeUnit.SECONDS).getMessage().contains("附件归属已变化"));
            assertEquals("ACTIVE", mapper.selectById(ATTACHMENT_ID).getStatus());
            assertEquals("100", bizMapper.selectById(MAPPING_ID).getBizId());
            assertEquals("原备注", bizMapper.selectById(MAPPING_ID).getRemark());
            // 重新请求也必须执行正式归属的冻结校验。
            assertThrows(BizException.class, () -> transaction.executeWithoutResult(status -> {
                if (deleting) service.markPendingDelete(ATTACHMENT_ID);
                else service.updateRemark(MAPPING_ID, ATTACHMENT_ID, "仍不可修改");
            }));
        } finally {
            resume.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    @Test
    void deletionAfterPromotionPreReadPreventsResurrection() throws Exception {
        var reachedLock = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        var delayed = new AttachmentTxService(delayedMapper(reachedLock, resume), bizMapper, resources);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var promotion = executor.submit(() -> assertThrows(BizException.class,
                    () -> transaction.executeWithoutResult(status -> promote(delayed))));
            assertTrue(reachedLock.await(10, TimeUnit.SECONDS));
            transaction.executeWithoutResult(status -> service.markPendingDelete(ATTACHMENT_ID));
            resume.countDown();
            assertTrue(promotion.get(10, TimeUnit.SECONDS).getMessage().contains("附件不可用"));
            assertEquals("PENDING_DELETE", mapper.selectById(ATTACHMENT_ID).getStatus());
            assertNull(bizMapper.selectById(MAPPING_ID));
            assertFalse(jdbc.queryForObject("SELECT frozen FROM verify_attachment_document WHERE id='100'", Boolean.class));
        } finally {
            resume.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private AttachmentMapper delayedMapper(CountDownLatch reachedLock, CountDownLatch resume) {
        var delayed = mock(AttachmentMapper.class, invocation -> invocation.getMethod().invoke(mapper, invocation.getArguments()));
        doAnswer(invocation -> {
            reachedLock.countDown();
            if (!resume.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("附件竞争测试等待超时");
            return mapper.selectForUpdate(ATTACHMENT_ID);
        }).when(delayed).selectForUpdate(ATTACHMENT_ID);
        return delayed;
    }

    @Test
    void deletionWaitsForAttachmentRowLockAndRechecksCommittedOwnership() throws Exception {
        var locked = new CountDownLatch(1);
        var commit = new CountDownLatch(1);
        var deletingBackend = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(2);
        try {
            var promotion = executor.submit(() -> transaction.executeWithoutResult(status -> {
                jdbc.queryForObject("SELECT frozen FROM verify_attachment_document WHERE id='100' FOR UPDATE", Boolean.class);
                mapper.selectForUpdate(ATTACHMENT_ID);
                locked.countDown();
                try {
                    if (!commit.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("等待提交超时");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                promote(service);
                jdbc.update("UPDATE verify_attachment_document SET frozen=true WHERE id='100'");
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var deletion = executor.submit(() -> assertThrows(BizException.class, () -> transaction.executeWithoutResult(status -> {
                deletingBackend.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                service.markPendingDelete(ATTACHMENT_ID);
            })));
            boolean blocked = false;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (System.nanoTime() < deadline && !deletion.isDone()) {
                blocked = Boolean.TRUE.equals(jdbc.queryForObject("SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, deletingBackend.get()));
                if (blocked) break;
                Thread.sleep(10);
            }
            assertTrue(blocked, "删除必须等待正在提交的附件行锁");
            commit.countDown();
            promotion.get(10, TimeUnit.SECONDS);
            assertTrue(deletion.get(10, TimeUnit.SECONDS).getMessage().contains("附件归属已变化"));
            assertEquals("ACTIVE", mapper.selectById(ATTACHMENT_ID).getStatus());
            assertEquals("100", bizMapper.selectById(MAPPING_ID).getBizId());
        } finally {
            commit.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private void promote(AttachmentTxService target) {
        var command = new AttachmentPromoteCommand();
        command.setAttachmentIds(List.of(ATTACHMENT_ID));
        command.setBizType("verify-document");
        command.setBizId("100");
        try {
            target.promote(command);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

package sm.domain.workflow.process.engine.warmflow;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.dto.NodeJson;
import org.dromara.warm.flow.core.dto.SkipJson;
import org.dromara.warm.flow.orm.mapper.FlowInstanceMapper;
import org.dromara.warm.flow.spring.boot.config.FlowAutoConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.engine.warmflow.helper.WarmFlowRuntimeAdapter;
import sm.system.exception.BizException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** 使用真实引擎和 PostgreSQL，保护终止语义、业务回滚和首个审批竞争。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WarmFlowRuntimePostgresTests {
    private AnnotationConfigApplicationContext context;
    private JdbcTemplate verification;
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private WarmFlowRuntimeAdapter engine;
    private String schema;

    @BeforeAll
    void initialize() throws Exception {
        String url = System.getProperty("smartManage.testDbUrl");
        String user = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        verification = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        String database = verification.queryForObject("SELECT current_database()", String.class);
        assertTrue(database != null && database.startsWith("smart_manage_verify_"));
        schema = "workflow_" + UUID.randomUUID().toString().replace("-", "");
        verification.execute("CREATE SCHEMA " + schema);
        var source = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema, user, password);
        jdbc = new JdbcTemplate(source);
        new ResourceDatabasePopulator(new ClassPathResource("db/workflow/migration/V1__warm_flow_schema.sql")).execute(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowTaskQueryMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var resolver = new PathMatchingResourcePatternResolver();
        var mapperResources = new java.util.ArrayList<org.springframework.core.io.Resource>();
        mapperResources.addAll(List.of(resolver.getResources("classpath*:warm/flow/*.xml")));
        mapperResources.addAll(List.of(resolver.getResources("classpath*:mapper/workflow/engine/WarmFlowTaskQueryMapper.xml")));
        factory.setMapperLocations(mapperResources.toArray(org.springframework.core.io.Resource[]::new));
        context = new AnnotationConfigApplicationContext();
        var sessionFactory = factory.getObject();
        context.registerBean("sqlSessionFactory", org.apache.ibatis.session.SqlSessionFactory.class, () -> sessionFactory);
        context.register(FlowAutoConfig.class);
        context.refresh();
        engine = new WarmFlowRuntimeAdapter(context.getBean(FlowInstanceMapper.class),
                new org.mybatis.spring.SqlSessionTemplate(sessionFactory).getMapper(sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowTaskQueryMapper.class));
        transaction.executeWithoutResult(status -> {
            var definition = new DefJson().setFlowCode("leave").setFlowName("请假验证").setVersion("1").setModelValue("CLASSICS")
                    .setNodeList(List.of(node("start", 0, null, "first"), node("first", 1, "20@@21", "second"),
                            node("second", 1, "30", "end"), node("end", 2, null, null)));
            var imported = FlowEngine.defService().importDef(definition);
            FlowEngine.defService().publish(imported.getId());
        });
    }

    @BeforeEach
    void clearRuns() {
        // 上游持有进程级静态上下文，整套真实引擎测试共享一个 Spring 生命周期。
        jdbc.execute("TRUNCATE flow_instance, flow_task, flow_his_task, flow_user");
    }

    @AfterAll
    void cleanup() {
        if (context != null) context.close();
        if (schema != null) verification.execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Test
    void taskBoxesExcludeWithdrawnTasksAndReplacedCandidatesButRetainActualHistory() {
        var withdrawn = start();
        assertEquals(List.of(withdrawn.id()), engine.select(20L, WorkflowEngine.Box.PENDING, 0, 20).instanceIds());
        transaction.executeWithoutResult(status -> engine.withdraw(withdrawn.id(), 10L));
        assertEquals(0, engine.select(20L, WorkflowEngine.Box.PENDING, 0, 20).total());
        assertEquals(0, engine.select(10L, WorkflowEngine.Box.COMPLETED, 0, 20).total());
        assertEquals(List.of(withdrawn.id()), engine.select(10L, WorkflowEngine.Box.STARTED, 0, 20).instanceIds());

        var run = start();
        transaction.executeWithoutResult(status -> engine.replaceCandidates(run.id(), run.tasks().getFirst().id(), 10L, List.of(40L)));
        assertEquals(0, engine.select(20L, WorkflowEngine.Box.PENDING, 0, 20).total());
        assertEquals(List.of(run.id()), engine.select(40L, WorkflowEngine.Box.PENDING, 0, 20).instanceIds());
        var next = transaction.execute(status -> engine.approve(run.id(), run.tasks().getFirst().id(), 40L, "同意"));
        assertEquals(0, engine.select(40L, WorkflowEngine.Box.PENDING, 0, 20).total());
        assertEquals(List.of(run.id()), engine.select(40L, WorkflowEngine.Box.COMPLETED, 0, 20).instanceIds());
        assertEquals(List.of(run.id()), engine.select(30L, WorkflowEngine.Box.PENDING, 0, 20).instanceIds());
        transaction.executeWithoutResult(status -> engine.reject(run.id(), next.tasks().getFirst().id(), 30L, "拒绝"));
        assertEquals(0, engine.select(30L, WorkflowEngine.Box.PENDING, 0, 20).total());
        assertEquals(List.of(run.id()), engine.select(30L, WorkflowEngine.Box.COMPLETED, 0, 20).instanceIds());
    }

    @Test
    void mutuallyExclusiveConditionsChooseByValueAndChartKeepsExecutedTrajectory() {
        transaction.executeWithoutResult(status -> {
            var gateway = node("gate", 3, null, null).setSkipList(List.of(
                    new SkipJson().setNowNodeCode("gate").setNextNodeCode("small").setSkipType("PASS").setSkipCondition("lt@@days|3"),
                    new SkipJson().setNowNodeCode("gate").setNextNodeCode("large").setSkipType("PASS").setSkipCondition("ge@@days|3"),
                    new SkipJson().setNowNodeCode("gate").setNextNodeCode("fallback").setSkipType("PASS")));
            var definition = new DefJson().setFlowCode("conditional").setFlowName("分支验证").setVersion("1").setModelValue("CLASSICS")
                    .setNodeList(List.of(node("start", 0, null, "gate"), gateway, node("small", 1, "20", "end"),
                            node("large", 1, "30", "end"), node("fallback", 1, "40", "end"), node("end", 2, null, null)));
            var imported = FlowEngine.defService().importDef(definition);
            FlowEngine.defService().publish(imported.getId());
        });
        var small = transaction.execute(status -> engine.start("conditional", "SMALL", 10L, Map.of("days", new java.math.BigDecimal("2.99"))));
        assertEquals(List.of(20L), small.tasks().getFirst().candidates());
        var large = transaction.execute(status -> engine.start("conditional", "LARGE", 10L, Map.of("days", new java.math.BigDecimal("3.00"))));
        assertEquals(List.of(30L), large.tasks().getFirst().candidates());
        var before = FlowEngine.jsonConvert.strToBean(engine.chart(large.id()), DefJson.class);
        assertEquals(org.dromara.warm.flow.core.enums.ChartStatus.TO_DO.getKey(), before.getNodeList().stream().filter(node -> node.getNodeCode().equals("large")).findFirst().orElseThrow().getStatus());
        var completed = transaction.execute(status -> engine.approve(large.id(), large.tasks().getFirst().id(), 30L, "同意"));
        assertEquals(WorkflowEngine.State.APPROVED, completed.state());
        var after = FlowEngine.jsonConvert.strToBean(engine.chart(large.id()), DefJson.class);
        assertEquals(org.dromara.warm.flow.core.enums.ChartStatus.DONE.getKey(), after.getNodeList().stream().filter(node -> node.getNodeCode().equals("large")).findFirst().orElseThrow().getStatus());
        assertEquals(org.dromara.warm.flow.core.enums.ChartStatus.NOT_DONE.getKey(), after.getNodeList().stream().filter(node -> node.getNodeCode().equals("small")).findFirst().orElseThrow().getStatus());
        assertNull(after.getInstance(), "图形接口不能暴露实例变量");
        assertNotNull(completed.history().getLast().time());
    }

    @Test
    void applicantCanWithdrawBeforeAnyApprovalAndWithdrawalEndsAllTasks() {
        var run = start();
        assertFalse(run.approvalStarted());
        assertEquals(List.of(20L, 21L), run.tasks().getFirst().candidates());
        assertThrows(BizException.class, () -> transaction.execute(status -> engine.withdraw(run.id(), 20L)));
        var withdrawn = transaction.execute(status -> engine.withdraw(run.id(), 10L));
        assertEquals(WorkflowEngine.State.WITHDRAWN, withdrawn.state());
        assertTrue(withdrawn.tasks().isEmpty());
        assertEquals(List.of("SUBMITTED", "WITHDRAWN"), withdrawn.history().stream().map(WorkflowEngine.History::action).toList());
        assertThrows(BizException.class, () -> transaction.execute(status -> engine.approve(run.id(), run.tasks().getFirst().id(), 20L, "同意")));
    }

    @Test
    void firstApprovalBlocksWithdrawalAndOrSignCanOnlyBeProcessedOnce() {
        var run = start();
        var next = transaction.execute(status -> engine.approve(run.id(), run.tasks().getFirst().id(), 20L, "同意"));
        assertTrue(next.approvalStarted());
        assertEquals("second", next.tasks().getFirst().nodeCode());
        assertThrows(BizException.class, () -> transaction.execute(status -> engine.withdraw(run.id(), 10L)));
        assertThrows(BizException.class, () -> transaction.execute(status -> engine.approve(run.id(), run.tasks().getFirst().id(), 21L, "重复同意")));
        var completed = transaction.execute(status -> engine.approve(run.id(), next.tasks().getFirst().id(), 30L, "同意"));
        assertEquals(WorkflowEngine.State.APPROVED, completed.state());
        assertTrue(completed.tasks().isEmpty());
    }

    @Test
    void rejectionTerminatesInsteadOfReturningAndResubmissionUsesNewInstance() {
        var run = start();
        var rejected = transaction.execute(status -> engine.reject(run.id(), run.tasks().getFirst().id(), 20L, "理由"));
        assertEquals(WorkflowEngine.State.REJECTED, rejected.state());
        assertTrue(rejected.tasks().isEmpty());
        assertEquals("REJECTED", rejected.history().getLast().action());
        assertNotEquals(run.id(), start().id());
    }

    @Test
    void businessFailureRollsBackStartAndApprovalWithHistory() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            engine.start("leave", "leave/1", 10L, Map.of());
            throw new IllegalStateException("业务提交失败");
        }));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM flow_instance", Integer.class));
        var run = start();
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            engine.approve(run.id(), run.tasks().getFirst().id(), 20L, "同意");
            throw new IllegalStateException("业务回写失败");
        }));
        var restored = engine.inspect(run.id());
        assertEquals(run.tasks(), restored.tasks());
        assertEquals(run.history(), restored.history());
        assertFalse(restored.approvalStarted());
    }

    @Test
    void withdrawalWaitsForFirstApprovalCommitThenFails() throws Exception {
        var run = start();
        var approved = new CountDownLatch(1);
        var commit = new CountDownLatch(1);
        var withdrawing = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var approval = executor.submit(() -> transaction.executeWithoutResult(status -> {
                engine.approve(run.id(), run.tasks().getFirst().id(), 20L, "同意");
                approved.countDown();
                await(commit);
            }));
            try {
                assertTrue(approved.await(10, TimeUnit.SECONDS));
                var withdrawal = executor.submit(() -> {
                    withdrawing.countDown();
                    return transaction.execute(status -> engine.withdraw(run.id(), 10L));
                });
                assertTrue(withdrawing.await(10, TimeUnit.SECONDS));
                commit.countDown();
                approval.get(10, TimeUnit.SECONDS);
                var failure = assertThrows(java.util.concurrent.ExecutionException.class, () -> withdrawal.get(10, TimeUnit.SECONDS));
                assertInstanceOf(BizException.class, failure.getCause());
                assertEquals("second", engine.inspect(run.id()).tasks().getFirst().nodeCode());
            } finally { commit.countDown(); }
        }
    }

    private WorkflowEngine.Run start() {
        return transaction.execute(status -> engine.start("leave", "leave/1", 10L, Map.of()));
    }

    @Test
    void candidateMaintenanceFreezesNewUsersWithoutFabricatingApprovalAndCanRollback() {
        var run = start();
        Long taskId = run.tasks().getFirst().id();
        transaction.executeWithoutResult(status -> engine.replaceCandidates(run.id(), taskId, 99L, List.of(40L, 41L)));
        var replaced = engine.inspect(run.id());
        assertEquals(List.of(40L, 41L), replaced.tasks().getFirst().candidates());
        assertEquals(run.history(), replaced.history());
        assertFalse(replaced.approvalStarted());
        assertThrows(BizException.class, () -> transaction.execute(status -> engine.approve(run.id(), taskId, 20L, "原候选人")));
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            engine.replaceCandidates(run.id(), taskId, 99L, List.of(50L));
            throw new IllegalStateException("审计失败");
        }));
        assertEquals(List.of(40L, 41L), engine.inspect(run.id()).tasks().getFirst().candidates());
        assertEquals(WorkflowEngine.State.WITHDRAWN, transaction.execute(status -> engine.withdraw(run.id(), 10L)).state());
    }

    private static NodeJson node(String code, int type, String users, String next) {
        return new NodeJson().setNodeCode(code).setNodeName(code).setNodeType(type).setNodeRatio("0").setPermissionFlag(users)
                .setSkipList(next == null ? List.of() : List.of(new SkipJson().setNowNodeCode(code).setNextNodeCode(next).setSkipType("PASS")));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("等待事务超时");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failure);
        }
    }
}

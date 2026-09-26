package sm.domain.demo.office.leave.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.spring.boot.config.FlowAutoConfig;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mapstruct.factory.Mappers;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sm.domain.demo.office.leave.converter.LeaveConverter;
import sm.domain.demo.office.leave.mapper.*;
import sm.domain.demo.office.leave.model.form.LeaveSubmitForm;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.numberrule.contract.NumberGenerator;
import sm.domain.sys.base.role.contract.RoleReferenceReader;
import sm.domain.sys.base.user.contract.*;
import sm.domain.sys.message.inbox.contract.InboxNotificationPublisher;
import sm.domain.workflow.process.definition.model.form.*;
import sm.domain.workflow.process.definition.service.DefinitionService;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.domain.workflow.process.runtime.service.RuntimeService;
import sm.domain.workflow.process.notification.service.NotificationService;
import sm.system.datascope.*;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** 真实引擎、业务表和 Spring 事务闭环；外部用户目录、文件存储和消息投递以边界替身隔离。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaveWorkflowPostgresTests {
    private AnnotationConfigApplicationContext context;
    private JdbcTemplate admin;
    private JdbcTemplate jdbc;
    private String database;
    private final ThreadLocal<Long> actor = ThreadLocal.withInitial(() -> 10L);
    private LeaveTxService leave;
    private RuntimeService runtime;
    private WorkflowEngine engine;
    private InstanceService instances;
    private InboxNotificationPublisher publisher;
    private UserReferenceReader users;

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Transactions { }

    @BeforeAll
    void initialize() throws Exception {
        String url = System.getProperty("smartManage.testDbUrl");
        String username = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        admin = new JdbcTemplate(new DriverManagerDataSource(url, username, password));
        assertTrue(admin.queryForObject("SELECT current_database()", String.class).startsWith("smart_manage_verify_"));
        String candidateDatabase = "smart_manage_leave_verify_" + UUID.randomUUID().toString().replace("-", "");
        admin.execute("CREATE DATABASE " + candidateDatabase);
        database = candidateDatabase;
        var source = new DriverManagerDataSource(url.substring(0, url.lastIndexOf('/') + 1) + database, username, password);
        jdbc = new JdbcTemplate(source);
        try (var migrationContext = new AnnotationConfigApplicationContext(sm.infrastructure.persistence.FlywayMigrationConfig.class)) {
            migrationContext.getBean(org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy.class)
                    .migrate(Flyway.configure().dataSource(source).locations("classpath:db/platform/migration").load());
        }
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(sm.infrastructure.persistence.UuidTypeHandler.class);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
        var operator = mock(sm.system.security.context.CurrentOperatorProvider.class);
        when(operator.getCurrentUserIdOrNull()).thenAnswer(invocation -> actor.get());
        var global = new GlobalConfig().setDbConfig(new GlobalConfig.DbConfig())
                .setMetaObjectHandler(new sm.system.persistence.audit.BaseEntityAuditMetaObjectHandler(operator));
        // 手动构造测试工厂时，必须先设置全局配置，再注册 Mapper；否则元数据会缓存默认配置。
        com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils.setGlobalConfig(configuration, global);
        var mapperClasses = List.of(LeaveMapper.class, LeaveAttachmentEntryMapper.class, LeaveAttachmentSnapshotMapper.class,
                sm.domain.workflow.process.definition.mapper.WorkflowBindingMapper.class,
                sm.domain.workflow.process.instance.mapper.WorkflowInstanceMapper.class,
                sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowCoordinationMapper.class,
                sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowTaskQueryMapper.class,
                sm.domain.workflow.process.task.mapper.TaskCandidateChangeMapper.class,
                sm.domain.workflow.process.runtime.mapper.RuntimeCommandMapper.class,
                sm.domain.workflow.process.notification.mapper.WorkflowOutboxMapper.class);
        mapperClasses.forEach(configuration::addMapper);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setGlobalConfig(global);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor(com.baomidou.mybatisplus.annotation.DbType.POSTGRE_SQL));
        factory.setPlugins(interceptor);
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = new ArrayList<org.springframework.core.io.Resource>();
        resources.addAll(List.of(resolver.getResources("classpath*:warm/flow/*.xml")));
        resources.addAll(List.of(resolver.getResources("classpath*:mapper/workflow/**/*.xml")));
        factory.setMapperLocations(resources.toArray(org.springframework.core.io.Resource[]::new));
        var sessionFactory = factory.getObject();
        var session = new SqlSessionTemplate(sessionFactory);
        context = new AnnotationConfigApplicationContext();
        context.registerBean("sqlSessionFactory", org.apache.ibatis.session.SqlSessionFactory.class, () -> sessionFactory);
        for (var mapperClass : mapperClasses) registerMapper(mapperClass, session);
        context.registerBean("transactionManager", org.springframework.transaction.PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source));
        context.registerBean(ObjectMapper.class, () -> JsonMapper.builder().findAndAddModules().build());
        var current = mock(CurrentUserContext.class);
        when(current.getUserId()).thenAnswer(invocation -> actor.get());
        when(current.getOrgId()).thenReturn(100L);
        context.registerBean(CurrentUserContext.class, () -> current);
        users = mock(UserReferenceReader.class);
        when(users.requireEnabled(anyLong())).thenAnswer(invocation -> new UserReference(invocation.getArgument(0), "USER", "测试用户", "test", null, true));
        when(users.findByIds(anyCollection())).thenReturn(Map.of());
        context.registerBean(UserReferenceReader.class, () -> users);
        context.registerBean(UserAssignmentReader.class, () -> mock(UserAssignmentReader.class));
        context.registerBean(RoleReferenceReader.class, () -> mock(RoleReferenceReader.class));
        context.registerBean(sm.domain.sys.base.feature.contract.FeatureDirectoryReader.class,
                () -> mock(sm.domain.sys.base.feature.contract.FeatureDirectoryReader.class));
        context.registerBean(DataScopeResolver.class, () -> (resource, action) -> new DataScope(false, true, Set.of(), actor.get()));
        var numbers = mock(NumberGenerator.class);
        var sequence = new AtomicLong();
        when(numbers.nextNumber(anyString(), any())).thenAnswer(invocation -> "LV-" + sequence.incrementAndGet());
        context.registerBean(NumberGenerator.class, () -> numbers);
        var attachments = mock(AttachmentGateway.class);
        when(attachments.listForAggregate(anyString(), anyString())).thenReturn(List.of());
        context.registerBean(AttachmentGateway.class, () -> attachments);
        publisher = mock(InboxNotificationPublisher.class);
        context.registerBean(InboxNotificationPublisher.class, () -> publisher);
        context.registerBean(LeaveConverter.class, () -> Mappers.getMapper(LeaveConverter.class));
        context.register(Transactions.class, FlowAutoConfig.class, LeaveTxService.class, LeaveDataScope.class, LeaveWorkflowParticipant.class, LeaveResourceRegistration.class);
        context.scan("sm.domain.workflow.process");
        context.refresh();
        leave = context.getBean(LeaveTxService.class);
        runtime = context.getBean(RuntimeService.class);
        engine = context.getBean(WorkflowEngine.class);
        instances = context.getBean(InstanceService.class);
        var definitions = context.getBean(DefinitionService.class);
        Long definitionId = definitions.create(new DefinitionCreateForm("leave", "请假验证", "demo/office/leave"));
        var definition = FlowEngine.defService().queryDesign(definitionId);
        definition.getNodeList().stream().filter(node -> node.getNodeType() == 1).forEach(node -> node.setPermissionFlag("sm:user:20@@sm:user:21"));
        var mapper = context.getBean(ObjectMapper.class);
        var saved = definitions.save(new DefinitionSaveForm(definitionId, mapper.readTree(FlowEngine.jsonConvert.objToStr(definition)), definitions.design(definitionId).digest()));
        definitions.publish(new DefinitionPublishForm(definitionId, saved.digest()));
    }

    private <Target> void registerMapper(Class<Target> type, SqlSessionTemplate session) { context.registerBean(type, () -> session.getMapper(type)); }

    @BeforeEach
    void resetRuns() {
        actor.set(10L);
        jdbc.execute("TRUNCATE t_workflow_outbox,t_workflow_instance,t_workflow_command,t_workflow_candidate_change,t_demo_leave,t_demo_leave_attachment_entry,t_demo_leave_attachment_snapshot,flow_instance,flow_task,flow_user,flow_his_task CASCADE");
        reset(publisher);
    }

    @AfterAll
    void cleanup() {
        if (context != null) context.close();
        if (database != null) admin.execute("DROP DATABASE " + database + " WITH (FORCE)");
        actor.remove();
    }

    @Test
    void replacingAnUnusedPublishedVersionKeepsItImmutable() {
        var definitions = context.getBean(DefinitionService.class);
        Long original = FlowEngine.defService().getPublishByFlowCode("leave").getId();
        Long copied = definitions.copy(original);
        var copyDesign = definitions.design(copied);
        definitions.publish(new DefinitionPublishForm(copied, copyDesign.digest()));
        assertEquals(9, FlowEngine.defService().getById(original).getIsPublish());
        assertEquals(copied, FlowEngine.defService().getPublishByFlowCode("leave").getId());
        var oldDesign = definitions.design(original);
        assertThrows(BizException.class, () -> definitions.save(new DefinitionSaveForm(original, oldDesign.definition(), oldDesign.digest())));
        assertThrows(BizException.class, () -> definitions.publish(new DefinitionPublishForm(original, oldDesign.digest())));
        assertEquals(0, FlowEngine.defService().getById(definitions.copy(original)).getIsPublish());
    }

    @Test
    void candidateMaintenanceIsAuditedWithoutApprovalAndAttachmentAccessStaysInItsRound() {
        var form = form();
        Long id = leave.submit(form);
        Long first = currentInstance(id);
        jdbc.update("INSERT INTO t_demo_leave_attachment_snapshot(id,leave_id,instance_id,attachment_id) VALUES(1001,?,?,42)", id, first);
        var policy = context.getBean(LeaveResourceRegistration.class);
        var maintenance = context.getBean(sm.domain.workflow.process.task.service.TaskMaintenanceService.class);
        try (var permissions = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            actor.set(20L);
            policy.requireAttachmentAllowed(id.toString(), 42L, sm.system.resource.BusinessResourceAction.READ);
            runtime.approve(new ApprovalForm(first, engine.inspect(first).tasks().getFirst().id(), "REJECT", "修改", UUID.randomUUID()));
            actor.set(10L);
            form.setId(id);
            form.setVersion(jdbc.queryForObject("SELECT version FROM t_demo_leave WHERE id=?", Integer.class, id));
            form.setRequestId(UUID.randomUUID());
            leave.submit(form);
            Long second = currentInstance(id);
            jdbc.update("INSERT INTO t_demo_leave_attachment_snapshot(id,leave_id,instance_id,attachment_id) VALUES(1002,?,?,43)", id, second);
            var before = engine.inspect(second);
            actor.set(99L);
            maintenance.replace(new sm.domain.workflow.process.task.model.form.TaskCandidateForm(second,
                    before.tasks().getFirst().id(), List.of(30L), "原候选人不可用"));
            permissions.verify(() -> cn.dev33.satoken.stp.StpUtil.checkPermission(sm.domain.workflow.process.task.constant.TaskPermission.MAINTAIN));
            assertEquals(before.history(), engine.inspect(second).history());
            assertFalse(engine.inspect(second).approvalStarted());
            assertEquals(1, count("t_workflow_candidate_change"));
            assertEquals(99L, jdbc.queryForObject("SELECT operator_id FROM t_workflow_candidate_change", Long.class));
            actor.set(20L);
            policy.requireAttachmentAllowed(id.toString(), 42L, sm.system.resource.BusinessResourceAction.READ);
            assertThrows(BizException.class, () -> policy.requireAttachmentAllowed(id.toString(), 43L, sm.system.resource.BusinessResourceAction.READ));
            assertThrows(BizException.class, () -> instances.readable(second));
            actor.set(30L);
            policy.requireAttachmentAllowed(id.toString(), 43L, sm.system.resource.BusinessResourceAction.READ);
            assertThrows(BizException.class, () -> policy.requireAttachmentAllowed(id.toString(), 42L, sm.system.resource.BusinessResourceAction.READ));
            actor.set(10L);
            runtime.withdraw(new sm.domain.workflow.process.runtime.model.form.WithdrawForm(second, UUID.randomUUID()));
            var transaction = new org.springframework.transaction.support.TransactionTemplate(context.getBean(org.springframework.transaction.PlatformTransactionManager.class));
            assertThrows(BizException.class, () -> transaction.executeWithoutResult(status -> policy.beforeAttachmentMutation(
                    id.toString(), 42L, sm.system.resource.BusinessResourceAction.DELETE)));
        }
    }

    @Test
    void submissionReplaySnapshotRejectionAndNewRoundKeepTheirOwnFacts() {
        var form = form();
        Long id = leave.submit(form);
        Long first = currentInstance(id);
        assertEquals(id, leave.submit(form));
        form.setReason("不能用同键更改内容");
        assertThrows(BizException.class, () -> leave.submit(form));
        form.setReason("原始事由");
        assertEquals(1, count("t_workflow_instance"));
        assertEquals(2, count("t_workflow_outbox"));
        actor.set(99L);
        assertThrows(BizException.class, () -> instances.requireSnapshot("demo/office/leave", id, first));
        actor.set(20L);
        assertTrue(instances.requireSnapshot("demo/office/leave", id, first).contains("原始事由"));
        var rejection = new ApprovalForm(first, engine.inspect(first).tasks().getFirst().id(), "REJECT", "请修改", UUID.randomUUID());
        assertEquals(runtime.approve(rejection), runtime.approve(rejection));
        assertThrows(BizException.class, () -> runtime.approve(new ApprovalForm(first, rejection.taskId(), "APPROVE", "请修改", rejection.requestId())));
        assertEquals("A", jdbc.queryForObject("SELECT bill_status FROM t_demo_leave WHERE id=?", String.class, id));
        actor.set(10L);
        form.setId(id);
        form.setVersion(jdbc.queryForObject("SELECT version FROM t_demo_leave WHERE id=?", Integer.class, id));
        form.setRequestId(UUID.randomUUID());
        form.setReason("修改后的事由");
        leave.submit(form);
        Long second = currentInstance(id);
        assertNotEquals(first, second);
        assertTrue(instances.requireSnapshot("demo/office/leave", id, first).contains("原始事由"));
        assertTrue(instances.requireSnapshot("demo/office/leave", id, second).contains("修改后的事由"));
        assertNull(jdbc.queryForObject("SELECT last_outcome FROM t_demo_leave WHERE id=?", String.class, id));
        assertThrows(BizException.class, () -> runtime.withdraw(new sm.domain.workflow.process.runtime.model.form.WithdrawForm(first, UUID.randomUUID())));
    }

    @Test
    void withdrawalAndApprovalWriteBusinessStateAndOutboxInOneTransaction() {
        Long id = leave.submit(form());
        Long instanceId = currentInstance(id);
        jdbc.execute("CREATE FUNCTION fail_leave_approval() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.bill_status='C' THEN RAISE EXCEPTION 'fixture rejection'; END IF; RETURN NEW; END $$");
        jdbc.execute("CREATE TRIGGER fail_leave_approval BEFORE UPDATE ON t_demo_leave FOR EACH ROW EXECUTE FUNCTION fail_leave_approval()");
        try {
            actor.set(20L);
            var task = engine.inspect(instanceId).tasks().getFirst();
            assertThrows(RuntimeException.class, () -> runtime.approve(new ApprovalForm(instanceId, task.id(), "APPROVE", "同意", UUID.randomUUID())));
            assertEquals(task, engine.inspect(instanceId).tasks().getFirst());
            assertFalse(engine.inspect(instanceId).approvalStarted());
            assertEquals(2, count("t_workflow_outbox"));
        } finally {
            jdbc.execute("DROP TRIGGER fail_leave_approval ON t_demo_leave");
            jdbc.execute("DROP FUNCTION fail_leave_approval()");
        }
        actor.set(10L);
        var withdrawal = new sm.domain.workflow.process.runtime.model.form.WithdrawForm(instanceId, UUID.randomUUID());
        assertEquals(runtime.withdraw(withdrawal), runtime.withdraw(withdrawal));
        assertEquals("A", jdbc.queryForObject("SELECT bill_status FROM t_demo_leave WHERE id=?", String.class, id));
        assertEquals(WorkflowEngine.State.WITHDRAWN, engine.inspect(instanceId).state());
        assertEquals(3, count("t_workflow_outbox"));
    }

    @Test
    void deliveryFailureDoesNotRollbackApprovalAndRetriesWithSameKeys() {
        Long id = leave.submit(form());
        var service = context.getBean(NotificationService.class);
        when(publisher.publish(any())).thenThrow(new IllegalStateException("fixture unavailable"));
        assertEquals(2, service.dispatch());
        assertEquals("B", jdbc.queryForObject("SELECT bill_status FROM t_demo_leave WHERE id=?", String.class, id));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_workflow_outbox WHERE status='DELIVERED'", Integer.class));
        jdbc.update("UPDATE t_workflow_outbox SET next_attempt_at=CURRENT_TIMESTAMP");
        doReturn(300L).when(publisher).publish(any());
        service.dispatch();
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM t_workflow_outbox WHERE status='DELIVERED'", Integer.class));
        assertEquals(0, service.dispatch());
    }

    private Long currentInstance(Long id) { return jdbc.queryForObject("SELECT current_instance_id FROM t_demo_leave WHERE id=?", Long.class, id); }
    private int count(String table) { return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class); }
    private LeaveSubmitForm form() {
        var form = new LeaveSubmitForm();
        form.setClientKey(UUID.randomUUID());
        form.setRequestId(UUID.randomUUID());
        form.setBizDate(LocalDate.of(2026, 9, 26));
        form.setLeaveType("PERSONAL");
        form.setStartTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        form.setEndTime(LocalDateTime.of(2026, 10, 2, 18, 0));
        form.setDays(new BigDecimal("2.00"));
        form.setReason("原始事由");
        return form;
    }
}

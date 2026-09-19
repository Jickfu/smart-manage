package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mapstruct.factory.Mappers;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.scheduler.converter.JobLogConverter;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.form.JobListForm;
import sm.domain.sys.scheduler.model.form.JobLogListForm;
import sm.system.query.ListSqlQuery;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 真实迁移和 Mapper 验证业务归属筛选、目录快照及历史分区兼容性。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SchedulerScopePostgresTests {
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private JobMapper jobMapper;
    private JobLogMapper logMapper;
    private JobLogService logService;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        // JDBC 夹具直接改表，避免同一事务内 MyBatis 一级缓存遮蔽真实数据库变化。
        configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
        configuration.addMapper(JobMapper.class);
        configuration.addMapper(JobLogMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        factory.setPlugins(interceptor);
        factory.setMapperLocations(new ClassPathResource("mapper/sys/scheduler/JobMapper.xml"),
                new ClassPathResource("mapper/sys/scheduler/JobLogMapper.xml"),
                new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        jobMapper = session.getMapper(JobMapper.class);
        logMapper = session.getMapper(JobLogMapper.class);
        logService = new JobLogService(logMapper, Mappers.getMapper(JobLogConverter.class));
    }

    @Test
    void taskScopeUsesCurrentAppWhileLogsKeepExecutionScopeAfterMoveAndDeletion() {
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            seed();
            var catalogService = new SchedulerCatalogService(jobMapper, logMapper);
            // 没有执行记录也展示业务目录，包括尚无应用的领域。
            assertTrue(catalogService.executions().stream().anyMatch(node ->
                    node.key().equals("domain:9400000011") && node.children().isEmpty()));
            assertTrue(logMapper.selectCatalog().stream().anyMatch(row ->
                    Long.valueOf(9400000020L).equals(row.getAppId())));
            var form = new JobListForm();
            form.setDomainId(9400000010L);
            form.setAppId(9400000020L);
            form.setPageSize(1);
            var page = jobMapper.selectListPage(new Page<>(1, 1), form, new ListSqlQuery(List.of(), null, null));
            assertEquals(2, page.getTotal());
            assertEquals(1, page.getRecords().size());
            assertEquals("原应用", page.getRecords().getFirst().getAppName());
            assertEquals(9400000010L, jobMapper.selectExecutionSnapshot(9400000030L).getDomainId());
            insertExecution(9400000040L, 9400000010L, "原领域", "原应用");

            jdbc.update("UPDATE t_sys_app SET domain_id=9400000011,name='新应用' WHERE id=9400000020");
            assertEquals(0, jobMapper.selectListPage(new Page<>(1, 1), form,
                    new ListSqlQuery(List.of(), null, null)).getTotal());
            assertEquals(9400000011L, jobMapper.selectExecutionSnapshot(9400000030L).getDomainId());
            insertExecution(9400000041L, 9400000011L, "新领域", "新应用");
            var catalog = catalogService.executions();
            var currentDomain = catalog.stream().filter(node -> node.key().equals("domain:9400000011")).findFirst().orElseThrow();
            assertEquals("app:9400000011:9400000020", currentDomain.children().getFirst().key());
            assertEquals("新应用", currentDomain.children().getFirst().title());
            var logForm = new JobLogListForm();
            logForm.setDomainId(9400000010L);
            assertEquals(0, logService.listPage(logForm).getTotal());
            logForm.setDomainId(9400000011L);
            assertEquals(2, logService.listPage(logForm).getTotal());
            // 应用节点按日志保存的应用 ID 过滤，不能再叠加执行时领域快照。
            logForm.setAppId(9400000020L);
            logForm.setDomainId(9400000010L);
            assertEquals(2, logService.listPage(logForm).getTotal());

            jdbc.update("DELETE FROM t_sys_job WHERE id IN (9400000030,9400000031)");
            jdbc.update("DELETE FROM t_sys_app WHERE id=9400000020");
            jdbc.update("DELETE FROM t_sys_domain WHERE id IN (9400000010,9400000011)");
            logForm.setDomainId(null);
            logForm.setAppId(null);
            var logs = logService.listPage(logForm);
            assertEquals(2, logs.getTotal());
            assertEquals("原应用", logService.detail(9400000040L).getAppName());
            assertEquals("原领域", logService.detail(9400000040L).getDomainName());
            assertTrue(logMapper.selectCatalog().stream().noneMatch(row ->
                    Long.valueOf(9400000020L).equals(row.getAppId())));
            assertNull(jobMapper.selectExecutionSnapshot(9400000030L));

            // 转储依靠同构父表；变更后的快照列必须能随整月分区一同转入历史。
            jdbc.execute("CREATE TABLE scheduler_scope_archive_probe PARTITION OF t_sys_job_log FOR VALUES FROM ('1900-01-01') TO ('1900-02-01')");
            jdbc.update("INSERT INTO t_sys_job_log (id,job_id,job_name,start_time,status,domain_id,domain_name,app_id,app_name) VALUES (9400000042,9400000030,'历史任务','1900-01-02','SUCCESS',9400000010,'原领域',9400000020,'原应用')");
            jdbc.execute("ALTER TABLE t_sys_job_log DETACH PARTITION scheduler_scope_archive_probe");
            jdbc.execute("ALTER TABLE t_sys_job_log_history ATTACH PARTITION scheduler_scope_archive_probe FOR VALUES FROM ('1900-01-01') TO ('1900-02-01')");
            assertEquals("原应用", jdbc.queryForObject("SELECT app_name FROM t_sys_job_log_history WHERE id=9400000042", String.class));
        });
    }

    @Test
    void existingTaskPreventsDeletingItsRequiredApplication() {
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            seed();
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                    () -> jdbc.update("DELETE FROM t_sys_app WHERE id=9400000020"));
        });
    }

    private void seed() {
        jdbc.update("INSERT INTO t_sys_domain(id,name,number) VALUES (9400000010,'原领域','verify-scheduler-old'),(9400000011,'新领域','verify-scheduler-new')");
        jdbc.update("INSERT INTO t_sys_app(id,name,number,icon,domain_id) VALUES (9400000020,'原应用','verify-scheduler-app','ClockCircleOutlined',9400000010)");
        jdbc.update("INSERT INTO t_sys_job(id,number,job_name,app_id,job_class_name,cron_expression,status) VALUES (9400000030,'verify-scheduler-one','同名任务',9400000020,'example.Job','0 0 3 * * ?','PAUSED'),(9400000031,'verify-scheduler-two','同名任务',9400000020,'example.Job','0 0 3 * * ?','PAUSED')");
    }

    private void insertExecution(Long id, Long domainId, String domainName, String appName) {
        jdbc.update("INSERT INTO t_sys_job_log(id,job_id,job_name,start_time,status,domain_id,domain_name,app_id,app_name) VALUES (?,9400000030,'执行时任务名',now(),'SUCCESS',?,?,9400000020,?)",
                id, domainId, domainName, appName);
    }
}

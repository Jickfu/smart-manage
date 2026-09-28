package sm.domain.workflow.process.log.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 流转日志是全局运维视图，不能把审批意见正文一并返回。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowLogMapperPostgresTests {
    private JdbcTemplate verification;
    private JdbcTemplate jdbc;
    private FlowLogMapper mapper;
    private String schema;

    @BeforeAll
    void initialize() throws Exception {
        String url = System.getProperty("smartManage.testDbUrl");
        String user = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        verification = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        String database = verification.queryForObject("SELECT current_database()", String.class);
        assertTrue(database != null && database.startsWith("smart_manage_verify_"));
        schema = "flow_log_" + UUID.randomUUID().toString().replace("-", "");
        verification.execute("CREATE SCHEMA " + schema);
        var source = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?")
                + "currentSchema=" + schema, user, password);
        jdbc = new JdbcTemplate(source);
        createTables();

        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(FlowLogMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        factory.setPlugins(interceptor);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/workflow/log/FlowLogMapper.xml"));
        mapper = new SqlSessionTemplate(factory.getObject()).getMapper(FlowLogMapper.class);
    }

    @AfterAll
    void cleanup() {
        if (schema != null) verification.execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Test
    void engineHistoryDoesNotExposeApprovalOpinion() {
        jdbc.update("INSERT INTO t_workflow_instance(id, number, business_type) VALUES (1, 'L-1', 'demo.leave')");
        jdbc.update("""
                INSERT INTO flow_his_task(id, instance_id, task_id, node_name, node_type, approver,
                                          message, flow_status, del_flag, create_time, update_time)
                VALUES (2, 1, 3, '部门审批', 1, '20', '不应进入全局日志的审批意见', '', '0', now(), now())
                """);

        var page = mapper.listPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(1, 10), null);

        assertNull(page.getRecords().getFirst().getOpinion());
    }

    private void createTables() {
        jdbc.execute("CREATE TABLE t_workflow_instance (id bigint PRIMARY KEY, number varchar(100), business_type varchar(100))");
        jdbc.execute("""
                CREATE TABLE flow_his_task (
                    id bigint PRIMARY KEY, instance_id bigint, task_id bigint, node_name varchar(100),
                    node_type integer, cooperate_type integer, flow_status varchar(50), skip_type varchar(50),
                    approver varchar(100), message text, del_flag char(1), create_time timestamp, update_time timestamp)
                """);
        jdbc.execute("""
                CREATE TABLE t_workflow_operation (
                    id bigint PRIMARY KEY, instance_id bigint, task_id bigint, action varchar(50),
                    operator_id bigint, reason text, create_time timestamp)
                """);
        jdbc.execute("""
                CREATE TABLE t_workflow_script_execution (
                    id bigint PRIMARY KEY, instance_id bigint, task_id bigint, node_code varchar(100),
                    status varchar(50), operator_id bigint, error_message text, create_time timestamp)
                """);
        jdbc.execute("CREATE TABLE flow_task (id bigint PRIMARY KEY, node_name varchar(100))");
    }
}

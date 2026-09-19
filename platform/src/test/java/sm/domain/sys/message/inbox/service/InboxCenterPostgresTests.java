package sm.domain.sys.message.inbox.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.user.apppin.mapper.UserAppPinMapper;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/** 空库迁移后的真实SQL验证：内置固定偏好不串用户，时间和类型筛选发生在游标截取前。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class InboxCenterPostgresTests {
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private UserAppPinMapper pinMapper;
    private InboxRecipientMapper recipientMapper;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(UserAppPinMapper.class);
        configuration.addMapper(InboxRecipientMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new ClassPathResource("mapper/sys/base/user/UserAppPinMapper.xml"),
                new ClassPathResource("mapper/sys/message/inbox/InboxRecipientMapper.xml"),
                new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        pinMapper = session.getMapper(UserAppPinMapper.class);
        recipientMapper = session.getMapper(InboxRecipientMapper.class);
    }

    @Test
    void builtinPinIsVisibleWithoutApplicationPermissionAndDoesNotCrossUsers() {
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            jdbc.update("INSERT INTO t_sys_user(id,username,name,number,password,enabled) VALUES (9300000010,'verify-inbox-pin','固定测试','verify-inbox-pin',?,false)",
                    sm.system.helper.Argon2Helper.encode(java.util.UUID.randomUUID().toString()));
            jdbc.update("INSERT INTO t_sys_user_app_pin(id,user_id,builtin_key,seq,create_time) VALUES (9300000011,9300000010,'builtin:inbox',1,now())");
            var pins = pinMapper.selectUserPins(9300000010L, 9300000000L, false);
            assertEquals(1, pins.size());
            assertEquals("builtin:inbox", pins.getFirst().getNumber());
            assertTrue(pinMapper.selectUserPins(9300000012L, 9300000000L, false).isEmpty());
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> jdbc.update(
                    "INSERT INTO t_sys_user_app_pin(id,user_id,builtin_key,seq,create_time) VALUES (9300000013,9300000010,'builtin:unknown',2,now())"));
        });
    }

    @Test
    void actualQueryCombinesUnreadCategoryAndMonthWithoutLosingMicroseconds() {
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            LocalDateTime month = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            insertMessage(9300000021L, "ALL_ENABLED_USERS", month.plusSeconds(1).withNano(123456000), false, 9300000020L);
            insertMessage(9300000022L, "USERS", month.plusSeconds(2), false, 9300000020L);
            insertMessage(9300000023L, "ALL_ENABLED_USERS", month.minusDays(1), false, 9300000020L);
            insertMessage(9300000024L, "ALL_ENABLED_USERS", month.plusSeconds(3), true, 9300000020L);
            insertMessage(9300000025L, "ALL_ENABLED_USERS", month.plusSeconds(4), false, 9300000029L);
            // 分类摘要独立于本月列表，且必须排除已读与其他收件人。
            LocalDateTime lowerBound = month.minusMonths(1);
            assertEquals(2, recipientMapper.countUnreadByAudienceCapped(9300000020L, lowerBound, 100, "ALL_ENABLED_USERS"));
            assertEquals(1, recipientMapper.countUnreadByAudienceCapped(9300000020L, lowerBound, 100, "USERS"));
            assertEquals(1, recipientMapper.countUnreadByAudienceCapped(9300000020L, lowerBound, 1, "ALL_ENABLED_USERS"));
            var form = new InboxCursorListForm();
            form.setUnreadOnly(true);
            form.setAudienceType("ALL_ENABLED_USERS");
            var rows = recipientMapper.selectCursorPage(9300000020L, month, form, 10, new sm.system.query.ListSqlQuery(java.util.List.of(), null, null));
            assertEquals(1, rows.size());
            assertEquals(9300000021L, rows.getFirst().getMessageId());
            assertTrue(rows.getFirst().getReceivedTime().endsWith(".123456"));
            form.setCursorTime(rows.getFirst().getReceivedTime());
            form.setCursorMessageId(rows.getFirst().getMessageId());
            assertTrue(recipientMapper.selectCursorPage(9300000020L, month, form, 10, new sm.system.query.ListSqlQuery(java.util.List.of(), null, null)).isEmpty());
        });
    }

    @Test
    void headerFiltersUseFullContentAndApplyBeforeCursorLimit() {
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            LocalDateTime received = LocalDateTime.now().withNano(123456000);
            insertMessage(9300000041L, "USERS", received, false, 9300000040L);
            insertMessage(9300000042L, "USERS", received.plusSeconds(1), false, 9300000040L);
            insertMessage(9300000043L, "USERS", received, false, 9300000049L);
            jdbc.update("UPDATE t_sys_inbox_message SET title=?, content=?, sender_name=? WHERE id IN (9300000041,9300000043)",
                    "过滤目标", "前".repeat(170) + "正文尾部", "测试发送人");
            var context = org.mockito.Mockito.mock(sm.system.security.context.CurrentUserContext.class);
            org.mockito.Mockito.when(context.getUserId()).thenReturn(9300000040L);
            var service = new InboxService(recipientMapper, org.mockito.Mockito.mock(InboxMessageTxService.class),
                    context, org.mockito.Mockito.mock(sm.domain.sys.base.sysparam.service.SysParamService.class));
            var form = new InboxCursorListForm();
            form.setPageSize(1);
            form.setFilters("""
                    [{"field":"title","type":"string","operator":"EQ","value":"过滤目标"},
                     {"field":"summary","type":"string","operator":"CONTAINS","value":"正文尾部"},
                     {"field":"senderName","type":"string","operator":"STARTS_WITH","value":"测试"},
                     {"field":"receivedTime","type":"date","operator":"EQ","value":"%s"}]
                    """.formatted(received.toLocalDate()));
            var page = service.list(form);
            assertEquals(1, page.records().size());
            assertEquals(9300000041L, page.records().getFirst().getMessageId());
            assertFalse(page.records().getFirst().getSummary().contains("正文尾部"));
            assertFalse(page.hasMore());
            form.setCursorTime(page.nextCursorTime());
            form.setCursorMessageId(page.nextCursorMessageId());
            assertTrue(service.list(form).records().isEmpty());
            form.setCursorTime(null);
            form.setCursorMessageId(null);
            form.setPageSize(20);
            insertMessage(9300000044L, "USERS", received.withDayOfMonth(1).minusDays(1), true, 9300000040L);
            form.setFilters("""
                    [{"field":"readStatus","type":"enum","operator":"IN","values":[true]}]
                    """);
            var readPage = service.list(form);
            assertEquals(1, readPage.records().size());
            assertEquals(9300000044L, readPage.records().getFirst().getMessageId());
            form.setFilters("""
                    [{"field":"readStatus","type":"enum","operator":"IN","values":[false]}]
                    """);
            assertEquals(2, service.list(form).records().size());
            form.setFilters("[{\"field\":\"userId\",\"operator\":\"EQ\",\"value\":9300000049}]");
            assertThrows(sm.system.exception.BizException.class, () -> service.list(form));
        });
    }

    private void insertMessage(long messageId, String audience, LocalDateTime received, boolean read, long userId) {
        jdbc.update("""
                INSERT INTO t_sys_inbox_message(id,scene_key,idempotency_key,title,content,status,audience_type,
                    publish_time,expire_time,create_time)
                VALUES (?,'verify-inbox',?,'验证消息','正文','PUBLISHED',?,now(),now()+interval '1 day',now())
                """, messageId, String.valueOf(messageId), audience);
        jdbc.update("INSERT INTO t_sys_inbox_recipient(message_id,user_id,received_time,read_status,read_time) VALUES (?,?,?,?,?)",
                messageId, userId, received, read, read ? received : null);
    }
}

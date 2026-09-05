package sm.domain.sys.message.inbox.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;
import sm.domain.sys.message.inbox.model.form.InboxReceiptKeyForm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InboxRecipientMapperXmlTests {
    private static final String RESOURCE = "mapper/sys/message/inbox/InboxRecipientMapper.xml";

    @Test
    void timelineQueryAlwaysUsesPartitionBoundaryAndStableCursor() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        String sharedResource = "mapper/common/ListSqlQueryMapper.xml";
        new XMLMapperBuilder(getClass().getClassLoader().getResourceAsStream(sharedResource), configuration,
                sharedResource, configuration.getSqlFragments()).parse();
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(RESOURCE);
        assertNotNull(mapperInput, "站内消息 Mapper XML 不存在");
        new XMLMapperBuilder(mapperInput, configuration, RESOURCE, configuration.getSqlFragments()).parse();
        InboxCursorListForm form = new InboxCursorListForm();
        form.setUnreadOnly(true);
        form.setAudienceType("USERS");
        form.setCursorTime("2026-08-01 12:00:00.123456");
        form.setCursorMessageId(30L);
        BoundSql boundSql = configuration
                .getMappedStatement(InboxRecipientMapper.class.getName() + ".selectCursorPage")
                .getBoundSql(Map.of("userId", 20L, "beginTime", java.time.LocalDateTime.of(2025, 8, 1, 0, 0),
                        "form", form, "limit", 21, "listQuery", new sm.system.query.ListSqlQuery(List.of(), null, null)));
        String sql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(sql.contains("a.received_time >= ?"));
        assertTrue(sql.contains("TO_CHAR(a.received_time, 'YYYY-MM-DD HH24:MI:SS.US')"));
        assertTrue(sql.contains("a.read_status = FALSE"));
        assertTrue(sql.contains("b.audience_type = ?"));
        assertTrue(sql.contains("(a.received_time, a.message_id) < (CAST(? AS timestamp), ?)"));
        assertTrue(sql.contains("ORDER BY a.received_time DESC, a.message_id DESC"));

        BoundSql detailSql = configuration
                .getMappedStatement(InboxRecipientMapper.class.getName() + ".selectDetail")
                .getBoundSql(Map.of("userId", 20L, "messageId", 30L,
                        "receivedTime", "2026-08-01 12:00:00.123456"));
        assertTrue(detailSql.getSql().replaceAll("\\s+", " ")
                .contains("a.received_time = CAST(? AS timestamp)"));

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("userId", 20L);
        updateParams.put("receipts", List.of(
                new InboxReceiptKeyForm(30L, "2026-08-01 12:00:00.123456")));
        updateParams.put("readStatus", false);
        updateParams.put("readTime", null);
        BoundSql updateSql = configuration
                .getMappedStatement(InboxRecipientMapper.class.getName() + ".updateReadStatus")
                .getBoundSql(updateParams);
        assertTrue(updateSql.getSql().replaceAll("\\s+", " ")
                .contains("(CAST(? AS timestamp), ?)"));
    }
}

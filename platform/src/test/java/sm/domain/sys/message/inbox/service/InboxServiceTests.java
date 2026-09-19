package sm.domain.sys.message.inbox.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.vo.InboxUnreadSummaryVO;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;
import sm.domain.sys.message.inbox.model.vo.InboxItemVO;
import sm.system.exception.BizException;
import org.mockito.ArgumentCaptor;
import java.util.List;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboxServiceTests {
    @Test
    void unreadSummaryUsesCurrentUserAndCapsBadgeAtNinetyNine() {
        InboxRecipientMapper mapper = mock(InboxRecipientMapper.class);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(20L);
        when(mapper.countUnreadCapped(eq(20L), any(LocalDateTime.class), eq(100))).thenReturn(100);
        when(mapper.countUnreadByAudienceCapped(eq(20L), any(LocalDateTime.class), eq(100), eq("ALL_ENABLED_USERS"))).thenReturn(100);
        when(mapper.countUnreadByAudienceCapped(eq(20L), any(LocalDateTime.class), eq(100), eq("USERS"))).thenReturn(7);
        SysParamService parameters = mock(SysParamService.class);
        when(parameters.getInt("INBOX_POLL_INTERVAL_SECONDS")).thenReturn(60);
        InboxService service = new InboxService(mapper, mock(InboxMessageTxService.class), context, parameters);

        InboxUnreadSummaryVO result = service.unreadSummary();

        assertEquals(99, result.unreadCount());
        assertTrue(result.overflow());
        assertEquals(60, result.pollingIntervalSeconds());
        assertEquals(100, result.announcementUnreadCount());
        assertEquals(7, result.businessUnreadCount());
        verify(mapper).countUnreadCapped(eq(20L), any(LocalDateTime.class), eq(100));
    }

    @Test
    void monthFilterBoundsQueryAndPreservesMicrosecondCursor() {
        InboxRecipientMapper mapper = mock(InboxRecipientMapper.class);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(20L);
        InboxCursorListForm form = new InboxCursorListForm();
        form.setMonthOnly(true);
        form.setPageSize(1);
        InboxItemVO item = new InboxItemVO();
        item.setMessageId(30L);
        item.setReceivedTime("2026-09-01 12:00:00.123456");
        when(mapper.selectCursorPage(eq(20L), any(), eq(form), eq(2), any(sm.system.query.ListSqlQuery.class))).thenReturn(List.of(item, new InboxItemVO()));
        var page = new InboxService(mapper, mock(InboxMessageTxService.class), context, mock(SysParamService.class)).list(form);
        ArgumentCaptor<LocalDateTime> boundary = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mapper).selectCursorPage(eq(20L), boundary.capture(), eq(form), eq(2), any(sm.system.query.ListSqlQuery.class));
        assertEquals(LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(), boundary.getValue());
        assertTrue(page.hasMore());
        assertEquals(item.getReceivedTime(), page.nextCursorTime());
    }

    @Test
    void zeroDisablesPollingAndUnsafeIntervalsFailClosed() {
        SysParamService parameters = mock(SysParamService.class);
        InboxService service = new InboxService(mock(InboxRecipientMapper.class), mock(InboxMessageTxService.class), mock(CurrentUserContext.class), parameters);
        when(parameters.getInt("INBOX_POLL_INTERVAL_SECONDS")).thenReturn(0);
        assertEquals(0, service.unreadSummary().pollingIntervalSeconds());
        for (Integer interval : new Integer[]{null, -1, 1, 9, 2147484}) {
            when(parameters.getInt("INBOX_POLL_INTERVAL_SECONDS")).thenReturn(interval);
            assertThrows(BizException.class, service::unreadSummary);
        }
    }
}

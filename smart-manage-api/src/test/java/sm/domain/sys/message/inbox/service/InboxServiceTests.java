package sm.domain.sys.message.inbox.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.vo.InboxUnreadSummaryVO;
import sm.system.security.context.CurrentUserContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        InboxService service = new InboxService(mapper, mock(InboxMessageTxService.class), context);

        InboxUnreadSummaryVO result = service.unreadSummary();

        assertEquals(99, result.unreadCount());
        assertTrue(result.overflow());
        verify(mapper).countUnreadCapped(eq(20L), any(LocalDateTime.class), eq(100));
    }
}

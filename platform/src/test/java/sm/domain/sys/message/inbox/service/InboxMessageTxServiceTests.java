package sm.domain.sys.message.inbox.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.message.inbox.mapper.InboxMessageMapper;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.entity.InboxMessageEntity;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboxMessageTxServiceTests {
    private final InboxMessageMapper messageMapper = mock(InboxMessageMapper.class);
    private final InboxRecipientMapper recipientMapper = mock(InboxRecipientMapper.class);
    private final InboxMessageTxService service = new InboxMessageTxService(messageMapper, recipientMapper,
            new sm.domain.sys.base.user.service.UserReferenceService(mock(UserMapper.class)),
            mock(CurrentUserContext.class));

    @Test
    void distributeCommitsRecipientSnapshotBeforePublishedState() {
        InboxMessageEntity message = message("PUBLISHING", 3);
        when(messageMapper.selectById(10L)).thenReturn(message);
        when(recipientMapper.insertEnabledRecipients(eq(10L), any(LocalDateTime.class))).thenReturn(120);
        when(messageMapper.completePublish(eq(10L), any(LocalDateTime.class), eq(120L))).thenReturn(1);

        service.distribute(10L);

        InOrder order = inOrder(recipientMapper, messageMapper);
        order.verify(recipientMapper).insertEnabledRecipients(eq(10L), any(LocalDateTime.class));
        order.verify(messageMapper).completePublish(eq(10L), any(LocalDateTime.class), eq(120L));
    }

    @Test
    void queuePublishRejectsNonDraftWithoutSideEffect() {
        when(messageMapper.selectById(10L)).thenReturn(message("PUBLISHED", 3));

        assertThrows(BizException.class, () -> service.queuePublish(10L, 3));

        verify(messageMapper, never()).updateById(any(InboxMessageEntity.class));
    }

    private static InboxMessageEntity message(String status, int version) {
        InboxMessageEntity message = new InboxMessageEntity();
        message.setId(10L);
        message.setStatus(status);
        message.setVersion(version);
        message.setExpireTime(LocalDateTime.now().plusDays(1));
        return message;
    }
}

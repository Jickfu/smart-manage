package sm.domain.sys.message.inbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;
import sm.domain.sys.message.inbox.model.form.InboxMarkReadForm;
import sm.domain.sys.message.inbox.model.form.InboxReceiptKeyForm;
import sm.domain.sys.message.inbox.model.vo.InboxCursorPageVO;
import sm.domain.sys.message.inbox.model.vo.InboxDetailVO;
import sm.domain.sys.message.inbox.model.vo.InboxItemVO;
import sm.domain.sys.message.inbox.model.vo.InboxUnreadSummaryVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InboxService {
    private static final int UNREAD_CAP = 100;
    private final InboxRecipientMapper recipientMapper;
    private final InboxMessageTxService txService;
    private final CurrentUserContext currentUserContext;

    public InboxUnreadSummaryVO unreadSummary() {
        int count = recipientMapper.countUnreadCapped(currentUserContext.getUserId(), beginTime(), UNREAD_CAP);
        return new InboxUnreadSummaryVO(Math.min(count, 99), count >= UNREAD_CAP);
    }

    public InboxCursorPageVO list(InboxCursorListForm form) {
        validateCursor(form);
        int pageSize = form.safePageSize();
        List<InboxItemVO> records = recipientMapper.selectCursorPage(currentUserContext.getUserId(),
                beginTime(), form, pageSize + 1);
        boolean hasMore = records.size() > pageSize;
        List<InboxItemVO> pageRecords = hasMore ? records.subList(0, pageSize) : records;
        InboxItemVO last = pageRecords.isEmpty() ? null : pageRecords.get(pageRecords.size() - 1);
        return new InboxCursorPageVO(pageRecords, hasMore,
                last == null ? null : last.getReceivedTime(), last == null ? null : last.getMessageId());
    }

    public InboxDetailVO detail(InboxReceiptKeyForm form) {
        InboxDetailVO detail = recipientMapper.selectDetail(currentUserContext.getUserId(),
                form.messageId(), form.receivedTime());
        if (detail == null) throw new BizException(ResultEnum.NOT_FOUND, "消息不存在、已过期或不属于当前用户");
        return detail;
    }

    public void markRead(InboxMarkReadForm form) {
        txService.updateReadStatus(currentUserContext.getUserId(), form.receipts(), true);
    }

    public void markUnread(InboxMarkReadForm form) {
        txService.updateReadStatus(currentUserContext.getUserId(), form.receipts(), false);
    }

    public void markAllRead() {
        txService.markAllRead(currentUserContext.getUserId(), beginTime());
    }

    private static LocalDateTime beginTime() {
        return LocalDateTime.now().minusDays(365);
    }

    private static void validateCursor(InboxCursorListForm form) {
        if ((form.getCursorTime() == null) != (form.getCursorMessageId() == null)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "消息游标时间和消息ID必须同时提供");
        }
    }
}

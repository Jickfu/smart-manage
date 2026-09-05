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
import sm.domain.sys.base.sysparam.service.SysParamService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import sm.system.query.ListSqlQuery;
import sm.system.form.PageForm;

@Service
@RequiredArgsConstructor
public class InboxService {
    private static final int UNREAD_CAP = 100;
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "readStatus", ListSqlQuery.bool("a.read_status", false),
            "title", ListSqlQuery.string("b.title", false),
            // 列表显示摘要，内容筛选始终检索完整正文，不能只匹配前160字。
            "summary", ListSqlQuery.string("b.content", false),
            "senderName", ListSqlQuery.string("b.sender_name", false),
            "receivedTime", ListSqlQuery.dateTime("a.received_time", false));
    private final InboxRecipientMapper recipientMapper;
    private final InboxMessageTxService txService;
    private final CurrentUserContext currentUserContext;
    private final SysParamService sysParamService;

    public InboxUnreadSummaryVO unreadSummary() {
        Long userId = currentUserContext.getUserId();
        LocalDateTime lowerBound = beginTime();
        int count = recipientMapper.countUnreadCapped(userId, lowerBound, UNREAD_CAP);
        Integer interval = sysParamService.getInt("INBOX_POLL_INTERVAL_SECONDS");
        if (interval == null || interval < 0 || (interval > 0 && interval < 10) || interval > 2147483) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "消息轮询间隔必须为0或不小于10秒的有效整数");
        }
        // 各类别独立封顶，不能从已封顶的总数相减推算另一类别。
        int announcementCount = recipientMapper.countUnreadByAudienceCapped(userId, lowerBound, UNREAD_CAP, "ALL_ENABLED_USERS");
        int businessCount = recipientMapper.countUnreadByAudienceCapped(userId, lowerBound, UNREAD_CAP, "USERS");
        return new InboxUnreadSummaryVO(Math.min(count, 99), count >= UNREAD_CAP, interval,
                announcementCount, businessCount);
    }

    public InboxCursorPageVO list(InboxCursorListForm form) {
        validateCursor(form);
        int pageSize = form.safePageSize();
        PageForm filterForm = new PageForm();
        filterForm.setFilters(form.getFilters());
        ListSqlQuery listQuery = ListSqlQuery.of(filterForm, LIST_FIELDS);
        LocalDateTime lowerBound = Boolean.TRUE.equals(form.getMonthOnly())
                ? LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay() : beginTime();
        List<InboxItemVO> records = recipientMapper.selectCursorPage(currentUserContext.getUserId(),
                lowerBound, form, pageSize + 1, listQuery);
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

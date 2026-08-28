package sm.domain.sys.message.inbox.model.vo;

/** Header 未读角标，计数最多到100。 */
public record InboxUnreadSummaryVO(int unreadCount, boolean overflow) {
}


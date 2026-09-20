package sm.domain.sys.message.inbox.model.vo;

/** 未读摘要：总角标通过 overflow 表达封顶，分类计数达到100时展示99+。 */
public record InboxUnreadSummaryVO(int unreadCount, boolean overflow, int pollingIntervalSeconds,
        int announcementUnreadCount, int businessUnreadCount) {
}

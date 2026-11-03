package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 未读摘要：总角标通过 overflow 表达封顶，分类计数达到100时展示99+。
 */
@Schema(description = "站内消息收件箱未读摘要输出模型")
public record InboxUnreadSummaryVO(
    @Schema(description = "未读消息数量")
    int unreadCount,
    @Schema(description = "未读数量是否超过统计上限")
    boolean overflow,
    @Schema(description = "建议的消息轮询间隔，单位秒")
    int pollingIntervalSeconds,
    @Schema(description = "未读公告数量")
    int announcementUnreadCount,
    @Schema(description = "未读业务消息数量")
    int businessUnreadCount) {
}

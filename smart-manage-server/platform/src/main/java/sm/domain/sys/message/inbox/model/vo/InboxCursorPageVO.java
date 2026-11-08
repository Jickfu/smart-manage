package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 游标分页结果，避免深分页扫描。
 */
@Schema(description = "站内消息收件箱游标分页输出模型")
public record InboxCursorPageVO(
    @Schema(description = "查询结果记录列表")
    List<InboxItemVO> records,
    @Schema(description = "是否还有下一页")
    boolean hasMore,
    @Schema(description = "下一页游标接收时间，与 nextCursorMessageId 配对使用")
    String nextCursorTime,
    @Schema(description = "下一页游标消息 ID，与 nextCursorTime 配对使用")
    Long nextCursorMessageId) {
}

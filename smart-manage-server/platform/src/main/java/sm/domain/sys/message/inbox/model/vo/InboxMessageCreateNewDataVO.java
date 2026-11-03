package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 管理员新增消息默认值。
 */
@Schema(description = "站内消息新增默认值输出模型")
public record InboxMessageCreateNewDataVO(
    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    String level,
    @Schema(description = "失效时间")
    LocalDateTime expireTime) {
}


package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户消息时间线条目。
 */
@Schema(description = "站内消息收件箱条目输出模型")
@Data
public class InboxItemVO {
    @Schema(description = "消息ID")
    private Long messageId;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    private String level;
    @Schema(description = "发布人名称快照")
    private String senderName;
    @Schema(description = "是否已读")
    private Boolean readStatus;
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
    /**
     * 数据库微秒精度的稳定收件键，不能经过全局秒级时间序列化
     */
    @Schema(description = "收件时间")
    private String receivedTime;
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;
}

package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户消息详情。
 */
@Schema(description = "站内消息收件箱详情输出模型")
@Data
public class InboxDetailVO {
    @Schema(description = "消息ID")
    private Long messageId;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "消息正文")
    private String content;
    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    private String level;
    @Schema(description = "发布人名称快照")
    private String senderName;
    @Schema(description = "是否已读")
    private Boolean readStatus;
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
    /**
     * 数据库微秒精度的稳定收件键
     */
    @Schema(description = "收件时间")
    private String receivedTime;
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;
    @Schema(description = "资源类型")
    private String resourceType;
    @Schema(description = "关联业务资源ID")
    private String resourceId;
    @Schema(description = "关联业务动作编码")
    private String actionCode;
    @Schema(description = "关联业务动作参数")
    private String actionPayload;
}

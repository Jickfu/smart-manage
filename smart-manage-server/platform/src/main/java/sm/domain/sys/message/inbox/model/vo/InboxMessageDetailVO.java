package sm.domain.sys.message.inbox.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端消息详情。
 */
@Schema(description = "站内消息详情输出模型")
@Data
public class InboxMessageDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "消息正文")
    private String content;
    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    private String level;
    @Schema(description = "消息发布状态：DRAFT、PENDING、PUBLISHING、PUBLISHED、FAILED")
    private String status;
    @Schema(description = "发布人名称快照")
    private String senderName;
    @Schema(description = "收件人数")
    private Long recipientCount;
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "错误说明")
    private String errorMessage;
}


package sm.domain.sys.message.inbox.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员消息草稿保存表单。
 */
@Schema(description = "站内消息保存输入表单")
@Data
public class InboxMessageSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;

    @Schema(description = "标题")
    @NotBlank(message = "消息标题不能为空")
    @Size(max = 200, message = "消息标题不能超过200个字符")
    private String title;

    @Schema(description = "消息正文")
    @NotBlank(message = "消息正文不能为空")
    @Size(max = 10000, message = "消息正文不能超过10000个字符")
    private String content;

    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    @NotBlank(message = "消息级别不能为空")
    private String level;

    @Schema(description = "失效时间")
    @NotNull(message = "失效时间不能为空")
    @Future(message = "失效时间必须晚于当前时间")
    private LocalDateTime expireTime;
}


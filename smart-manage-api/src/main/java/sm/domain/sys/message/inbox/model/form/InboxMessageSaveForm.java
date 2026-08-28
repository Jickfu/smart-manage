package sm.domain.sys.message.inbox.model.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理员消息草稿保存表单。 */
@Data
public class InboxMessageSaveForm {
    private Long id;
    private Integer version;

    @NotBlank(message = "消息标题不能为空")
    @Size(max = 200, message = "消息标题不能超过200个字符")
    private String title;

    @NotBlank(message = "消息正文不能为空")
    @Size(max = 10000, message = "消息正文不能超过10000个字符")
    private String content;

    @NotBlank(message = "消息级别不能为空")
    private String level;

    @NotNull(message = "失效时间不能为空")
    @Future(message = "失效时间必须晚于当前时间")
    private LocalDateTime expireTime;
}


package sm.domain.sys.message.inbox.model.form;

import lombok.Data;
import jakarta.validation.constraints.Pattern;

/** 当前用户消息游标查询。 */
@Data
public class InboxCursorListForm {
    private Integer pageSize = 20;
    private Boolean unreadOnly = false;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}",
            message = "消息游标时间格式不正确")
    private String cursorTime;
    private Long cursorMessageId;

    public int safePageSize() {
        if (pageSize == null || pageSize < 1) return 20;
        return Math.min(pageSize, 100);
    }
}

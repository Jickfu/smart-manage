package sm.domain.sys.message.inbox.model.form;

import lombok.Data;
import jakarta.validation.constraints.Pattern;

/** 当前用户消息游标查询。 */
@Data
public class InboxCursorListForm {
    private Integer pageSize = 20;
    /** 与普通列表共用过滤协议，字段由服务端白名单限定。 */
    private String filters;
    private Boolean unreadOnly = false;
    private Boolean monthOnly = false;
    /** 分类直接映射既有收件范围，不以标题或场景名称推断。 */
    @Pattern(regexp = "ALL_ENABLED_USERS|USERS", message = "消息类型不正确")
    private String audienceType;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}",
            message = "消息游标时间格式不正确")
    private String cursorTime;
    private Long cursorMessageId;

    public int safePageSize() {
        if (pageSize == null || pageSize < 1) return 20;
        return Math.min(pageSize, 100);
    }
}

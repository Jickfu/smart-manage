package sm.domain.sys.message.inbox.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 管理端站内消息列表条件。
 */
@Schema(description = "站内消息列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class InboxMessageListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "消息发布状态：DRAFT、PENDING、PUBLISHING、PUBLISHED、FAILED")
    private String status;
    @Schema(description = "消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急")
    private String level;
}


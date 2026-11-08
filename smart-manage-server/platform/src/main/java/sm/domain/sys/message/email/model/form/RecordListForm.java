package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "邮件投递记录列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecordListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "邮件任务状态：PENDING、SENDING、SUCCESS、RETRY_WAIT、FAILED、UNKNOWN、CANCELLED")
    private String status;
    @Schema(description = "发信账号 ID")
    private Long accountId;
}

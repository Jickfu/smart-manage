package sm.domain.sys.base.attachment.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 附件备注更新参数。 */
@Data
public class AttachmentRemarkUpdateForm {

    @NotNull(message = "附件 id 不能为空")
    private Long id;

    @NotNull(message = "业务附件关联 id 不能为空")
    private Long businessAttachmentId;

    @Size(max = 500, message = "附件备注不能超过 500 个字符")
    private String remark;
}

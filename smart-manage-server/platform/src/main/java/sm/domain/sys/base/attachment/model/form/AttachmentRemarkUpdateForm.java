package sm.domain.sys.base.attachment.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 附件备注更新参数。
 */
@Schema(description = "业务附件备注修改输入表单")
@Data
public class AttachmentRemarkUpdateForm {

    @Schema(description = "ID")
    @NotNull(message = "附件 id 不能为空")
    private Long id;

    @Schema(description = "业务附件关联 ID，不是底层文件 ID")
    @NotNull(message = "业务附件关联 id 不能为空")
    private Long businessAttachmentId;

    @Schema(description = "备注")
    @Size(max = 500, message = "附件备注不能超过 500 个字符")
    private String remark;
}

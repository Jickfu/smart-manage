package sm.domain.sys.base.attachment.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 按业务对象查询附件的接口入参。 */
@Data
public class AttachmentListForm {

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "业务单据ID不能为空")
    private String bizId;
}

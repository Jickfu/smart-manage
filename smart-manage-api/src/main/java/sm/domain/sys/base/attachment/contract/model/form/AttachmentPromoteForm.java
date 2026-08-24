package sm.domain.sys.base.attachment.contract.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 附件提升表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "附件提升表单")
public class AttachmentPromoteForm {

    @NotEmpty(message = "附件ID列表不能为空")
    @Schema(description = "附件ID列表")
    private List<Long> attachmentIds;

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型")
    private String bizType;

    @NotBlank(message = "业务单据ID不能为空")
    @Schema(description = "业务单据ID")
    private String bizId;

    @Schema(description = "临时附件ID到上传会话ID的映射")
    private Map<Long, String> uploadSessions;
}

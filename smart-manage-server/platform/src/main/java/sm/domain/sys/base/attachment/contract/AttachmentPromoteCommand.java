package sm.domain.sys.base.attachment.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 附件由临时状态提升并绑定业务对象的稳定命令。 */
@Data
public class AttachmentPromoteCommand {

    @NotEmpty(message = "附件ID列表不能为空")
    private List<Long> attachmentIds;

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "业务单据ID不能为空")
    private String bizId;

    private Map<Long, String> uploadSessions;
}

package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/** 当前用户可自行维护的基础资料。 */
@Data
public class CurrentUserProfileForm {
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Long avatarAttachmentId;
    private Map<Long, String> attachmentUploadSessions = Map.of();
}

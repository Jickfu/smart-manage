package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;
import java.time.LocalDate;
import sm.domain.sys.base.user.model.Gender;

/**
 * 当前用户可自行维护的基础资料。
 */
@Schema(description = "当前用户个人资料输入表单")
@Data
public class CurrentUserProfileForm {
    @Schema(description = "名称")
    @NotBlank(message = "姓名不能为空")
    private String name;
    @Schema(description = "性别")
    private Gender gender;
    @Schema(description = "生日")
    private LocalDate birthday;
    @Schema(description = "头像附件 ID")
    private Long avatarAttachmentId;
    @Schema(description = "附件上传会话映射，键为附件 ID，值为上传会话标识；用于确认临时附件归属")
    private Map<Long, String> attachmentUploadSessions = Map.of();
}

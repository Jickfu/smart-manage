package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.user.constant.UserPermission;
import sm.domain.sys.base.user.model.Gender;
import sm.system.json.masking.Masked;
import sm.system.json.masking.MaskingType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 后台用户详情；联系方式在 JSON 出站边界按独立权限决定是否脱敏。 */
@Data
@Schema(title = "用户详情")
public class UserDetailVO {
    private Long id;
    private String username;
    private String name;
    private String number;
    private Gender gender;
    private LocalDate birthday;
    private String avatar;
    private Long avatarAttachmentId;
    private String themeColor;

    @Masked(type = MaskingType.EMAIL, revealPermission = UserPermission.READ_SENSITIVE)
    private String email;

    @Masked(type = MaskingType.PHONE, revealPermission = UserPermission.READ_SENSITIVE)
    private String phone;

    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version;
    private List<UserAssignmentVO> assignments;
}

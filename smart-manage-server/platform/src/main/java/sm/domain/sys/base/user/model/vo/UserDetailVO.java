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

/**
 * 后台用户详情；联系方式在 JSON 出站边界按独立权限决定是否脱敏。
 */
@Data
@Schema(description = "用户详情")
public class UserDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "用户姓名")
    private String name;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "性别")
    private Gender gender;
    @Schema(description = "生日")
    private LocalDate birthday;
    @Schema(description = "头像访问 URL")
    private String avatar;
    @Schema(description = "头像附件 ID")
    private Long avatarAttachmentId;
    @Schema(description = "主题色")
    private String themeColor;

    @Schema(description = "邮箱地址，默认脱敏，具有敏感信息读取权限时返回明文")
    @Masked(type = MaskingType.EMAIL, revealPermission = UserPermission.READ_SENSITIVE)
    private String email;

    @Schema(description = "手机号码，默认脱敏，具有敏感信息读取权限时返回明文")
    @Masked(type = MaskingType.PHONE, revealPermission = UserPermission.READ_SENSITIVE)
    private String phone;

    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "用户任职明细")
    private List<UserAssignmentVO> assignments;
}

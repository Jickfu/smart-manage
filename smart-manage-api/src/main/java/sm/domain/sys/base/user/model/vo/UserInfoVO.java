package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import sm.domain.sys.base.user.model.Gender;

/**
 * 用户信息面板
 *
 * @author Chekfu
 */
@Data
@Schema(title = "用户信息面板")
public class UserInfoVO {

	@Schema(description = "用户 id")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "姓名")
	private String name;
	private String number;
	private Gender gender;
	private LocalDate birthday;

	@Schema(description = "头像")
	private String avatar;
	private Long avatarAttachmentId;

	@Schema(description = "主题颜色")
	private String themeColor;

	private String email;
	private LocalDateTime emailVerifiedAt;

	private String phone;

	private Boolean enabled;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private Integer version;

	@Schema(description = "当前组织下的角色ID列表")
	private List<Long> roleIds;

	@Schema(description = "当前组织ID")
	private Long currentOrgId;

	@Schema(description = "当前组织名称")
	private String currentOrgName;

	@Schema(description = "当前组织所属公司名称")
	private String companyName;

	private List<UserAssignmentVO> assignments;

}

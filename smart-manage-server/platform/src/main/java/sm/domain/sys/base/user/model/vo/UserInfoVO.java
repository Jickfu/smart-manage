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
@Schema(description = "用户信息面板")
public class UserInfoVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "当前会话是否为特殊管理员身份")
	private boolean administrator;

	@Schema(description = "姓名")
	private String name;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "性别")
	private Gender gender;
	@Schema(description = "生日")
	private LocalDate birthday;

	@Schema(description = "头像")
	private String avatar;
	@Schema(description = "头像附件 ID")
	private Long avatarAttachmentId;

	@Schema(description = "主题颜色")
	private String themeColor;

	@Schema(description = "邮箱地址")
	private String email;
	@Schema(description = "邮箱验证时间")
	private LocalDateTime emailVerifiedAt;

	@Schema(description = "手机号码")
	private String phone;

	@Schema(description = "是否启用")
	private Boolean enabled;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "乐观锁版本号")
	private Integer version;

	@Schema(description = "当前组织下的角色ID列表")
	private List<Long> roleIds;

	@Schema(description = "当前组织ID")
	private Long currentOrgId;

	@Schema(description = "当前组织名称")
	private String currentOrgName;

	@Schema(description = "当前组织所属公司名称")
	private String companyName;

	@Schema(description = "当前组织所在组织树的绝对顶层组织名称")
	private String rootOrgName;

	@Schema(description = "用户任职明细")
	private List<UserAssignmentVO> assignments;

}

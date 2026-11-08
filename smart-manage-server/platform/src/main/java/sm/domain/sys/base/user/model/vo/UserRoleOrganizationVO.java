package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

import java.util.List;

/**
 * 用户一个任职组织及其精确角色分配结果。
 */
@Schema(description = "用户角色分配组织输出模型")
@Data
public class UserRoleOrganizationVO {
	@Schema(description = "任职组织引用")
	private ReferenceVO org;
	@Schema(description = "组织名称路径")
	private String orgNamePath;
	@Schema(description = "任职岗位名称")
	private String position;
	@Schema(description = "是否为主职")
	private Boolean isPrimary;
	@Schema(description = "已分配角色列表")
	private List<UserAssignedRoleVO> roles;
}

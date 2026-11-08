package sm.domain.sys.base.user.model.vo;

import sm.domain.sys.base.common.model.vo.ReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

/**
 * 用户角色分配工作区，一次返回用户摘要、全部任职组织和精确角色关系。
 */
@Schema(description = "用户角色分配工作区输出模型")
@Data
public class UserRoleAssignmentWorkspaceVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "用户名")
	private String username;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "可分配角色的任职组织列表")
	private List<UserRoleOrganizationVO> organizations;
    @Schema(description = "自动继承的默认角色，与手工分配独立，空表示未设置或目标为管理员")
    private ReferenceVO defaultRole;
}

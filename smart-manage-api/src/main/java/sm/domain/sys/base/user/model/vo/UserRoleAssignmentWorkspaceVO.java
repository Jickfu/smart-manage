package sm.domain.sys.base.user.model.vo;

import lombok.Data;

import java.util.List;

/** 用户角色分配工作区，一次返回用户摘要、全部任职组织和精确角色关系。 */
@Data
public class UserRoleAssignmentWorkspaceVO {
	private Long id;
	private String name;
	private String username;
	private String number;
	private List<UserRoleOrganizationVO> organizations;
}

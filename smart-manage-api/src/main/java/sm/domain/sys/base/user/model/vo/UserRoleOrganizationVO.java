package sm.domain.sys.base.user.model.vo;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

import java.util.List;

/** 用户一个任职组织及其精确角色分配结果。 */
@Data
public class UserRoleOrganizationVO {
	private ReferenceVO org;
	private String orgNamePath;
	private String position;
	private Boolean isPrimary;
	private List<UserAssignedRoleVO> roles;
}

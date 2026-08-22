package sm.domain.sys.base.user.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** 用户角色分配工作区中的已分配角色记录。 */
@Data
public class UserAssignedRoleVO {
	@JsonIgnore
	private Long orgId;
	private Long id;
	private String number;
	private String name;
	private String description;
}

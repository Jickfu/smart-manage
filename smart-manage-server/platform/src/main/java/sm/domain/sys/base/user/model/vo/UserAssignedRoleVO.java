package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 用户角色分配工作区中的已分配角色记录。
 */
@Schema(description = "用户已分配角色输出模型")
@Data
public class UserAssignedRoleVO {
	@Schema(description = "组织 ID")
	@JsonIgnore
	private Long orgId;
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "说明")
	private String description;
}

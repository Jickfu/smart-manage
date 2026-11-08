package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
@Schema(description = "角色列表视图")
public class RoleListVO {
	@Schema(description = "ID")
	private Long id;

	@Schema(description = "角色名称")
	private String name;

	@Schema(description = "角色编号")
	private String number;

	@Schema(description = "描述")
	private String description;
}

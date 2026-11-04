package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色-基础资料选择-列表项。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "角色-基础资料选择-列表项")
public class RoleSelectVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "说明")
	private String description;
}

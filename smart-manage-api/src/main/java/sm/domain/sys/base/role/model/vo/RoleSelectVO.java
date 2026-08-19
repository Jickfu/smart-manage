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
	private Long id;
	private String number;
	private String name;
	private String description;
}

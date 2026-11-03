package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;

/**
 * 菜单-基础资料选择-列表项。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "菜单-基础资料选择-列表项")
public class MenuSelectVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "级别")
	private MenuLevelEnum level;
	@Schema(description = "是否启用")
	private Boolean enabled;
}

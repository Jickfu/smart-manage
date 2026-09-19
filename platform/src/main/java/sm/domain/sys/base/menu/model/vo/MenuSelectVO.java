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
	private Long id;
	private String number;
	private String name;
	private MenuLevelEnum level;
	private Boolean enabled;
}

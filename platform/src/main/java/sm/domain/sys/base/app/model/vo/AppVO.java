package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 精简的应用信息
 */
@Data
@Schema(description = "应用信息")
public class AppVO {
	@Schema(description = "id")
	private Long id;

	@Schema(description = "所属领域编号（t_sys_domain.number）")
	private String domainNumber;

	@Schema(description = "编号")
	private String number;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "图标颜色（SVG fill）")
	private String iconColor;

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "描述")
	private String description;
}


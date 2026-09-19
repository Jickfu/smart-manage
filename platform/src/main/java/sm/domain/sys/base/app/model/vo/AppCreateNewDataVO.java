package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用新增默认值
 */
@Data
@Schema(title = "应用新增默认值")
public class AppCreateNewDataVO {

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "图标颜色")
	private String iconColor;

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "启用")
	private Boolean enabled;
}


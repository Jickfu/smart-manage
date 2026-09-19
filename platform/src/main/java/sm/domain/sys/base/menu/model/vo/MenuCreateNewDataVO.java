package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单新增默认值
 */
@Data
@Schema(title = "菜单新增默认值")
public class MenuCreateNewDataVO {
	@Schema(description = "父级 id")
	private Long parentId;

	@Schema(description = "排序")
	private Integer sort;

	@Schema(description = "是否启用")
	private Boolean enabled;
}


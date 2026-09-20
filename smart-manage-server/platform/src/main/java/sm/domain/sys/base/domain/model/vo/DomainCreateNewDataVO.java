package sm.domain.sys.base.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 领域新增默认值
 */
@Data
@Schema(title = "领域新增默认值")
public class DomainCreateNewDataVO {

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "启用")
	private Boolean enabled;
}


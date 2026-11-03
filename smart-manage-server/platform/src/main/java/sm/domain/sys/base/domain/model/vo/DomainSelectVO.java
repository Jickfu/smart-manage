package sm.domain.sys.base.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "领域-基础资料选择-列表项")
public class DomainSelectVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "是否启用")
	private Boolean enabled;
}


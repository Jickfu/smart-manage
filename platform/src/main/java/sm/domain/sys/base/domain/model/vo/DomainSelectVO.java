package sm.domain.sys.base.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "领域-基础资料选择-列表项")
public class DomainSelectVO {
	private Long id;
	private String name;
	private String number;
	private Boolean enabled;
}


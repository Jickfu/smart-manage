package sm.domain.sys.base.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "领域管理-列表项")
public class DomainListVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "排序号")
	private Integer seq;
	@Schema(description = "是否启用")
	private Boolean enabled;
	@Schema(description = "创建时间")
	private LocalDateTime createTime;
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;
}


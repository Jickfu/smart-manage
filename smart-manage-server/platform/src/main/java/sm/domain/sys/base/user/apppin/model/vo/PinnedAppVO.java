package sm.domain.sys.base.user.apppin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "固定应用输出模型")
@Data
public class PinnedAppVO {
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "排序号")
	private Integer seq;
}

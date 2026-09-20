package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 领域及其下应用信息
 */
@Data
@Schema(description = "领域及应用")
public class DomainAppsVO {
	@Schema(description = "id")
	private Long id;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "编号")
	private String number;

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "应用列表")
	private List<AppVO> appList;
}


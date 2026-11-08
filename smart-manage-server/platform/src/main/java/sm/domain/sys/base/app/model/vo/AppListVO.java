package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用管理-列表项")
public class AppListVO {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "图标")
	private String icon;
	@Schema(description = "图标颜色（SVG fill）")
	private String iconColor;
	@Schema(description = "排序号")
	private Integer seq;
	@Schema(description = "说明")
	private String description;
	@Schema(description = "所属领域ID")
	private Long domainId;
	@Schema(description = "所属领域名称")
	private String domainName;
	@Schema(description = "是否启用")
	private Boolean enabled;
	@Schema(description = "创建时间")
	private LocalDateTime createTime;
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;
}


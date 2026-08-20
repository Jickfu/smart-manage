package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用管理-详情")
public class AppDetailVO {
	@Data
	@Schema(description = "所属领域")
	public static class DomainRef {
		@Schema(description = "id")
		private Long id;
		@Schema(description = "编码")
		private String number;
		@Schema(description = "名称")
		private String name;
	}

	@Schema(description = "id")
	private Long id;

	@Schema(description = "乐观锁版本号")
	private Integer version;

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

	private DomainRef domain;

	@Schema(description = "启用")
	private Boolean enabled;

	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}


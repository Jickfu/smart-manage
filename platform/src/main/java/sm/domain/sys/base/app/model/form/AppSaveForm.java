package sm.domain.sys.base.app.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "应用管理-保存")
public class AppSaveForm {
	@Schema(description = "id，为空则新增")
	private Long id;

	@Schema(description = "乐观锁版本号，修改时必传")
	private Integer version;

	@NotBlank(message = "名称不能为空")
	@Schema(description = "名称")
	private String name;

	@NotBlank(message = "编码不能为空")
	@Schema(description = "编码")
	private String number;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "图标颜色（SVG fill）")
	private String iconColor;

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "描述")
	private String description;

	@NotNull(message = "domainId 不能为空")
	@Schema(description = "领域 id")
	private Long domainId;

}


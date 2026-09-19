package sm.domain.sys.base.permission.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
@Schema(title = "权限保存")
public class PermissionSaveForm {
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

	@NotNull(message = "功能不能为空")
	@Schema(description = "功能ID")
	private Long featureId;
}

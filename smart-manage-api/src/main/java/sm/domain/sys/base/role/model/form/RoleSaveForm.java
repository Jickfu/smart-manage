package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


/**
 * 角色保存表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "角色保存表单")
public class RoleSaveForm {

	@Schema(description = "id，为空则新增")
	private Long id;

	@Schema(description = "乐观锁版本号，修改时必传")
	private Integer version;

	@Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "角色名称不能为空")
	private String name;

	@Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "角色编码不能为空")
	private String number;

	@Schema(description = "描述")
	private String description;

	@NotBlank(message = "默认数据范围不能为空")
	@Pattern(regexp = "ALL|ORG_AND_CHILDREN|ORG|SELF", message = "默认数据范围非法")
	private String defaultDataScope;

}

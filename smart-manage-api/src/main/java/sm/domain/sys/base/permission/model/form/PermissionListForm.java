package sm.domain.sys.base.permission.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "权限列表表单")
public class PermissionListForm extends PageForm {
	@Schema(description = "按应用筛选")
	private Long appId;

	@Schema(description = "按功能筛选")
	private Long featureId;

	@Schema(description = "按领域筛选")
	private Long domainId;

	@Schema(description = "关键词（名称、编码模糊匹配）")
	private String keyword;
}

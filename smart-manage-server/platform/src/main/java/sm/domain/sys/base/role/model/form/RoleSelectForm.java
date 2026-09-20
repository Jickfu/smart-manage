package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

import java.util.List;

/**
 * 角色-基础资料选择-分页查询表单。
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色-基础资料选择-分页查询")
public class RoleSelectForm extends PageForm {
	@Schema(description = "关键词（编码、名称、描述模糊匹配）")
	private String keyword;

	@Schema(description = "当前组织ID，用于明确候选范围上下文")
	private Long orgId;

	@Schema(description = "需要从候选范围排除的角色ID")
	private List<Long> excludedIds;
}

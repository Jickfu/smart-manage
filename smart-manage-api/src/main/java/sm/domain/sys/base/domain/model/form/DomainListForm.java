package sm.domain.sys.base.domain.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "领域管理-分页查询")
public class DomainListForm extends PageForm {
	@Schema(description = "关键字（名称、编码模糊匹配）")
	private String keyword;

	@Schema(description = "启用状态")
	private Boolean enabled;
}

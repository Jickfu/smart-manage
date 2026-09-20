package sm.domain.sys.base.domain.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "领域-基础资料选择-分页查询")
public class DomainSelectForm extends PageForm {
	@Schema(description = "关键词（名称、编码模糊匹配）")
	private String keyword;

	@Schema(description = "是否启用（默认 true）")
	private Boolean enabled = true;
}


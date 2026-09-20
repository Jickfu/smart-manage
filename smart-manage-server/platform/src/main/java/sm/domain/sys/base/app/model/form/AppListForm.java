package sm.domain.sys.base.app.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用管理-分页查询")
public class AppListForm extends PageForm {
	@Schema(description = "按领域筛选")
	private Long domainId;

	@Schema(description = "关键词（名称、编码模糊匹配）")
	private String keyword;
}


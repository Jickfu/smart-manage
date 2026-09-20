package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/** 菜单管理树形列表筛选条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单树形列表表单")
public class MenuTreeListForm extends PageForm {
    @Schema(description = "按领域筛选")
    private Long domainId;

    @Schema(description = "按应用筛选")
    private Long appId;

    @Schema(description = "按功能筛选")
    private Long featureId;

    @Schema(description = "关键词（名称、路径或外部链接模糊匹配）")
    private String keyword;
}

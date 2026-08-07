package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 菜单管理树形列表筛选条件。 */
@Data
@Schema(description = "菜单树形列表表单")
public class MenuTreeListForm {
    @Schema(description = "按云筛选")
    private Long cloudId;

    @Schema(description = "按应用筛选")
    private Long appId;

    @Schema(description = "关键词（名称、路径模糊匹配）")
    private String keyword;
}

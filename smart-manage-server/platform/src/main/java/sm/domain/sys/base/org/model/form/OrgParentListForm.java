package sm.domain.sys.base.org.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "组织上级组织列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgParentListForm extends PageForm {
    @Schema(description = "父ID")
    private Long parentId;
    @Schema(description = "需要排除的ID")
    private Long excludedId;
    @Schema(description = "查询关键字")
    private String keyword;
}

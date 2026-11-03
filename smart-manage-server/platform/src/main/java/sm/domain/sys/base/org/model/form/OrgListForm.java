package sm.domain.sys.base.org.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "组织列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgListForm extends PageForm {
    @Schema(description = "父ID")
    private Long parentId;
    @Schema(description = "是否包含下级组织")
    private Boolean includeDescendants;
    @Schema(description = "是否包含已归档组织")
    private Boolean showArchived;
    @Schema(description = "查询关键字")
    private String keyword;
}

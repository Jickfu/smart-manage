package sm.domain.workflow.process.definition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/** 流程定义列表范围；领域和应用来自业务显式关联的功能目录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程定义列表查询条件")
public class DefinitionListForm extends PageForm {
    @Schema(description = "列表快速搜索关键字")
    private String keyword;
    @Schema(description = "所属领域 ID")
    private Long domainId;
    @Schema(description = "所属应用 ID")
    private Long appId;
}

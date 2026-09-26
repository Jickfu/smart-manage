package sm.domain.workflow.process.definition.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/** 流程定义列表范围；领域和应用来自业务显式关联的功能目录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinitionListForm extends PageForm {
    private String keyword;
    private Long domainId;
    private Long appId;
}

package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "编号规则列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class NumberRuleListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围")
    private String scopeType;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属功能ID")
    private Long featureId;
    @Schema(description = "稳定编号引用键")
    private String referenceKey;
    @Schema(description = "是否启用")
    private Boolean enabled;
}

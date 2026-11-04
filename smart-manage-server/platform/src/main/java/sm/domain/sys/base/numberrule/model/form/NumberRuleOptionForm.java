package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "编号规则选项输入表单")
@Data
public class NumberRuleOptionForm {
    @Schema(description = "流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围")
    private String scopeType;
    @Schema(description = "稳定编号引用键")
    private String referenceKey;
}

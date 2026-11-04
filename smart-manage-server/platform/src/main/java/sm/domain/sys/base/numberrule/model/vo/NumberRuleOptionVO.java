package sm.domain.sys.base.numberrule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "编号规则选项输出模型")
@Data
@AllArgsConstructor
public class NumberRuleOptionVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "稳定规则键")
    private String ruleKey;
    @Schema(description = "稳定编号引用键")
    private String referenceKey;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围")
    private String scopeType;
    @Schema(description = "编号模板")
    private String pattern;
    @Schema(description = "是否为该编号引用的默认规则")
    private Boolean defaultRule;
}

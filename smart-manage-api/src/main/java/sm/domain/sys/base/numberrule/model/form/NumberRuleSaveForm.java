package sm.domain.sys.base.numberrule.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class NumberRuleSaveForm {
    private Long id;
    private Integer version;
    @NotBlank(message = "规则键不能为空")
    @Size(max = 200, message = "规则键不能超过200个字符")
    private String ruleKey;
    @NotBlank(message = "编号引用不能为空")
    @Size(max = 200, message = "编号引用键不能超过200个字符")
    private String referenceKey;
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;
    @NotBlank(message = "流水作用域不能为空")
    private String scopeType;
    @NotBlank(message = "重置周期不能为空")
    private String resetPeriod;
    @NotNull(message = "起始流水值不能为空")
    @Min(value = 1, message = "起始流水值不能小于1")
    @Max(value = 2147483647, message = "起始流水值不能超过2147483647")
    private Integer startValue;
    @NotNull(message = "编号格式段不能为空")
    @Size(min = 1, max = 20, message = "编号格式段数量必须在1到20之间")
    @Valid
    private List<NumberRuleSegmentForm> segments;
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}

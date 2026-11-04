package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Schema(description = "编号规则保存输入表单")
@Data
public class NumberRuleSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "稳定规则键")
    @NotBlank(message = "规则键不能为空")
    @Size(max = 200, message = "规则键不能超过200个字符")
    private String ruleKey;
    @Schema(description = "稳定编号引用键")
    @NotBlank(message = "编号引用不能为空")
    @Size(max = 200, message = "编号引用键不能超过200个字符")
    private String referenceKey;
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;
    @Schema(description = "流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围")
    @NotBlank(message = "流水作用域不能为空")
    private String scopeType;
    @Schema(description = "流水重置周期：NEVER、YEAR、MONTH、DAY")
    @NotBlank(message = "重置周期不能为空")
    private String resetPeriod;
    @Schema(description = "新分段起始流水值")
    @NotNull(message = "起始流水值不能为空")
    @Min(value = 1, message = "起始流水值不能小于1")
    @Max(value = 2147483647, message = "起始流水值不能超过2147483647")
    private Integer startValue;
    @Schema(description = "按顺序排列的编号格式段")
    @NotNull(message = "编号格式段不能为空")
    @Size(min = 1, max = 20, message = "编号格式段数量必须在1到20之间")
    @Valid
    private List<NumberRuleSegmentForm> segments;
    @Schema(description = "说明")
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}

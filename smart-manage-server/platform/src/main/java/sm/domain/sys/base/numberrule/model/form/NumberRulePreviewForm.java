package sm.domain.sys.base.numberrule.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NumberRulePreviewForm {
    @NotBlank(message = "编号引用不能为空")
    private String referenceKey;
    @NotNull(message = "编号格式段不能为空")
    @Valid
    private List<NumberRuleSegmentForm> segments;
    @NotNull(message = "示例流水值不能为空")
    @Min(value = 1, message = "示例流水值不能小于1")
    private Long sequenceValue;
}

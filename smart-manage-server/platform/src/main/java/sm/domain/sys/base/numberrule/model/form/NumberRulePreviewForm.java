package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "编号规则预览输入表单")
@Data
public class NumberRulePreviewForm {
    @Schema(description = "稳定编号引用键")
    @NotBlank(message = "编号引用不能为空")
    private String referenceKey;
    @Schema(description = "按顺序排列的编号格式段")
    @NotNull(message = "编号格式段不能为空")
    @Valid
    private List<NumberRuleSegmentForm> segments;
    @Schema(description = "编号预览使用的流水值，不推进正式计数器")
    @NotNull(message = "示例流水值不能为空")
    @Min(value = 1, message = "示例流水值不能小于1")
    private Long sequenceValue;
}

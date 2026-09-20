package sm.domain.sys.base.numberrule.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NumberRuleSegmentForm {
    @NotNull(message = "格式段顺序不能为空")
    private Integer sort;
    @NotBlank(message = "格式段类型不能为空")
    private String segmentType;
    @Size(max = 200, message = "格式段值不能超过200个字符")
    private String value;
    @Size(max = 20, message = "日期格式不能超过20个字符")
    private String format;
    private Integer length;
    @Size(max = 10, message = "段间分隔符不能超过10个字符")
    private String separator;
}

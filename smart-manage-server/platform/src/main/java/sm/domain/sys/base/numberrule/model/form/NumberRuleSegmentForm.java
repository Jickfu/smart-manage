package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "编号格式段输入表单")
@Data
public class NumberRuleSegmentForm {
    @Schema(description = "排序号")
    @NotNull(message = "格式段顺序不能为空")
    private Integer sort;
    @Schema(description = "格式段类型：FIXED 固定文本、VARIABLE 受控变量、DATE 日期、SEQUENCE 流水")
    @NotBlank(message = "格式段类型不能为空")
    private String segmentType;
    @Schema(description = "FIXED 段为固定文本，VARIABLE、DATE 段为编号引用允许的对应类型变量键（内置日期变量为 system.date）；SEQUENCE 段不使用")
    @Size(max = 200, message = "格式段值不能超过200个字符")
    private String value;
    @Schema(description = "日期格式，DATE 段仅支持 yyyy、yyyyMM、yyyyMMdd")
    @Size(max = 20, message = "日期格式不能超过20个字符")
    private String format;
    @Schema(description = "流水位数，SEQUENCE 段取值 1～18")
    private Integer length;
    @Schema(description = "段后分隔符")
    @Size(max = 10, message = "段间分隔符不能超过10个字符")
    private String separator;
}

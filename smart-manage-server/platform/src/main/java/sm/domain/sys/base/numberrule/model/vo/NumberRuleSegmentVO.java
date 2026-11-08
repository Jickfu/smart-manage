package sm.domain.sys.base.numberrule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "编号格式段输出模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberRuleSegmentVO {
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "格式段类型：FIXED 固定文本、VARIABLE 受控变量、DATE 日期、SEQUENCE 流水")
    private String segmentType;
    @Schema(description = "FIXED 段为固定文本，VARIABLE、DATE 段为编号引用允许的对应类型变量键（内置日期变量为 system.date）；SEQUENCE 段不使用")
    private String value;
    @Schema(description = "日期格式，DATE 段仅支持 yyyy、yyyyMM、yyyyMMdd")
    private String format;
    @Schema(description = "流水位数，SEQUENCE 段取值 1～18")
    private Integer length;
    @Schema(description = "段后分隔符")
    private String separator;
}

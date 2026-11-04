package sm.domain.sys.base.numberrule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "编号受控变量输出模型")
public record NumberVariableVO(
    @Schema(description = "受控编号变量的稳定键")
    String key,
    @Schema(description = "名称")
    String name,
    @Schema(description = "格式段类型：FIXED 固定文本、VARIABLE 受控变量、DATE 日期、SEQUENCE 流水")
    String segmentType) {
}

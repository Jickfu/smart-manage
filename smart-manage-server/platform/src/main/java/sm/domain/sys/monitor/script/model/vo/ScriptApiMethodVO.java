package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "脚本接口目录方法定义输出模型")
@Data
public class ScriptApiMethodVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "脚本方法签名")
    private String signature;
    @Schema(description = "方法返回类型")
    private String returnType;
    @Schema(description = "方法入参定义")
    private List<ScriptApiParameterVO> parameters;
    @Schema(description = "脚本调用示例")
    private String example;
}

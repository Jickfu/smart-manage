package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "脚本接口目录参数定义输出模型")
@Data
public class ScriptApiParameterVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "脚本接口元数据声明的 Java 类型名称")
    private String type;
    @Schema(description = "是否必填")
    private boolean required;
    @Schema(description = "结构化参数的字段定义")
    private List<ScriptApiFieldVO> fields;
}

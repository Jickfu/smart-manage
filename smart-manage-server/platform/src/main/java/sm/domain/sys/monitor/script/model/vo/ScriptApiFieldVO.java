package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "脚本接口目录字段定义输出模型")
@Data
public class ScriptApiFieldVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "脚本接口元数据声明的 Java 类型名称")
    private String type;
    @Schema(description = "是否必填")
    private boolean required;
    @Schema(description = "参数校验约束说明")
    private List<String> constraints;
}

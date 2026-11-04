package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "脚本接口目录服务定义输出模型")
@Data
public class ScriptApiServiceVO {
    @Schema(description = "脚本可调用的 Spring Bean 名称")
    private String beanName;
    @Schema(description = "类名")
    private String className;
    @Schema(description = "脚本可调用的方法列表")
    private List<ScriptApiMethodVO> methods;
}

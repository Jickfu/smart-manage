package sm.domain.workflow.process.definition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import sm.domain.workflow.process.engine.WorkflowDefinitions;
@Schema(description = "流程定义和版本摘要")
public record DefinitionListVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "业务编码") String number,
        @Schema(description = "显示名称") String name,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "乐观锁版本，修改已有记录时使用") Integer version,
        @Schema(description = "同一绑定下的流程版本列表") List<WorkflowDefinitions.Definition> definitions) { }

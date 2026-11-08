package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 两层调度目录；应用节点键包含领域 ID，避免应用改属后混入旧执行记录。
 */
@Schema(description = "调度目录树节点输出模型")
public record SchedulerCatalogNodeVO(
    @Schema(description = "调度目录树节点键")
    String key,
    @Schema(description = "标题")
    String title,
    @Schema(description = "子节点列表")
    List<SchedulerCatalogNodeVO> children) {
}

package sm.domain.sys.scheduler.model.vo;

import java.util.List;

/** 两层调度目录；应用节点键包含领域 ID，避免应用改属后混入旧执行记录。 */
public record SchedulerCatalogNodeVO(String key, String title, List<SchedulerCatalogNodeVO> children) {
}

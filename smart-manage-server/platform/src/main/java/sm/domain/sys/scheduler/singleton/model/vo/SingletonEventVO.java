package sm.domain.sys.scheduler.singleton.model.vo;
import io.swagger.v3.oas.annotations.media.Schema;
/** 单例任务状态变更记录。 */
@Schema(description = "单例任务状态变更记录")
public record SingletonEventVO(
    @Schema(description = "ID") Long id,
    @Schema(description = "事件时间") java.time.LocalDateTime occurredAt,
    @Schema(description = "事件类型") String eventType,
    @Schema(description = "执行实例") String instanceId,
    @Schema(description = "运行身份") String runId,
    @Schema(description = "安全的原因分类") String reason) { }

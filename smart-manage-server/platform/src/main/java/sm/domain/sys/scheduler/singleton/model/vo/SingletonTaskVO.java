package sm.domain.sys.scheduler.singleton.model.vo;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
/** 常驻任务管理详情。 */
@Data
@Schema(description = "常驻任务管理详情")
public class SingletonTaskVO {
    @Schema(description = "任务编码")
    private String number;
    @Schema(description = "任务名称")
    private String name;
    @Schema(description = "所属应用编码")
    private String appNumber;
    @Schema(description = "任务说明")
    private String description;
    @Schema(description = "是否允许管理端启停")
    private Boolean controllable;
    @Schema(description = "期望是否运行")
    private Boolean requested;
    @Schema(description = "启停命令乐观锁版本")
    private Integer version;
    @Schema(description = "运行状态：STOPPED、STARTING、RUNNING、STOPPING、FAILED、UNKNOWN")
    private String state;
    @Schema(description = "当前执行实例")
    private String ownerInstanceId;
    @Schema(description = "本次运行身份")
    private String runId;
    @Schema(description = "单调递增的执行代际")
    private Long generation;
    @Schema(description = "本次开始时间")
    private java.time.LocalDateTime startedAt;
    @Schema(description = "最近心跳时间")
    private java.time.LocalDateTime heartbeatAt;
    @Schema(description = "失败退避截止时间")
    private java.time.LocalDateTime retryAt;
    @Schema(description = "安全的失败分类")
    private String lastError;
    @Schema(description = "当前服务实例是否装配该任务实现")
    private boolean available;
}

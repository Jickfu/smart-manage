package sm.domain.sys.monitor.home.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;

@Schema(description = "监控概览输出模型")
@Data
public class MonitorOverviewVO {
  @Schema(description = "存在可用遥测的主机数量")
  private int hostTelemetryAvailable;
  @Schema(description = "当前拓扑主机数量")
  private int hostTotal;
  @Schema(description = "当前拓扑中在线且为 ACTIVE 的应用实例数")
  private int applicationOnline;
  @Schema(description = "当前拓扑中 ACTIVE 应用实例总数")
  private int applicationTotal;
  @Schema(description = "数据库健康状态")
  private String databaseHealth;
  @Schema(description = "Redis 健康状态")
  private String redisHealth;
  @Schema(description = "等待持续时间满足的告警数量")
  private int pendingCount;
  @Schema(description = "正在触发的告警数量")
  private int firingCount;
  @Schema(description = "当前严重告警数量")
  private int criticalCount;
  @Schema(description = "当前异常摘要")
  private List<AttentionItem> currentAbnormal;
  @Schema(description = "当前主机与应用实例拓扑")
  private List<HostSummary> topology;

  @Schema(description = "当前异常关注项")
  @Data
  public static class AttentionItem {
    @Schema(description = "告警严重程度：WARNING 警告、CRITICAL 严重")
    private String severity;
    @Schema(description = "规则编码")
    private String ruleCode;
    @Schema(description = "监控作用对象类型：HOST 主机、INSTANCE 应用实例")
    private String scopeType;
    @Schema(description = "作用对象标识")
    private String scopeId;
    @Schema(description = "摘要")
    private String summary;
  }

  @Schema(description = "主机摘要")
  @Data
  public static class HostSummary {
    @Schema(description = "运行主机稳定标识")
    private String hostId;
    @Schema(description = "主机名称")
    private String hostName;
    @Schema(description = "当前遥测可用状态")
    private String telemetryStatus;
    @Schema(description = "当前在线应用实例数")
    private int onlineInstances;
    @Schema(description = "应用实例总数")
    private int totalInstances;
  }
}

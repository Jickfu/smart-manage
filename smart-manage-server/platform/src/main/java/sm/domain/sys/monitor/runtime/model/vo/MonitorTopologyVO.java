package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

@Schema(description = "监控拓扑输出模型")
@Data
public class MonitorTopologyVO {
  @Schema(description = "运行主机稳定标识")
  private String hostId;
  @Schema(description = "主机名称")
  private String hostName;
  @Schema(description = "操作系统名称")
  private String osName;
  @Schema(description = "操作系统版本")
  private String osVersion;
  @Schema(description = "当前遥测可用状态")
  private String telemetryStatus;
  @Schema(description = "应用实例列表")
  private List<Instance> instances;

  @Schema(description = "应用实例节点")
  @Data
  public static class Instance {
    @Schema(description = "应用实例唯一标识")
    private String instanceId;
    @Schema(description = "应用名称")
    private String applicationName;
    @Schema(description = "应用版本")
    private String applicationVersion;
    @Schema(description = "实例生命周期：ACTIVE/RETIRED")
    private String lifecycle;
    @Schema(description = "应用实例当前是否在线")
    private boolean online;
    @Schema(description = "是否为处理当前请求的应用实例")
    private boolean current;
    @Schema(description = "最后发现时间")
    private OffsetDateTime lastSeenTime;
    @Schema(description = "退役时间")
    private OffsetDateTime retiredAt;
  }
}

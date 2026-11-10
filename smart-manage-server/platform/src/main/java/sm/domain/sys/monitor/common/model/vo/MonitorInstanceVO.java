package sm.domain.sys.monitor.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 可供管理端选择的应用实例目录项，不暴露内部管理地址。
 */
@Schema(description = "监控应用实例输出模型")
@Data
public class MonitorInstanceVO {
  @Schema(description = "应用实例唯一标识")
  private String instanceId;
  @Schema(description = "运行主机稳定标识")
  private String hostId;
  @Schema(description = "应用名称")
  private String applicationName;
  @Schema(description = "应用版本")
  private String applicationVersion;
  @Schema(description = "实例生命周期：ACTIVE/RETIRED")
  private String lifecycle;
  @Schema(description = "应用实例当前是否在线")
  private boolean online;
  @Schema(description = "应用实例启动时间")
  private LocalDateTime startTime;
  @Schema(description = "最后发现时间")
  private LocalDateTime lastSeenTime;
  @Schema(description = "是否为处理当前请求的应用实例")
  private boolean current;
}

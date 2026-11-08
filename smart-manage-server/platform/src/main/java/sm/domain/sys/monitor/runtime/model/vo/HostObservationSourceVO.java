package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 单个应用实例发布的 Host 观测；实例身份用于防止 Redis source key 与内容串写。
 */
@Schema(description = "主机观测来源输出模型")
@Data
public class HostObservationSourceVO {
  @Schema(description = "运行主机稳定标识")
  private String hostId;
  @Schema(description = "应用实例唯一标识")
  private String instanceId;
  @Schema(description = "遥测快照")
  private HostSnapshotVO snapshot;
}

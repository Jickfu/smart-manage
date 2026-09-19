package sm.domain.sys.monitor.runtime.model.vo;

import lombok.Data;

/** 单个应用实例发布的 Host 观测；实例身份用于防止 Redis source key 与内容串写。 */
@Data
public class HostObservationSourceVO {
  private String hostId;
  private String instanceId;
  private HostSnapshotVO snapshot;
}

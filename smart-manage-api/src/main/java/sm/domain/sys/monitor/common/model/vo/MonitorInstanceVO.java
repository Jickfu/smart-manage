package sm.domain.sys.monitor.common.model.vo;

import lombok.Data;

/** 可供管理端选择的应用实例目录项，不暴露内部管理地址。 */
@Data
public class MonitorInstanceVO {
  private String instanceId;
  private String hostId;
  private String applicationName;
  private String applicationVersion;
  private String lifecycle;
  private boolean online;
  private String startTime;
  private String lastSeenTime;
  private boolean current;
}

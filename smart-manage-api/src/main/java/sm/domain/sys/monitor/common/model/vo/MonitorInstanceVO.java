package sm.domain.sys.monitor.common.model.vo;

import lombok.Data;

/** 可供管理端选择的在线应用实例，不暴露内部管理地址。 */
@Data
public class MonitorInstanceVO {
    private String instanceId;
    private String applicationVersion;
    private String startTime;
    private String lastSeenTime;
    private boolean current;
}

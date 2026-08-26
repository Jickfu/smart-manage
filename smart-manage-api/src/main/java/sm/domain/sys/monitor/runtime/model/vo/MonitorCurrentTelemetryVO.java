package sm.domain.sys.monitor.runtime.model.vo;

/** 当前遥测缺失是可观测状态，不是页面级请求失败。 */
public record MonitorCurrentTelemetryVO<T>(String status, T snapshot) {
  public static <T> MonitorCurrentTelemetryVO<T> available(T snapshot) {
    return new MonitorCurrentTelemetryVO<>("AVAILABLE", snapshot);
  }

  public static <T> MonitorCurrentTelemetryVO<T> unavailable() {
    return new MonitorCurrentTelemetryVO<>("UNAVAILABLE", null);
  }
}

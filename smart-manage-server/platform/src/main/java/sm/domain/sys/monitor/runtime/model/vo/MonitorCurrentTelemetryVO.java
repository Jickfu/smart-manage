package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 当前遥测缺失是可观测状态，不是页面级请求失败。
 */
@Schema(description = "当前遥测输出模型")
public record MonitorCurrentTelemetryVO<T>(
    @Schema(description = "遥测状态：AVAILABLE 可用、UNAVAILABLE 不可用；不可用时 snapshot 为 null")
    String status,
    @Schema(description = "遥测快照")
    T snapshot) {
  public static <T> MonitorCurrentTelemetryVO<T> available(T snapshot) {
    return new MonitorCurrentTelemetryVO<>("AVAILABLE", snapshot);
  }

  public static <T> MonitorCurrentTelemetryVO<T> unavailable() {
    return new MonitorCurrentTelemetryVO<>("UNAVAILABLE", null);
  }
}

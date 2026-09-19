package sm.domain.sys.monitor.runtime.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 对重复 collector 故障限频，避免默认 10 秒采样持续刷相同堆栈。 */
@Component
@Slf4j
class MonitorCollectorWarningLogger {
  private static final long WARN_INTERVAL_MS = Duration.ofMinutes(1).toMillis();
  private final ConcurrentHashMap<String, Long> lastWarnings = new ConcurrentHashMap<>();

  void warn(String collector, String targetId, Exception exception) {
    long now = System.currentTimeMillis();
    String warningKey = collector + ':' + targetId;
    Long previous = lastWarnings.put(warningKey, now);
    if (previous == null || now - previous >= WARN_INTERVAL_MS) {
      log.warn(
          "监控指标采集降级，collector={}, targetId={}, error={}",
          collector,
          targetId,
          exception.toString());
    }
  }
}

package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MonitorMetricValueFormatterTests {
  private final MonitorMetricValueFormatter formatter = new MonitorMetricValueFormatter();

  @Test
  void formatsEveryBuiltInMetricKindConsistently() {
    assertEquals("95.34%", formatter.format(BigDecimal.valueOf(.9534), "RATIO", "%"));
    assertEquals("12 个", formatter.format(BigDecimal.valueOf(12), "COUNT", "个"));
    assertEquals("1532 ms", formatter.format(BigDecimal.valueOf(1532), "DURATION_MS", "ms"));
    assertEquals("3.25 req/s", formatter.format(BigDecimal.valueOf(3.25), "RATE", "req/s"));
    assertEquals("异常", formatter.format(BigDecimal.ONE, "BOOLEAN", ""));
    assertEquals("正常", formatter.format(BigDecimal.ZERO, "BOOLEAN", ""));
  }
}

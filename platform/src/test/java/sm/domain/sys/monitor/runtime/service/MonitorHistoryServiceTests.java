package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

class MonitorHistoryServiceTests {
  @Test
  void supportsOnlyBoundedHistoryRanges() {
    assertEquals(MonitorHistoryService.Range.H1, MonitorHistoryService.Range.parse("1h"));
    assertEquals(MonitorHistoryService.Range.H6, MonitorHistoryService.Range.parse("6h"));
    assertEquals(MonitorHistoryService.Range.H24, MonitorHistoryService.Range.parse("24h"));
    assertEquals(MonitorHistoryService.Range.D7, MonitorHistoryService.Range.parse("7d"));
    assertThrows(BizException.class, () -> MonitorHistoryService.Range.parse("30d"));
  }
}

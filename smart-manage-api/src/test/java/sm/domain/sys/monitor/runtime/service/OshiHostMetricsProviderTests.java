package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OshiHostMetricsProviderTests {
  @Test
  void normalDeltaProducesRate() {
    assertEquals(50d, OshiHostMetricsProvider.rate(200, 100, 2));
  }

  @Test
  void counterResetDoesNotProduceNegativeRate() {
    assertNull(OshiHostMetricsProvider.rate(10, 100, 2));
  }

  @Test
  void invalidIntervalRepresentsFirstOrUnavailableSample() {
    assertNull(OshiHostMetricsProvider.rate(100, 100, 0));
  }
}

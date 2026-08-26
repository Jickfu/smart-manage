package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

class AggregateHttpMetricsFilterTests {
  @Test
  void recordsAllRequestsIntoOneLowCardinalityTimer() throws Exception {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    AggregateHttpMetricsFilter filter = new AggregateHttpMetricsFilter(registry);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(anyString())).thenReturn(null);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    filter.doFilterInternal(request, response, chain);
    filter.doFilterInternal(request, response, chain);
    assertEquals(2, registry.get(AggregateHttpMetricsFilter.METER_NAME).timer().count());
    assertEquals(1, registry.find(AggregateHttpMetricsFilter.METER_NAME).timers().size());
  }
}

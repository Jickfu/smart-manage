package sm.domain.sys.monitor.runtime.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 无标签总体 Timer，使 P95/P99 表示整个应用实例的 HTTP 分布。 */
@Component
class AggregateHttpMetricsFilter extends OncePerRequestFilter {
  static final String METER_NAME = "smart.manage.http.aggregate";
  private final Timer timer;

  AggregateHttpMetricsFilter(MeterRegistry registry) {
    this.timer =
        Timer.builder(METER_NAME)
            .publishPercentileHistogram()
            .publishPercentiles(0.95, 0.99)
            .register(registry);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Timer.Sample sample = Timer.start();
    try {
      chain.doFilter(request, response);
    } finally {
      sample.stop(timer);
    }
  }
}

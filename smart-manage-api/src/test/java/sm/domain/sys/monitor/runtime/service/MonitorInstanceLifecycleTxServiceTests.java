package sm.domain.sys.monitor.runtime.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class MonitorInstanceLifecycleTxServiceTests {
  @Test
  void retireClosesEveryActiveInstanceIncidentWithoutRecoveryNotification() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
    new MonitorInstanceLifecycleTxService(jdbc).retire("instance1");
    verify(jdbc, times(2)).update(anyString(), any(Object[].class));
    verify(jdbc).update(contains("scope_type='INSTANCE'"), eq("instance1"));
    verify(jdbc, never()).update(contains("rule_code='INSTANCE_OFFLINE'"), any(Object[].class));
    verify(jdbc, never()).update(contains("notification"), any(Object[].class));
  }
}

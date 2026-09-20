package sm.domain.sys.monitor.alert.service;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertRuleSaveForm;
import sm.system.security.context.CurrentUserContext;

class MonitorAlertServiceTests {
  @Test
  void saveRulePassesCurrentUserToTransaction() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    MonitorAlertTxService txService = mock(MonitorAlertTxService.class);
    CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    when(currentUserContext.getUserId()).thenReturn(1L);
    MonitorAlertService service =
        new MonitorAlertService(
            jdbcTemplate,
            txService,
            mock(MonitorAlertStateTxService.class),
            currentUserContext,
            new MonitorMetricValueFormatter());
    MonitorAlertRuleSaveForm form =
        new MonitorAlertRuleSaveForm(
            1L,
            0,
            true,
            "WARNING",
            BigDecimal.valueOf(0.9),
            300,
            BigDecimal.valueOf(0.8),
            1800,
            false,
            List.of(),
            null);

    service.saveRule(form);

    verify(txService).saveRule(form, 1L);
  }
}

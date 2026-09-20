package sm.domain.sys.monitor.alert.model.form;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record MonitorAlertRuleSaveForm(
        @NotNull Long id,
        @NotNull Integer version,
        @NotNull Boolean enabled,
        @NotBlank String severity,
        @NotNull @DecimalMin("0") BigDecimal threshold,
        @NotNull @Min(0) @Max(86400) Integer durationSeconds,
        @DecimalMin("0") BigDecimal recoveryThreshold,
        @NotNull @Min(60) @Max(604800) Integer repeatIntervalSeconds,
        @NotNull Boolean emailEnabled,
        @Size(max = 50) List<Long> recipientUserIds,
        @Size(max = 500) String description) { }

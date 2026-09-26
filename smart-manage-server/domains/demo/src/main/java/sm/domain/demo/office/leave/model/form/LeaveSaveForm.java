package sm.domain.demo.office.leave.model.form;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Data
public class LeaveSaveForm {
    private Long id;
    private Integer version;
    @NotNull private UUID clientKey;
    @NotNull private LocalDate bizDate;
    @NotBlank @Pattern(regexp = "ANNUAL|SICK|PERSONAL|OTHER") private String leaveType;
    @NotNull private LocalDateTime startTime;
    @NotNull private LocalDateTime endTime;
    @NotNull @DecimalMin("0.01") @Digits(integer = 4, fraction = 2) private BigDecimal days;
    @NotBlank @Size(max = 2000) private String reason;
    @NotNull @Size(max = 50) private List<@NotNull @Positive Long> attachmentIds = List.of();
    private Map<Long, String> attachmentUploadSessions = Map.of();
}

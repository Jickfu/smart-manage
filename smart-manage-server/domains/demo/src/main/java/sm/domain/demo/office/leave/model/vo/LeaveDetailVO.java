package sm.domain.demo.office.leave.model.vo;
import lombok.Data;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Data
public class LeaveDetailVO {
    private Long id;
    private Integer version;
    private String number;
    private UUID clientKey;
    private Long orgId;
    private Long applicantId;
    private LocalDate bizDate;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal days;
    private String reason;
    private String billStatus;
    private Long currentInstanceId;
    private String lastOutcome;
    private List<Object> entries = List.of();
    private List<AttachmentReference> attachments = List.of();
    private List<Long> retainedAttachmentIds = List.of();
}

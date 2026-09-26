package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseBillEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demo_leave")
public class LeaveEntity extends BaseBillEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private UUID clientKey;
    private Long applicantId;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal days;
    private String reason;
    private Long currentInstanceId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String lastOutcome;
    @Version
    private Integer version;
}

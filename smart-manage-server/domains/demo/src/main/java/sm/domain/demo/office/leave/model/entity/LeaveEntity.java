package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseBillEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
/**
 * 请假样板单据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demo_leave")
public class LeaveEntity extends BaseBillEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 客户端生成的请假草稿 UUID
     */
    private UUID clientKey;
    /**
     * 申请人用户 ID
     */
    private Long applicantId;
    /**
     * 请假类型，取值 ANNUAL、SICK、PERSONAL 或 OTHER
     */
    private String leaveType;
    /**
     * 请假开始时间
     */
    private LocalDateTime startTime;
    /**
     * 请假结束时间
     */
    private LocalDateTime endTime;
    /**
     * 请假天数，单位为天，最多两位小数
     */
    private BigDecimal days;
    /**
     * 请假事由
     */
    private String reason;
    /**
     * 最近一轮审批的工作流实例 ID，未发起时为空
     */
    private Long currentInstanceId;
    /**
     * 最近一轮审批的结果，未结束时可为空
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String lastOutcome;
    /**
     * 乐观锁版本，修改已有记录时使用
     */
    @Version
    private Integer version;
}

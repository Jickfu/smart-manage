package sm.domain.workflow.process.instance.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_instance")
public class WorkflowInstanceEntity extends BaseEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String businessType;
    private Long businessId;
    private String number;
    private Long orgId;
    private Long applicantId;
    private UUID requestId;
    private String requestDigest;
    private String snapshot;
}

package sm.domain.workflow.process.instance.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
import java.util.UUID;

/**
 * 引擎实例和业务单据审批轮次的关联及提交快照
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_instance")
public class WorkflowInstanceEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 可审批业务的稳定类型标识
     */
    private String businessType;
    /**
     * 业务单据 ID
     */
    private Long businessId;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 单据所属组织 ID
     */
    private Long orgId;
    /**
     * 申请人用户 ID
     */
    private Long applicantId;
    /**
     * 客户端请求 UUID，用于同一命令安全重试
     */
    private UUID requestId;
    /**
     * 规范化请求内容的 SHA-256 摘要
     */
    private String requestDigest;
    /**
     * 提交时冻结的业务单据快照 JSON
     */
    private String snapshot;
}

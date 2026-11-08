package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
/** 历史附件引用永久保留；后续草稿的附件选择不改变本轮引用。 */
/**
 * 请假单审批轮次不可变附件关联
 */
@Data
@TableName("t_demo_leave_attachment_snapshot")
public class LeaveAttachmentSnapshotEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属请假单 ID
     */
    private Long leaveId;
    /**
     * 工作流实例 ID
     */
    private Long instanceId;
    /**
     * 平台附件 ID
     */
    private Long attachmentId;
}

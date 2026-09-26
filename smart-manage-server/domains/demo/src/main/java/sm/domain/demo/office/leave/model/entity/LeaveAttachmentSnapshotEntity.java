package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
/** 历史附件引用永久保留；后续草稿的附件选择不改变本轮引用。 */
@Data
@TableName("t_demo_leave_attachment_snapshot")
public class LeaveAttachmentSnapshotEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long leaveId;
    private Long instanceId;
    private Long attachmentId;
}

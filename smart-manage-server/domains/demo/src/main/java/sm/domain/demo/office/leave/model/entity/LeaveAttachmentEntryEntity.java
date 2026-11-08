package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
/**
 * 请假草稿当前选择的附件关联
 */
@Data
@TableName("t_demo_leave_attachment_entry")
public class LeaveAttachmentEntryEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属请假单 ID
     */
    private Long parentId;
    /**
     * 平台附件 ID
     */
    private Long attachmentId;
}

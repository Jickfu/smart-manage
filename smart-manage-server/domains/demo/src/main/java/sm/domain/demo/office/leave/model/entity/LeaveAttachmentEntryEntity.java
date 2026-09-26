package sm.domain.demo.office.leave.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
@Data
@TableName("t_demo_leave_attachment_entry")
public class LeaveAttachmentEntryEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long parentId;
    private Long attachmentId;
}

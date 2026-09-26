package sm.domain.workflow.process.notification.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.UUID;

@Data
@TableName("t_workflow_outbox")
public class WorkflowOutboxEntity {
    @TableId
    private Long id;
    private String eventKey;
    private Long instanceId;
    private Long recipientId;
    private String title;
    private String content;
    private UUID claimToken;
}

package sm.domain.workflow.process.notification.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.UUID;

/**
 * 工作流通知发件箱记录
 */
@Data
@TableName("t_workflow_outbox")
public class WorkflowOutboxEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 通知事件与接收人的稳定幂等键
     */
    private String eventKey;
    /**
     * 工作流实例 ID
     */
    private Long instanceId;
    /**
     * 通知接收人用户 ID
     */
    private Long recipientId;
    /**
     * 站内通知标题
     */
    private String title;
    /**
     * 站内通知正文
     */
    private String content;
    /**
     * 发件箱投递租约标识
     */
    private UUID claimToken;
}

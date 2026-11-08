package sm.domain.workflow.process.runtime.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.UUID;
/**
 * 与命令同事务提交的流程幂等回执
 */
@Data
@TableName("t_workflow_command")
public class RuntimeCommandEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 客户端请求 UUID，用于同一命令安全重试
     */
    private UUID requestId;
    /**
     * 命令执行人用户 ID
     */
    private Long actorId;
    /**
     * 工作流实例 ID
     */
    private Long instanceId;
    /**
     * 规范化请求内容的 SHA-256 摘要
     */
    private String requestDigest;
    /**
     * 用于重放的内部流程运行结果 JSON
     */
    private String result;
}

package sm.domain.workflow.process.runtime.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.UUID;
@Data
@TableName("t_workflow_command")
public class RuntimeCommandEntity {
    @TableId(type = IdType.INPUT)
    private UUID requestId;
    private Long actorId;
    private Long instanceId;
    private String requestDigest;
    private String result;
}

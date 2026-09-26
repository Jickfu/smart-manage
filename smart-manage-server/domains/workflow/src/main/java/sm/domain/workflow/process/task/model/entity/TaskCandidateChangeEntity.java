package sm.domain.workflow.process.task.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_candidate_change")
public class TaskCandidateChangeEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long instanceId;
    private Long taskId;
    private Long operatorId;
    private String beforeCandidates;
    private String afterCandidates;
    private String reason;
}

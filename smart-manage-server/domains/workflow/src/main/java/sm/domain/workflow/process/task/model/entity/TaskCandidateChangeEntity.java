package sm.domain.workflow.process.task.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
/**
 * 任务候选人变更审计记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_candidate_change")
public class TaskCandidateChangeEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 工作流实例 ID
     */
    private Long instanceId;
    /**
     * 引擎任务 ID
     */
    private Long taskId;
    /**
     * 操作人用户 ID
     */
    private Long operatorId;
    /**
     * 变更前的候选人用户 ID 列表 JSON
     */
    private String beforeCandidates;
    /**
     * 变更后的候选人用户 ID 列表 JSON
     */
    private String afterCandidates;
    /**
     * 操作原因
     */
    private String reason;
}

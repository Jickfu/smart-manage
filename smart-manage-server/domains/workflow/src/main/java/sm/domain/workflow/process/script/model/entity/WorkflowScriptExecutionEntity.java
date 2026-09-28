package sm.domain.workflow.process.script.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_script_execution")
public class WorkflowScriptExecutionEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long instanceId;
    private Long taskId;
    private String nodeCode;
    private String status;
    private Integer durationMs;
    private String errorMessage;
    private String resultData;
    private Long operatorId;
}

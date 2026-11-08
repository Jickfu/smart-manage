package sm.domain.workflow.process.runtime.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 流程管理员操作审计记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_operation")
public class WorkflowOperationEntity extends BaseEntity {
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
     * 工作流动作编码
     */
    private String action;
    /**
     * 操作人用户 ID
     */
    private Long operatorId;
    /**
     * 操作原因
     */
    private String reason;
    /**
     * 操作前的流程审计投影 JSON
     */
    private String beforeData;
    /**
     * 操作后的流程审计投影 JSON
     */
    private String afterData;
}

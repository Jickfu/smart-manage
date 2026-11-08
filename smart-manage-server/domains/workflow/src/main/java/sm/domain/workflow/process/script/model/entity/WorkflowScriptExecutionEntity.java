package sm.domain.workflow.process.script.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 受限脚本节点执行和重试记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_script_execution")
public class WorkflowScriptExecutionEntity extends BaseEntity {
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
     * 流程定义内的节点编码
     */
    private String nodeCode;
    /**
     * 脚本执行状态，取值 SUCCESS、ERROR 或 TIMEOUT
     */
    private String status;
    /**
     * 脚本执行耗时，单位毫秒
     */
    private Integer durationMs;
    /**
     * 脚本执行失败时的安全裁剪说明
     */
    private String errorMessage;
    /**
     * 脚本返回的变量和下一节点候选人 JSON
     */
    private String resultData;
    /**
     * 操作人用户 ID
     */
    private Long operatorId;
}

package sm.domain.workflow.process.definition.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 仅保存本项目业务绑定和停用开关，定义版本仍在引擎中。 */
/**
 * 流程定义与可审批业务的绑定
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_binding")
public class WorkflowBindingEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 显示名称
     */
    private String name;
    /**
     * 可审批业务的稳定类型标识
     */
    private String businessType;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 乐观锁版本，修改已有记录时使用
     */
    @Version
    private Integer version;
}

package sm.domain.workflow.process.definition.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 仅保存本项目业务绑定和停用开关，定义版本仍在引擎中。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_workflow_binding")
public class WorkflowBindingEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private String name;
    private String businessType;
    private Boolean enabled;
    @Version
    private Integer version;
}

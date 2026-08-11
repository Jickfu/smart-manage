package sm.domain.sys.base.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 用户在行政组织中的任职关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_assignment")
public class UserAssignmentEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long orgId;
    private String position;
    private Boolean isOrgLeader;
    private Boolean isPrimary;
}

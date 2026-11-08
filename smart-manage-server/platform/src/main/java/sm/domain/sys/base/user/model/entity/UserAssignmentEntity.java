package sm.domain.sys.base.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 用户在行政组织中的任职关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_assignment")
public class UserAssignmentEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 组织 ID
     */
    private Long orgId;
    /**
     * 任职岗位名称
     */
    private String position;
    /**
     * 是否为部门负责人
     */
    private Boolean isOrgLeader;
    /**
     * 是否为主职
     */
    private Boolean isPrimary;
}

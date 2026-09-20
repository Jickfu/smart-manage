package sm.domain.sys.base.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 用户聚合内部的角色关联实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_role")
public class UserRoleEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long orgId;

    private Long roleId;
}

package sm.domain.sys.base.role.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 角色聚合内部的权限关联实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role_perms")
public class RolePermissionEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long permissionId;
}

package sm.domain.sys.base.datascope.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 角色数据权限持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role_data_scope")
public class RoleDataScopeEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 数据权限操作标识
     */
    private String action;
    /**
     * 数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS
     */
    private String scopeType;
}

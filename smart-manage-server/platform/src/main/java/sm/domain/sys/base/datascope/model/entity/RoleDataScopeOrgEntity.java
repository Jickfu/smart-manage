package sm.domain.sys.base.datascope.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 数据权限指定组织持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role_data_scope_org")
public class RoleDataScopeOrgEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属数据权限规则 ID
     */
    private Long scopeRuleId;
    /**
     * 组织 ID
     */
    private Long orgId;
}

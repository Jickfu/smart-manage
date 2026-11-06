package sm.domain.sys.base.role.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 全局默认角色配置，固定单例且允许不设置角色。
 */
@Data
@TableName("t_sys_role_default")
public class RoleDefaultEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 默认角色ID，空表示未设置
     */
    private Long roleId;
    /**
     * 配置并发版本号
     */
    @Version
    private Integer version;
}

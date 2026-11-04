package sm.domain.sys.base.user.quicklaunch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.system.entity.BaseEntity;

/**
 * 用户首页快捷入口持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_home_quick_launch")
public class UserHomeQuickLaunchEntity extends BaseEntity {
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
     * 首页范围类型：SYSTEM 系统首页、APPLICATION 应用首页
     */
    private HomeScopeEnum scopeType;
    /**
     * 所属应用ID
     */
    private Long appId;
    /**
     * 菜单ID
     */
    private Long menuId;
    /**
     * 排序号
     */
    private Integer seq;
}

package sm.domain.sys.base.user.quicklaunch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_home_quick_launch")
public class UserHomeQuickLaunchEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private HomeScopeEnum scopeType;
    private Long appId;
    private Long menuId;
    private Integer seq;
}

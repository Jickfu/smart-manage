package sm.domain.sys.base.passwordconfig.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 全系统密码策略单例，数据库约束固定唯一身份。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_password_config")
public class PasswordConfigEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Integer minLength;
    private Boolean complexityEnabled;
    @Version
    private Integer version;
}

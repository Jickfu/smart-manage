package sm.domain.sys.base.passwordconfig.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 全系统密码策略单例，数据库约束固定唯一身份。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_password_config")
public class PasswordConfigEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 密码最小长度
     */
    private Integer minLength;
    /**
     * 是否启用四类字符复杂度
     */
    private Boolean complexityEnabled;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

package sm.domain.sys.base.weakpassword.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 公共弱口令词条，不与任何用户凭据关联。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_weak_password")
public class WeakPasswordEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 完整弱口令词条
     */
    private String word;
    /**
     * 规范化词条的SHA-256匹配摘要
     */
    private String matchDigest;
    /**
     * 说明
     */
    private String description;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

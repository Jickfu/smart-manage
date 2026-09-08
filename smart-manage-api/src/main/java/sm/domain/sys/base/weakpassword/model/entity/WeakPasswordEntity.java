package sm.domain.sys.base.weakpassword.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 公共弱口令词条，不与任何用户凭据关联。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_weak_password")
public class WeakPasswordEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String word;
    private String matchDigest;
    private String description;
    @Version
    private Integer version;
}

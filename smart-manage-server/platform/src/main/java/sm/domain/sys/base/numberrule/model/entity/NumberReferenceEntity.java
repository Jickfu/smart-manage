package sm.domain.sys.base.numberrule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 编号引用持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_number_reference")
public class NumberReferenceEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 稳定编号引用键
     */
    private String referenceKey;
    /**
     * 所属功能ID
     */
    private Long featureId;
    /**
     * 名称
     */
    private String name;
    /**
     * 默认编号规则键
     */
    private String defaultRuleKey;
    /**
     * 是否系统预置
     */
    private Boolean systemPreset;
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

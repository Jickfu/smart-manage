package sm.domain.sys.base.numberrule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_number_rule")
public class NumberRuleEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ruleKey;
    private String referenceKey;
    private String name;
    private String pattern;
    private String scopeType;
    private String resetPeriod;
    private Long startValue;
    private Boolean enabled;
    private Boolean systemPreset;
    private String description;
    @Version
    private Integer version;
}

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
@TableName("t_sys_number_reference")
public class NumberReferenceEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String referenceKey;
    private Long featureId;
    private String name;
    private String defaultRuleKey;
    private Boolean systemPreset;
    private String description;
    @Version
    private Integer version;
}

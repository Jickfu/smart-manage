package sm.domain.sys.base.numberrule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_sys_number_rule_segment")
public class NumberRuleSegmentEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ruleKey;
    private Integer sort;
    private String segmentType;
    private String value;
    private String format;
    private Integer length;
    private String separator;
}

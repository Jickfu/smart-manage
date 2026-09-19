package sm.domain.sys.base.numberrule.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberRuleSegmentVO {
    private Integer sort;
    private String segmentType;
    private String value;
    private String format;
    private Integer length;
    private String separator;
}

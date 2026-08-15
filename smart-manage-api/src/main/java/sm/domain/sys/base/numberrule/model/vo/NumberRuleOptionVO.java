package sm.domain.sys.base.numberrule.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NumberRuleOptionVO {
    private Long id;
    private String ruleKey;
    private String referenceKey;
    private String name;
    private String scopeType;
    private String pattern;
    private Boolean defaultRule;
}

package sm.domain.sys.base.numberrule.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class NumberRuleVO {
    private Long id;
    private Integer version;
    private String ruleKey;
    private String referenceKey;
    private String referenceName;
    private Long featureId;
    private String featureKey;
    private String featureName;
    private Long appId;
    private String appName;
    private Long cloudId;
    private String cloudName;
    private String name;
    private String pattern;
    private String scopeType;
    private String resetPeriod;
    private Integer startValue;
    private Boolean enabled;
    private Boolean systemPreset;
    private Boolean defaultRule;
    private Long usageCount;
    private List<NumberRuleSegmentVO> segments;
    private String description;
}

package sm.domain.sys.base.numberrule.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class NumberReferenceVO {
    private String referenceKey;
    private String name;
    private Long featureId;
    private String featureKey;
    private String featureName;
    private Long appId;
    private String appName;
    private Long domainId;
    private String domainName;
    private Set<String> allowedScopes;
    private List<NumberVariableVO> variables;
}

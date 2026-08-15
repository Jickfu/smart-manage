package sm.domain.sys.base.basicdata.model.vo;

import lombok.Data;

@Data
public class BasicDataCategoryVO {
    private Long id;
    private Long cloudId;
    private String cloudName;
    private String number;
    private String name;
    private String description;
    private Boolean enabled;
    private Boolean systemPreset;
    private String numberMode;
    private String numberRuleKey;
    private Integer version;
}

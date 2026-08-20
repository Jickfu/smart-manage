package sm.domain.sys.base.feature.model.vo;

import lombok.Data;

@Data
public class FeatureVO {
    private Long id;
    private Integer version;
    private String featureKey;
    private Long appId;
    private String appName;
    private Long domainId;
    private String domainName;
    private String defaultName;
    private String customName;
    private String name;
    private Integer defaultSeq;
    private Integer customSeq;
    private Integer seq;
    private String description;
    private Boolean visible;
    private String source;
}

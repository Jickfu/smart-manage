package sm.domain.sys.base.app.model.vo;

import lombok.Data;

/**
 * 领域与应用联表查询的扁平结果，仅用于服务层组装。
 */
@Data
public class DomainAppRowVO {
    private Long domainId;
    private String domainName;
    private String domainNumber;
    private Integer domainSeq;
    private Long appId;
    private String appName;
    private String appNumber;
    private String appIcon;
    private String appIconColor;
    private Integer appSeq;
    private String appDescription;
}

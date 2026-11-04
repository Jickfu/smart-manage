package sm.domain.sys.base.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 领域与应用联表查询的扁平结果，仅用于服务层组装。
 */
@Schema(description = "领域应用行输出模型")
@Data
public class DomainAppRowVO {
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属领域名称")
    private String domainName;
    @Schema(description = "所属领域编号（t_sys_domain.number）")
    private String domainNumber;
    @Schema(description = "领域排序号")
    private Integer domainSeq;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属应用名称")
    private String appName;
    @Schema(description = "应用编码")
    private String appNumber;
    @Schema(description = "应用图标")
    private String appIcon;
    @Schema(description = "应用图标颜色")
    private String appIconColor;
    @Schema(description = "应用排序号")
    private Integer appSeq;
    @Schema(description = "应用说明")
    private String appDescription;
}

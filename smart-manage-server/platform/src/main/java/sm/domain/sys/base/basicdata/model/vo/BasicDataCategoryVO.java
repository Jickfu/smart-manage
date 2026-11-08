package sm.domain.sys.base.basicdata.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "基础资料分类输出模型")
@Data
public class BasicDataCategoryVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属领域名称")
    private String domainName;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "是否系统预置")
    private Boolean systemPreset;
    @Schema(description = "节点编号模式：MANUAL人工、AUTO_LOCKED自动锁定、AUTO_DEFAULT留空自动")
    private String numberMode;
    @Schema(description = "节点编号规则键")
    private String numberRuleKey;
    @Schema(description = "乐观锁版本号")
    private Integer version;
}

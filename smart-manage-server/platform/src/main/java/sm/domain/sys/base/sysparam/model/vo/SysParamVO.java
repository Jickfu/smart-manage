package sm.domain.sys.base.sysparam.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 系统参数 VO
 *
 * @author Chekfu
 */
@Schema(description = "系统参数输出模型")
@Data
public class SysParamVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "业务编码")
    private String number;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "系统参数值，具体格式由所属参数定义")
    private String value;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "是否系统内置")
    private Boolean isSystem;

    @Schema(description = "所属功能ID")
    private Long featureId;

    @Schema(description = "所属功能名称")
    private String featureName;

    @Schema(description = "所属应用ID")
    private Long appId;

    @Schema(description = "所属应用名称")
    private String appName;
}

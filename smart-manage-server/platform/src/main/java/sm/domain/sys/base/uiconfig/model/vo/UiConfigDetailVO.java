package sm.domain.sys.base.uiconfig.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 界面配置详情 VO（含所有字段，供消费端使用）
 *
 * @author Chekfu
 */
@Schema(description = "界面配置详情输出模型")
@Data
public class UiConfigDetailVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "页面标题")
    private String pageTitle;

    @Schema(description = "系统名称")
    private String systemName;

    @Schema(description = "登录页Banner")
    private String loginBanner;

    @Schema(description = "登录页Logo")
    private String loginLogo;

    @Schema(description = "顶部Logo")
    private String headerLogo;

    @Schema(description = "登录页Banner附件ID")
    private Long loginBannerAttachmentId;

    @Schema(description = "登录页Logo附件ID")
    private Long loginLogoAttachmentId;

    @Schema(description = "顶部Logo附件ID")
    private Long headerLogoAttachmentId;

    @Schema(description = "是否启用水印")
    private Boolean watermarkEnabled;

    @Schema(description = "水印固定内容")
    private String watermarkContent;

    @Schema(description = "水印是否显示当前用户姓名")
    private Boolean watermarkShowName;

    @Schema(description = "水印是否显示脱敏手机号")
    private Boolean watermarkShowPhone;

    @Schema(description = "水印是否显示脱敏邮箱")
    private Boolean watermarkShowEmail;

    @Schema(description = "水印是否显示当前用户工号")
    private Boolean watermarkShowNumber;

    @Schema(description = "水印是否显示当前用户绝对顶层组织名称")
    private Boolean watermarkShowRootOrg;

    @Schema(description = "水印水平间距（像素）")
    private Integer watermarkGapX;

    @Schema(description = "水印垂直间距（像素）")
    private Integer watermarkGapY;

    @Schema(description = "水印字体大小（像素）")
    private Integer watermarkFontSize;
}

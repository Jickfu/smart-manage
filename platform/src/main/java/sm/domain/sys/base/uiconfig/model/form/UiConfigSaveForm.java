package sm.domain.sys.base.uiconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

/**
 * 界面配置保存表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "界面配置保存表单")
public class UiConfigSaveForm {

    @Schema(description = "主键ID（新建时不传）")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必传")
    private Integer version;

    @NotBlank(message = "页面标题不能为空")
    @Schema(description = "页面标题")
    private String pageTitle;

    @Schema(description = "登录页 banner 图片路径")
    private String loginBanner;

    @Schema(description = "登录页 logo 路径")
    private String loginLogo;

    @NotBlank(message = "系统名称不能为空")
    @Schema(description = "系统名称")
    private String systemName;

    @Schema(description = "首页 header logo 路径")
    private String headerLogo;

    @Schema(description = "登录页Banner附件ID")
    private Long loginBannerAttachmentId;

    @Schema(description = "登录页Logo附件ID")
    private Long loginLogoAttachmentId;

    @Schema(description = "顶部Logo附件ID")
    private Long headerLogoAttachmentId;

    @NotNull(message = "水印启用状态不能为空")
    @Schema(description = "是否启用登录后主应用水印")
    private Boolean watermarkEnabled;

    @Size(max = 200, message = "水印固定内容不能超过200个字符")
    @Schema(description = "水印固定内容")
    private String watermarkContent;

    @NotNull(message = "姓名水印配置不能为空")
    private Boolean watermarkShowName;

    @NotNull(message = "手机号水印配置不能为空")
    private Boolean watermarkShowPhone;

    @NotNull(message = "邮箱水印配置不能为空")
    private Boolean watermarkShowEmail;

    @NotNull(message = "工号水印配置不能为空")
    private Boolean watermarkShowNumber;

    @NotNull(message = "最根级组织水印配置不能为空")
    private Boolean watermarkShowRootOrg;

    @NotNull(message = "水印水平间距不能为空")
    @Min(value = 20, message = "水印水平间距不能小于20像素")
    @Max(value = 500, message = "水印水平间距不能大于500像素")
    private Integer watermarkGapX;

    @NotNull(message = "水印垂直间距不能为空")
    @Min(value = 20, message = "水印垂直间距不能小于20像素")
    @Max(value = 500, message = "水印垂直间距不能大于500像素")
    private Integer watermarkGapY;

    @NotNull(message = "水印字体大小不能为空")
    @Min(value = 12, message = "水印字体大小不能小于12像素")
    @Max(value = 32, message = "水印字体大小不能大于32像素")
    private Integer watermarkFontSize;

    @Schema(description = "本次上传附件的上传会话")
    private Map<Long, String> attachmentUploadSessions;
}

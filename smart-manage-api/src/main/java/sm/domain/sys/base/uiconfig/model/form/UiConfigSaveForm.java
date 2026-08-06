package sm.domain.sys.base.uiconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "本次上传附件的上传会话")
    private Map<Long, String> attachmentUploadSessions;
}

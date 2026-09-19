package sm.domain.sys.base.uiconfig.model.vo;

import lombok.Data;

/**
 * 界面配置详情 VO（含所有字段，供消费端使用）
 *
 * @author Chekfu
 */
@Data
public class UiConfigDetailVO {

    private Long id;

    private Integer version;

    private String pageTitle;

    private String systemName;

    private String loginBanner;

    private String loginLogo;

    private String headerLogo;

    private Long loginBannerAttachmentId;

    private Long loginLogoAttachmentId;

    private Long headerLogoAttachmentId;

    private Boolean watermarkEnabled;

    private String watermarkContent;

    private Boolean watermarkShowName;

    private Boolean watermarkShowPhone;

    private Boolean watermarkShowEmail;

    private Boolean watermarkShowNumber;

    private Boolean watermarkShowRootOrg;

    private Integer watermarkGapX;

    private Integer watermarkGapY;

    private Integer watermarkFontSize;
}

package sm.domain.sys.base.uiconfig.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 界面配置实体
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_ui_config")
public class UiConfigEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 页面标题 */
    private String pageTitle;

    /** 登录页 banner 图片路径 */
    private String loginBanner;

    /** 登录页 logo 路径 */
    private String loginLogo;

    /** 系统名称 */
    private String systemName;

    /** 首页 header logo 路径 */
    private String headerLogo;

    /** 登录页 Banner 附件 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long loginBannerAttachmentId;

    /** 登录页 Logo 附件 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long loginLogoAttachmentId;

    /** 顶部 Logo 附件 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long headerLogoAttachmentId;

    /** 是否启用登录后主应用水印 */
    private Boolean watermarkEnabled;

    /** 水印固定内容 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String watermarkContent;

    private Boolean watermarkShowName;

    private Boolean watermarkShowPhone;

    private Boolean watermarkShowEmail;

    private Boolean watermarkShowNumber;

    private Boolean watermarkShowRootOrg;

    /** 水印水平间距（像素） */
    private Integer watermarkGapX;

    /** 水印垂直间距（像素） */
    private Integer watermarkGapY;

    /** 水印字体大小（像素） */
    private Integer watermarkFontSize;

    @Version
    private Integer version;
}

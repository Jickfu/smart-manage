package sm.domain.sys.base.uiconfig.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    private Long loginBannerAttachmentId;

    /** 登录页 Logo 附件 ID */
    private Long loginLogoAttachmentId;

    /** 顶部 Logo 附件 ID */
    private Long headerLogoAttachmentId;

    @Version
    private Integer version;
}

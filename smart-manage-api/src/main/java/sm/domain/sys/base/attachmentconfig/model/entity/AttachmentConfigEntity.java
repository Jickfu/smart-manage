package sm.domain.sys.base.attachmentconfig.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 附件全局限制配置单例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_attachment_config")
public class AttachmentConfigEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long maxUploadBytes;
    private String allowedExtensions;
    private String allowedMimeTypes;
    private Integer tempExpireHours;
    @Version
    private Integer version;
}

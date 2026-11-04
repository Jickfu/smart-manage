package sm.domain.sys.base.attachmentconfig.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 附件全局限制配置单例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_attachment_config")
public class AttachmentConfigEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 单文件最大字节数
     */
    private Long maxUploadBytes;
    /**
     * 允许扩展名，逗号分隔
     */
    private String allowedExtensions;
    /**
     * 允许MIME类型，逗号分隔
     */
    private String allowedMimeTypes;
    /**
     * 临时附件有效小时数
     */
    private Integer tempExpireHours;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

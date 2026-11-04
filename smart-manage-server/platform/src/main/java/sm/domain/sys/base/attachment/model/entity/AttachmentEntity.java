package sm.domain.sys.base.attachment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
import java.time.LocalDateTime;

/**
 * 附件实体
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_attachment")
public class AttachmentEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * MIME类型
     */
    private String mimeType;
    /**
     * 文件扩展名
     */
    private String fileExt;
    /**
     * 文件存储类型：LOCAL 本地、FTP、S3 兼容对象存储
     */
    private String storageType;
    /**
     * 稳定对象键或兼容存储定位键
     */
    private String objectKey;
    /**
     * 附件状态：TEMP、ACTIVE、PENDING_DELETE、DELETED
     */
    private String status;
    /**
     * 上传会话ID
     */
    private String uploadSessionId;
    /**
     * 临时附件过期时间
     */
    private LocalDateTime expiresAt;
    /**
     * 文件SHA-256摘要
     */
    private String sha256;
}

package sm.domain.sys.base.fileartifact.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 具有独立授权、有效期和删除补偿语义的文件制品。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_file_artifact")
public class FileArtifactEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 受控文件用途
     */
    private String purpose;
    /**
     * 所有者用户ID
     */
    private Long ownerUserId;
    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 文件存储类型：LOCAL 本地、FTP、S3 兼容对象存储
     */
    private String storageType;
    /**
     * 稳定对象键或兼容存储定位键
     */
    private String objectKey;
    /**
     * MIME类型
     */
    private String mimeType;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件制品状态：ACTIVE、DOWNLOADING、PENDING_DELETE、DELETED
     */
    private String status;
    /**
     * 文件制品有效期截止时间
     */
    private LocalDateTime expiresAt;
    /**
     * 已下载次数
     */
    private Integer downloadCount;
    /**
     * 最大下载次数
     */
    private Integer maxDownloads;
    /**
     * 下载资格声明令牌
     */
    private String downloadClaimToken;
    /**
     * 下载资格声明时间
     */
    private LocalDateTime downloadClaimedAt;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

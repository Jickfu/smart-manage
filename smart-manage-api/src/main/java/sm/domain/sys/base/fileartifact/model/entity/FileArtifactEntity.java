package sm.domain.sys.base.fileartifact.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/** 具有独立授权、有效期和删除补偿语义的文件制品。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_file_artifact")
public class FileArtifactEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String purpose;
    private Long ownerUserId;
    private String originalName;
    private String storageType;
    private String objectKey;
    private String mimeType;
    private Long fileSize;
    private String status;
    private LocalDateTime expiresAt;
    private Integer downloadCount;
    private Integer maxDownloads;
    @Version
    private Integer version;
}

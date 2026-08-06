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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String originalName;
    private Long fileSize;
    private String mimeType;
    private String fileExt;
    private String storageType;
    private String objectKey;
    private String status;
    private String uploadSessionId;
    private LocalDateTime expiresAt;
    private String sha256;
}

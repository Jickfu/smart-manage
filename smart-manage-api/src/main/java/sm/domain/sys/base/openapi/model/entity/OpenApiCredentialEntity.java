package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_credential")
public class OpenApiCredentialEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long applicationId;
    private String keyId;
    private String name;
    private Boolean enabled;
    private String encryptionAlgorithm;
    private String signingSecretCipher;
    private String requestEncryptionKeyCipher;
    private String responseEncryptionKeyCipher;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    @Version
    private Integer version;
}

package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 开放接口凭据持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_credential")
public class OpenApiCredentialEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 接入应用 ID
     */
    private Long applicationId;
    /**
     * 凭据公开标识，不是密钥正文
     */
    private String keyId;
    /**
     * 名称
     */
    private String name;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 报文加密算法：NONE、AES_256_GCM、SM4_GCM
     */
    private String encryptionAlgorithm;
    /**
     * 加密保存的签名密钥
     */
    private String signingSecretCipher;
    /**
     * 加密保存的请求报文密钥
     */
    private String requestEncryptionKeyCipher;
    /**
     * 加密保存的响应报文密钥
     */
    private String responseEncryptionKeyCipher;
    /**
     * API 凭据有效期截止时间，为空表示未设置过期时间
     */
    private LocalDateTime expiresAt;
    /**
     * 凭据最后使用时间
     */
    private LocalDateTime lastUsedAt;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 邮件发信账号持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_email_account")
public class EmailAccountEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 名称
     */
    private String name;
    /**
     * SMTP 服务器主机名
     */
    private String host;
    /**
     * SMTP 服务器端口
     */
    private Integer port;
    /**
     * SMTP 传输安全模式：NONE、STARTTLS、SSL_TLS
     */
    private String securityMode;
    /**
     * SMTP 认证用户名
     */
    private String username;
    /**
     * SMTP 密码或授权码的 SM4/GCM 密文
     */
    private String passwordCipher;
    /**
     * 发件人邮箱地址
     */
    private String fromAddress;
    /**
     * 发件人显示名称
     */
    private String fromName;
    /**
     * 回复邮箱地址
     */
    private String replyTo;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 是否为全局默认发信账号
     */
    private Boolean defaultAccount;
    /**
     * 是否允许管理员手工选择
     */
    private Boolean allowManual;
    /**
     * SMTP 连接超时，单位毫秒
     */
    private Integer connectionTimeoutMs;
    /**
     * SMTP 读取超时，单位毫秒
     */
    private Integer readTimeoutMs;
    /**
     * 说明
     */
    private String description;
    /**
     * 乐观锁版本号
     */
    @Version private Integer version;
}

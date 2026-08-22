package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_email_account")
public class EmailAccountEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private String number;
    private String name;
    private String host;
    private Integer port;
    private String securityMode;
    private String username;
    private String passwordCipher;
    private String fromAddress;
    private String fromName;
    private String replyTo;
    private Boolean enabled;
    private Boolean defaultAccount;
    private Boolean allowManual;
    private Integer connectionTimeoutMs;
    private Integer readTimeoutMs;
    private String description;
    @Version private Integer version;
}

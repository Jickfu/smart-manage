package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 开放接口接入应用持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_application")
public class OpenApiApplicationEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 名称
     */
    private String name;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 外部调用建立业务上下文时使用的普通用户
     */
    private Long proxyUserId;
    /**
     * 外部调用固定组织，不跟随代理用户浏览器会话
     */
    private Long proxyOrgId;
    /**
     * 认证方式，当前仅支持 HMAC_SHA256
     */
    private String authenticationType;
    /**
     * 报文加密算法：NONE、AES_256_GCM、SM4_GCM
     */
    private String encryptionAlgorithm;
    /**
     * IP 访问策略：DISABLED 不限制、WHITELIST 白名单、BLACKLIST 黑名单
     */
    private String ipPolicyMode;
    /**
     * 每行一个 IPv4/IPv6 地址或 CIDR 网段
     */
    private String ipRanges;
    /**
     * 说明
     */
    private String description;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

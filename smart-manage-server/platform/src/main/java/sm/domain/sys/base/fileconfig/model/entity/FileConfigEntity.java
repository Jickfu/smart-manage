package sm.domain.sys.base.fileconfig.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 文件配置实体
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_file_config")
public class FileConfigEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件存储类型：LOCAL 本地、FTP、S3 兼容对象存储
     */
    private String storageType;

    /**
     * 本地目录路径
     */
    private String localDir;

    /**
     * FTP 主机
     */
    private String ftpHost;

    /**
     * FTP 端口
     */
    private Integer ftpPort;

    /**
     * FTP 用户名
     */
    private String ftpUsername;

    /**
     * FTP 密码密文
     */
    private String ftpPasswordCipher;

    /**
     * FTP 远程目录
     */
    private String ftpDir;

    /**
     * FTP 被动模式
     */
    private Boolean ftpPassiveMode;

    /**
     * S3兼容服务地址
     */
    private String s3Endpoint;
    /**
     * S3区域
     */
    private String s3Region;
    /**
     * S3私有Bucket
     */
    private String s3Bucket;
    /**
     * S3 访问标识（Access Key）
     */
    private String s3AccessKey;
    /**
     * S3 Secret Key密文
     */
    private String s3SecretKeyCipher;
    /**
     * 是否使用Path Style
     */
    private Boolean s3PathStyle;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

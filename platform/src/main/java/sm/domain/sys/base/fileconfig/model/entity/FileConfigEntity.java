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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 存储类型：LOCAL / FTP */
    private String storageType;

    /** 本地目录路径 */
    private String localDir;

    /** FTP 主机 */
    private String ftpHost;

    /** FTP 端口 */
    private Integer ftpPort;

    /** FTP 用户名 */
    private String ftpUsername;

    /** FTP 密码密文 */
    private String ftpPasswordCipher;

    /** FTP 远程目录 */
    private String ftpDir;

    /** FTP 被动模式 */
    private Boolean ftpPassiveMode;

    private String s3Endpoint;
    private String s3Region;
    private String s3Bucket;
    private String s3AccessKey;
    private String s3SecretKeyCipher;
    private Boolean s3PathStyle;

    @Version
    private Integer version;
}

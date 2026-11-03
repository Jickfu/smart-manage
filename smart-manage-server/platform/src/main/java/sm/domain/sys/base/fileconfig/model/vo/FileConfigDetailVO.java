package sm.domain.sys.base.fileconfig.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 文件配置详情 VO
 *
 * @author Chekfu
 */
@Schema(description = "文件存储配置详情输出模型")
@Data
public class FileConfigDetailVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "文件存储类型：LOCAL 本地、FTP、S3 兼容对象存储")
    private String storageType;

    @Schema(description = "本地存储目录")
    private String localDir;

    @Schema(description = "FTP 主机")
    private String ftpHost;

    @Schema(description = "FTP 端口")
    private Integer ftpPort;

    @Schema(description = "FTP 用户名")
    private String ftpUsername;

    @Schema(description = "是否已配置 FTP 密码，不返回密码正文")
    private Boolean ftpPasswordConfigured;

    @Schema(description = "FTP 远程目录")
    private String ftpDir;

    @Schema(description = "FTP被动模式")
    private Boolean ftpPassiveMode;

    @Schema(description = "S3兼容服务地址")
    private String s3Endpoint;
    @Schema(description = "S3区域")
    private String s3Region;
    @Schema(description = "S3私有Bucket")
    private String s3Bucket;
    @Schema(description = "S3 访问标识（Access Key）")
    private String s3AccessKey;
    @Schema(description = "是否已配置 S3 访问密钥，不返回密钥正文")
    private Boolean s3SecretKeyConfigured;
    @Schema(description = "是否使用Path Style")
    private Boolean s3PathStyle;
}

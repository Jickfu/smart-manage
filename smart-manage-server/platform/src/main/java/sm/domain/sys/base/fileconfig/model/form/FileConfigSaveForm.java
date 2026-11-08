package sm.domain.sys.base.fileconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文件配置保存表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "文件配置保存表单")
public class FileConfigSaveForm {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必传")
    private Integer version;

    @NotBlank(message = "存储类型不能为空")
    @Schema(description = "文件存储类型：LOCAL 本地、FTP、S3 兼容对象存储")
    private String storageType;

    @Schema(description = "本地目录路径")
    private String localDir;

    @Schema(description = "FTP 主机")
    private String ftpHost;

    @Schema(description = "FTP 端口")
    private Integer ftpPort;

    @Schema(description = "FTP 用户名")
    private String ftpUsername;

    @Schema(description = "FTP 密码")
    private String ftpPassword;

    @Schema(description = "FTP 远程目录")
    private String ftpDir;

    @Schema(description = "FTP 被动模式")
    private Boolean ftpPassiveMode;

    @Schema(description = "S3兼容服务地址")
    private String s3Endpoint;
    @Schema(description = "S3区域")
    private String s3Region;
    @Schema(description = "S3私有Bucket")
    private String s3Bucket;
    @Schema(description = "S3 访问标识（Access Key）")
    private String s3AccessKey;
    @Schema(description = "S3 访问密钥，按现有敏感配置提交规则处理")
    private String s3SecretKey;
    @Schema(description = "是否使用Path Style")
    private Boolean s3PathStyle;
}

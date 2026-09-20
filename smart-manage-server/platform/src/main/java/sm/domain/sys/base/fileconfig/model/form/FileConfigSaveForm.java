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

    @Schema(description = "主键ID（新建时不传）")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必传")
    private Integer version;

    @NotBlank(message = "存储类型不能为空")
    @Schema(description = "存储类型：LOCAL / FTP")
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

    private String s3Endpoint;
    private String s3Region;
    private String s3Bucket;
    private String s3AccessKey;
    private String s3SecretKey;
    private Boolean s3PathStyle;
}

package sm.domain.sys.base.fileconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * FTP 连接测试表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "FTP连接测试表单")
public class FtpTestForm {

    @NotBlank(message = "FTP主机不能为空")
    @Schema(description = "FTP 主机")
    private String ftpHost;

    @NotNull(message = "端口不能为空")
    @Schema(description = "FTP 端口")
    private Integer ftpPort;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "FTP 用户名")
    private String ftpUsername;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "FTP 密码")
    private String ftpPassword;

    @Schema(description = "FTP 远程目录")
    private String ftpDir;

    @Schema(description = "被动模式")
    private Boolean ftpPassiveMode;
}

package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "发信账号保存输入表单")
public record AccountSaveForm(
    @Schema(description = "ID")
    Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    Integer version,
    @Schema(description = "业务编码")
    @NotBlank @Size(max = 64) String number,
    @Schema(description = "名称")
    @NotBlank @Size(max = 100) String name,
    @Schema(description = "SMTP 服务器主机名")
    @NotBlank @Size(max = 255) String host,
    @Schema(description = "SMTP 服务器端口")
    @NotNull @Min(1) @Max(65535) Integer port,
    @Schema(description = "SMTP 传输安全模式：NONE、STARTTLS、SSL_TLS")
    @NotBlank String securityMode,
    @Schema(description = "SMTP 认证用户名")
    @NotBlank @Size(max = 255) String username,
    @Schema(description = "SMTP 密码或授权码，服务端加密保存；新增时必填，修改时留空保留原值")
    @Size(max = 1000) String password,
    @Schema(description = "发件人邮箱地址")
    @NotBlank @Email @Size(max = 320) String fromAddress,
    @Schema(description = "发件人显示名称")
    @Size(max = 100) String fromName,
    @Schema(description = "回复邮箱地址")
    @Email @Size(max = 320) String replyTo,
    @Schema(description = "是否为全局默认发信账号")
    @NotNull Boolean defaultAccount,
    @Schema(description = "是否允许管理员手工选择")
    @NotNull Boolean allowManual,
    @Schema(description = "SMTP 连接超时，单位毫秒")
    @NotNull @Min(1000) @Max(60000) Integer connectionTimeoutMs,
    @Schema(description = "SMTP 读取超时，单位毫秒")
    @NotNull @Min(1000) @Max(60000) Integer readTimeoutMs,
    @Schema(description = "说明")
    @Size(max = 500) String description) {
}

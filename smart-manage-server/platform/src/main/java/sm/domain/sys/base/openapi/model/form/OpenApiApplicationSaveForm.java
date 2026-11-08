package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "开放接口接入应用保存输入表单")
public record OpenApiApplicationSaveForm(
    @Schema(description = "ID")
    Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    Integer version,
    @Schema(description = "业务编码")
    @NotBlank @Size(max = 100) @Pattern(regexp = "[A-Za-z0-9._-]+") String number,
    @Schema(description = "名称")
    @NotBlank @Size(max = 200) String name,
    @Schema(description = "外部调用建立业务上下文时使用的普通用户")
    @NotNull Long proxyUserId,
    @Schema(description = "外部调用固定组织，不跟随代理用户浏览器会话")
    @NotNull Long proxyOrgId,
    @Schema(description = "认证方式，当前仅支持 HMAC_SHA256")
    @NotBlank String authenticationType,
    @Schema(description = "报文加密算法：NONE、AES_256_GCM、SM4_GCM")
    @NotBlank String encryptionAlgorithm,
    @Schema(description = "IP 访问策略：DISABLED 不限制、WHITELIST 白名单、BLACKLIST 黑名单")
    @NotBlank String ipPolicyMode,
    @Schema(description = "每行一个 IPv4/IPv6 地址或 CIDR 网段")
    String ipRanges,
    @Schema(description = "说明")
    @Size(max = 500) String description,
    @Schema(description = "授权的 API 操作标识列表")
    @NotNull List<@NotBlank @Size(max = 200) String> operationKeys) {
}

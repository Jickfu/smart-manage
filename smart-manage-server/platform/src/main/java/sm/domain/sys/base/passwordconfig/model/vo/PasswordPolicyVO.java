package sm.domain.sys.base.passwordconfig.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 公开的规则快照，不包含维护身份和版本，不接收候选密码。
 */
@Schema(description = "密码策略输出模型")
public record PasswordPolicyVO(
    @Schema(description = "密码最小长度")
    int minLength,
    @Schema(description = "密码最大长度")
    int maxLength,
    @Schema(description = "是否启用四类字符复杂度")
    boolean complexityEnabled) {}

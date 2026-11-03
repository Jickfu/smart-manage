package sm.domain.sys.base.passwordconfig.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "密码策略配置详情输出模型")
public record PasswordConfigDetailVO(
    @Schema(description = "ID")
    Long id,
    @Schema(description = "乐观锁版本号")
    Integer version,
    @Schema(description = "密码最小长度")
    int minLength,
    @Schema(description = "是否启用四类字符复杂度")
    boolean complexityEnabled) {}

package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "开放接口凭据创建输入表单")
public record OpenApiCredentialCreateForm(
    @Schema(description = "接入应用 ID")
    @NotNull Long applicationId,
    @Schema(description = "名称")
    @NotBlank @Size(max = 200) String name,
    @Schema(description = "API 凭据有效期截止时间，为空表示未设置过期时间")
    LocalDateTime expiresAt) {
}

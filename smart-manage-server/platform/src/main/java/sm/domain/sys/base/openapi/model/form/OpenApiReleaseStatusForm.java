package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "开放接口版本状态变更输入表单")
public record OpenApiReleaseStatusForm(
    @Schema(description = "ID")
    @NotNull Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull Integer version,
    @Schema(description = "API 发布状态：PUBLISHED 已发布、OFFLINE 已下线")
    @NotBlank String status) {
}

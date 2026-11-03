package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "发信账号启停输入表单")
public record AccountEnableForm(
    @Schema(description = "ID")
    @NotNull Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull Integer version,
    @Schema(description = "是否启用")
    @NotNull Boolean enabled) {
}

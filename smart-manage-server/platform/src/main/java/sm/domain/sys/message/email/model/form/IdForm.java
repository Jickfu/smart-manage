package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "主键输入表单")
public record IdForm(
    @Schema(description = "ID")
    @NotNull Long id) {
}

package sm.domain.sys.message.email.model.form;

import jakarta.validation.constraints.NotNull;

public record AccountEnableForm(@NotNull Long id, @NotNull Integer version, @NotNull Boolean enabled) {
}

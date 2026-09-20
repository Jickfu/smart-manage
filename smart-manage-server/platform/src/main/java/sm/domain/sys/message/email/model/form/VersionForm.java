package sm.domain.sys.message.email.model.form;

import jakarta.validation.constraints.NotNull;

public record VersionForm(@NotNull Long id, @NotNull Integer version) {
}

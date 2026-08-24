package sm.domain.sys.message.email.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountTestForm(@NotNull Long accountId, @Email @Size(max = 320) String recipient) {
}

package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OpenApiApplicationSaveForm(
        Long id,
        Integer version,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[A-Za-z0-9._-]+") String number,
        @NotBlank @Size(max = 200) String name,
        @NotNull Long proxyUserId,
        @NotNull Long proxyOrgId,
        @NotBlank String authenticationType,
        @NotBlank String encryptionAlgorithm,
        @NotBlank String ipPolicyMode,
        String ipRanges,
        @Size(max = 500) String description,
        @NotNull List<@NotBlank @Size(max = 200) String> operationKeys) {
}

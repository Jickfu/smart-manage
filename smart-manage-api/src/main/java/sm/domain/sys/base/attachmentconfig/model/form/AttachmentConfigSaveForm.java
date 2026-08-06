package sm.domain.sys.base.attachmentconfig.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AttachmentConfigSaveForm {
    private Long id;
    private Integer version;
    @NotNull
    @Min(1)
    @Max(1073741824)
    private Long maxUploadBytes;
    @NotEmpty
    private List<String> allowedExtensions;
    @NotEmpty
    private List<String> allowedMimeTypes;
    @NotNull
    @Min(1)
    @Max(168)
    private Integer tempExpireHours;
}

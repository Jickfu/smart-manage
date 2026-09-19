package sm.domain.sys.base.feature.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeatureSaveForm {
    @NotNull
    private Long id;
    @NotNull
    private Integer version;
    private String customName;
    private Integer customSeq;
    private String description;
    @NotNull
    private Boolean visible;
}

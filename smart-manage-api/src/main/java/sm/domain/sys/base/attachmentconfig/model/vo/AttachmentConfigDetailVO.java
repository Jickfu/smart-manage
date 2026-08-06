package sm.domain.sys.base.attachmentconfig.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentConfigDetailVO {
    private Long id;
    private Integer version;
    private Long maxUploadBytes;
    private List<String> allowedExtensions;
    private List<String> allowedMimeTypes;
    private Integer tempExpireHours;
}

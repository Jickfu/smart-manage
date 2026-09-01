package sm.domain.sys.base.user.model.vo;

import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import java.util.List;

public record UserImportResultVO(int total, int success, int failed, List<String> errors,
                                 List<String> warnings,
                                 List<FileArtifactReference> credentialFiles,
                                 FileArtifactReference errorFile) { }

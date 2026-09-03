package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactRegistrar;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.user.model.form.UserSaveForm;

import java.util.List;

/** 用户导入批次的唯一数据库事务 owner。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserImportTxService {
    private final UserWriter userWriter;
    private final FileArtifactRegistrar fileArtifactRegistrar;

    BatchCommitResult commitBatch(List<UserSaveForm> forms, PreparedFileArtifact preparedCredentialFile) {
        List<Long> savedIds = userWriter.saveBatch(forms);
        FileArtifactReference credentialFile = preparedCredentialFile == null
                ? null : fileArtifactRegistrar.registerPrepared(preparedCredentialFile);
        return new BatchCommitResult(savedIds, credentialFile);
    }

    record BatchCommitResult(List<Long> savedIds,
                             FileArtifactReference credentialFile) {
    }
}

package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactRegistrar;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.form.UserSaveForm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 用户导入批次的唯一数据库事务 owner。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserImportTxService {
    private final UserWriter userWriter;
    private final UserRoleMapper userRoleMapper;
    private final FileArtifactRegistrar fileArtifactRegistrar;

    BatchCommitResult commitBatch(List<UserSaveForm> forms, PreparedFileArtifact preparedCredentialFile) {
        Map<Long, List<Long>> previousOrgIds = new HashMap<>();
        for (UserSaveForm form : forms) {
            if (form.getId() != null) {
                previousOrgIds.put(form.getId(), userRoleMapper.selectOrgIdsByUserId(form.getId()));
            }
        }
        List<Long> savedIds = userWriter.saveBatch(forms);
        FileArtifactReference credentialFile = preparedCredentialFile == null
                ? null : fileArtifactRegistrar.registerPrepared(preparedCredentialFile);
        return new BatchCommitResult(savedIds, previousOrgIds, credentialFile);
    }

    record BatchCommitResult(List<Long> savedIds, Map<Long, List<Long>> previousOrgIds,
                             FileArtifactReference credentialFile) {
    }
}

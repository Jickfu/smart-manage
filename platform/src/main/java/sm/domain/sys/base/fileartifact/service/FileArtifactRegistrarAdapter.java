package sm.domain.sys.base.fileartifact.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactRegistrar;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;

/** 文件制品模块对事务协调器发布的最小写入适配器，本身不拥有事务。 */
@Component
@RequiredArgsConstructor
class FileArtifactRegistrarAdapter implements FileArtifactRegistrar {
    private final FileArtifactWriter writer;

    @Override
    public FileArtifactReference registerPrepared(PreparedFileArtifact prepared) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("文件制品登记必须加入调用方已经开启的数据库事务");
        }
        FileArtifactEntity entity = FileArtifactEntityFactory.fromPrepared(prepared);
        writer.insert(entity);
        return new FileArtifactReference(entity.getId(), prepared.originalName(), entity.getExpiresAt());
    }
}

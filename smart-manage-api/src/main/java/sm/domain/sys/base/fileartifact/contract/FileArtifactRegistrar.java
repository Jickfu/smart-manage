package sm.domain.sys.base.fileartifact.contract;

/**
 * 将已准备好的物理对象登记为受管理制品，并加入调用方已经开启的数据库事务。
 * 本 Contract 不创建事务；无活动事务时必须拒绝调用。
 */
public interface FileArtifactRegistrar {
    FileArtifactReference registerPrepared(PreparedFileArtifact prepared);
}

package sm.domain.sys.base.fileartifact.service;

import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;

/** 下载资格声明；claimToken 用于完成或释放同一次流传输。 */
public record FileArtifactDownloadClaim(FileArtifactEntity artifact, String claimToken) {
}

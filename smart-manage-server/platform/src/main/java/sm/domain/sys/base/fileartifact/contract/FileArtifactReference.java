package sm.domain.sys.base.fileartifact.contract;

public record FileArtifactReference(Long id, String originalName, java.time.LocalDateTime expiresAt) { }

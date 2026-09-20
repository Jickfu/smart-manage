package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CacheRuntimeVO {
    private boolean available;
    private String version;
    private long uptimeSeconds;
    private long usedMemoryBytes;
    private String usedMemoryDisplay;
    private long maxMemoryBytes;
    private int connectedClients;
    private long dbSize;
    private long keyspaceHits;
    private long keyspaceMisses;
    private Double hitRate;
    private int database;
    private LocalDateTime collectedAt;
}

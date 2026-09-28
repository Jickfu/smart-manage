package sm.domain.workflow.process.task.model.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record TaskMonitorVO(Long id, Long instanceId, String number, String businessType,
                            String nodeCode, String nodeName, boolean active,
                            List<Long> candidates, Map<Long, String> candidateNames, LocalDateTime createTime) { }

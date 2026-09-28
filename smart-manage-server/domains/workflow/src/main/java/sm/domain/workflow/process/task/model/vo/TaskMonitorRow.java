package sm.domain.workflow.process.task.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskMonitorRow {
    private Long id;
    private Long instanceId;
    private String number;
    private String businessType;
    private String nodeCode;
    private String nodeName;
    private Boolean active;
    private String candidateIds;
    private LocalDateTime createTime;
}

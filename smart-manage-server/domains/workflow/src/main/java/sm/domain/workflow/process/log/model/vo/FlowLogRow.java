package sm.domain.workflow.process.log.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlowLogRow {
    private String id;
    private Long instanceId;
    private Long taskId;
    private String number;
    private String businessType;
    private String nodeName;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String opinion;
    private LocalDateTime eventTime;
    private String source;
}

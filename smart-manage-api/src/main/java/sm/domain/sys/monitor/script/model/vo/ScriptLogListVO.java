package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScriptLogListVO {
    private Long id;
    private Long scriptId;
    private String scriptName;
    private String transactionMode;
    private String executeStatus;
    private Integer executeDuration;
    private String transactionResult;
    private String createName;
    private String createIp;
    private LocalDateTime createTime;
}

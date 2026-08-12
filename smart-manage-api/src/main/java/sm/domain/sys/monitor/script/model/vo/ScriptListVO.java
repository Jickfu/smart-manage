package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScriptListVO {
    private Long id;
    private String number;
    private String name;
    private String description;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

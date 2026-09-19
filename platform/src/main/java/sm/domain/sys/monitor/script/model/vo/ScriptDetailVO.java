package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptDetailVO extends ScriptListVO {
    private String content;
    private Integer version;
}

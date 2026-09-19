package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptLogDetailVO extends ScriptLogListVO {
    private String scriptContent;
    private String output;
    private String errorMessage;
}

package sm.domain.sys.monitor.script.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptLogListForm extends PageForm {
    private String keyword;
    private String status;
    private String transactionMode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

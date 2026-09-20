package sm.domain.sys.monitor.alert.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorAlertIncidentListForm extends PageForm {
    private String status;
    private String severity;
    private String scopeType;
    private String keyword;
}

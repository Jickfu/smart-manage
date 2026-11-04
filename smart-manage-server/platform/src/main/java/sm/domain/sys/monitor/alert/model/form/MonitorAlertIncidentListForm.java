package sm.domain.sys.monitor.alert.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "监控告警事件列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorAlertIncidentListForm extends PageForm {
    @Schema(description = "告警状态：PENDING、FIRING、RECOVERED、CLOSED")
    private String status;
    @Schema(description = "告警严重程度：WARNING 警告、CRITICAL 严重")
    private String severity;
    @Schema(description = "监控作用对象类型：HOST 主机、INSTANCE 应用实例")
    private String scopeType;
    @Schema(description = "查询关键字")
    private String keyword;
}

package sm.domain.sys.monitor.slowsql.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "慢 SQL目标输入表单")
@Data
public class SlowSqlTargetForm {
    @Schema(description = "应用实例唯一标识")
    private String instanceId;
}

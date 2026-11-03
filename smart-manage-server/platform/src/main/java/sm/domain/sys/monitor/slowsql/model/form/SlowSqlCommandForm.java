package sm.domain.sys.monitor.slowsql.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "慢 SQL命令输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class SlowSqlCommandForm extends SlowSqlTargetForm {
    @Schema(description = "慢 SQL 判定阈值，单位毫秒")
    @NotNull(message = "慢 SQL 阈值不能为空")
    @Min(value = 100, message = "慢 SQL 阈值不能小于 100 毫秒")
    @Max(value = 60000, message = "慢 SQL 阈值不能大于 60000 毫秒")
    private Long thresholdMs;
}

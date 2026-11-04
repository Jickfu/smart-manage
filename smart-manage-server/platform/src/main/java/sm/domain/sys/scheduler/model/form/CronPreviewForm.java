package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Cron 触发时间预览输入表单")
@Data
public class CronPreviewForm {
    @Schema(description = "Cron表达式")
    @NotBlank(message = "Cron 表达式不能为空")
    private String cronExpression;
}

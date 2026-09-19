package sm.domain.sys.scheduler.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CronPreviewForm {
    @NotBlank(message = "Cron 表达式不能为空")
    private String cronExpression;
}

package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 定时任务批量状态命令。 */
@Data
@Schema(description = "定时任务批量命令表单")
public class JobBatchCommandForm {

    @Valid
    @NotEmpty(message = "任务列表不能为空")
    @Size(max = 100, message = "单次最多操作100个任务")
    private List<JobCommandForm> jobs;
}

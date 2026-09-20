package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 定时任务状态变更及删除命令。 */
@Data
@Schema(description = "定时任务命令表单")
public class JobCommandForm {

    @NotNull(message = "任务ID不能为空")
    private Long id;

    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}

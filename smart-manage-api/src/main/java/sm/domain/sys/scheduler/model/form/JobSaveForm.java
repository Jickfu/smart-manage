package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 任务保存表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "任务保存表单")
public class JobSaveForm {

    @Schema(description = "ID（新增时不传）")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必填")
    private Integer version;

    @NotBlank(message = "编码不能为空")
    @Schema(description = "任务编码")
    private String number;

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务分组，默认 DEFAULT")
    private String jobGroup;

    @NotBlank(message = "执行类不能为空")
    @Schema(description = "Job 实现类全限定名")
    private String jobClassName;

    @NotBlank(message = "Cron 表达式不能为空")
    @Schema(description = "Cron 表达式")
    private String cronExpression;

    @Schema(description = "任务参数（JSON）")
    private String jobData;

    @Schema(description = "共享资源互斥键；留空表示不启用跨任务互斥")
    private String mutexKey;

    @Schema(description = "备注")
    private String remark;
}

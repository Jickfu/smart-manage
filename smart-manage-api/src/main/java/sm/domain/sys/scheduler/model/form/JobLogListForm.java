package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 执行实例查询表单
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "执行实例分页查询")
public class JobLogListForm extends PageForm {

    @Schema(description = "关键词：任务名称")
    private String keyword;

    @Schema(description = "实例状态：RUNNING / SUCCESS / FAILED")
    private String status;

    @Schema(description = "关联任务ID")
    private Long jobId;
}

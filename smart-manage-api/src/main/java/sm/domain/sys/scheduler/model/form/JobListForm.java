package sm.domain.sys.scheduler.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 任务列表查询表单
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务分页查询")
public class JobListForm extends PageForm {

    @Schema(description = "关键词：任务名称/分组/描述")
    private String keyword;

    @Schema(description = "状态：ENABLED / PAUSED")
    private String status;
}

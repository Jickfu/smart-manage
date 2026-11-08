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

    @Schema(description = "关键词：任务编码/名称")
    private String keyword;

    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属应用ID")
    private Long appId;

    @Schema(description = "任务状态：ENABLED 启用、PAUSED 暂停")
    private String status;
}

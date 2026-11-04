package sm.domain.sys.scheduler.singleton.model.form;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;
import sm.system.form.PageForm;
/** 常驻任务分页查询。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "常驻任务分页查询")
public class SingletonListForm extends PageForm {
    @Schema(description = "任务编码或名称关键词")
    private String keyword;
}

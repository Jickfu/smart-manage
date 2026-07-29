package sm.domain.sys.base.sysparam.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 系统参数列表查询表单
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统参数列表表单")
public class SysParamListForm extends PageForm {

    @Schema(description = "所属应用ID")
    private Long appId;

    @Schema(description = "所属云ID")
    private Long cloudId;

    @Schema(description = "是否只查询全局参数")
    private Boolean globalOnly;

    @Schema(description = "关键词")
    private String keyword;
}

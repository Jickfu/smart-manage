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

    @Schema(description = "所属领域ID")
    private Long domainId;

    @Schema(description = "所属功能ID")
    private Long featureId;

    @Schema(description = "是否只查询全局参数（所属功能为空）")
    private Boolean globalOnly;

    @Schema(description = "关键词")
    private String keyword;
}

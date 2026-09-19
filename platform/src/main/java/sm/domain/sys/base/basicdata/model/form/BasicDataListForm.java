package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 基础数据列表查询表单
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "基础数据列表表单")
public class BasicDataListForm extends PageForm {

    private Long categoryId;

    @Schema(description = "关键词（名称、编码模糊匹配）")
    private String keyword;
}

package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 基础数据删除命令。 */
@Data
@Schema(description = "基础数据删除表单")
public class BasicDataDeleteForm {

    @NotNull(message = "基础数据ID不能为空")
    private Long id;

    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}

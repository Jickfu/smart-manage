package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 基础数据删除命令。
 */
@Data
@Schema(description = "基础数据删除表单")
public class BasicDataDeleteForm {

    @Schema(description = "ID")
    @NotNull(message = "基础数据ID不能为空")
    private Long id;

    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}
